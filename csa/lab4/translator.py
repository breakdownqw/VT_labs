from __future__ import annotations

import argparse
from dataclasses import dataclass
from pathlib import Path

from isa import (
    INPUT_ADDR,
    OUTPUT_ADDR,
    WORD_SIZE_BYTES,
    Instruction,
    bytes_to_word,
    disassemble_program,
    encode_program,
    word_to_bytes,
    words_to_bytes,
)

DATA_START = 0x1000


@dataclass(frozen=True)
class Token:
    kind: str
    value: str


@dataclass
class PendingJump:
    instruction_index: int
    operand_index: int
    label: str


@dataclass
class PendingAddress:
    instruction_index: int
    operand_index: int
    label: str


@dataclass
class BuildResult:
    code: list[Instruction]
    memory_image: bytes
    disasm: str


class Translator:
    def __init__(self, source: str) -> None:
        self.tokens = tokenize(source)
        self.position = 0

        self.constants: dict[str, int] = {}
        self.data_labels: dict[str, int] = {}
        self.procedures: dict[str, list[Token]] = {}

        self.data_bytes = bytearray()

        self.instructions: list[Instruction] = []
        self.labels: dict[str, int] = {}
        self.pending_jumps: list[PendingJump] = []
        self.pending_addresses: list[PendingAddress] = []

        self.internal_label_counter = 0
        self.need_print_int = False
        self.print_int_buffer_label = "__print_int_buffer"
        self.print_int_buffer_size = 16

    def translate(self) -> BuildResult:
        self.parse_program()

        if "main" not in self.procedures:
            raise ValueError("Program must contain procedure ': main ... ;'")

        self.compile_all_procedures()
        self.resolve_jumps()
        self.resolve_addresses()

        code_words = encode_program(self.instructions)
        memory_image = self.build_memory_image(code_words)
        disasm = self.build_disasm(memory_image)

        return BuildResult(
            code=self.instructions,
            memory_image=memory_image,
            disasm=disasm,
        )

    def parse_program(self) -> None:
        while not self.is_end():
            token = self.peek()

            if token.value == "const":
                self.parse_const()
            elif token.value == "var":
                self.parse_var()
            elif token.value == "array":
                self.parse_array()
            elif token.value == "buffer":
                self.parse_buffer()
            elif token.value == "pstr":
                self.parse_pstr()
            elif token.value == ":":
                self.parse_procedure()
            else:
                raise ValueError(f"Unexpected top-level token: {token.value}")

    def parse_const(self) -> None:
        self.expect_value("const")
        name = self.expect_kind("word").value
        value_token = self.advance()

        self.constants[name] = token_to_number(value_token)

    def parse_var(self) -> None:
        self.expect_value("var")
        name = self.expect_kind("word").value

        if name in self.data_labels:
            raise ValueError(f"Data label is already defined: {name}")

        self.align_data_to_cell()
        self.data_labels[name] = self.current_data_address()
        self.append_data_word(0)

    def parse_array(self) -> None:
        self.expect_value("array")
        name = self.expect_kind("word").value
        size_token = self.advance()
        size = token_to_number(size_token)

        if size <= 0:
            raise ValueError("Array size must be positive")

        if name in self.data_labels:
            raise ValueError(f"Data label is already defined: {name}")

        self.align_data_to_cell()
        self.data_labels[name] = self.current_data_address()

        for _ in range(size):
            self.append_data_word(0)

    def parse_buffer(self) -> None:
        self.expect_value("buffer")
        name = self.expect_kind("word").value
        size_token = self.advance()
        size = token_to_number(size_token)

        if size <= 0:
            raise ValueError("Buffer size must be positive")

        if name in self.data_labels:
            raise ValueError(f"Data label is already defined: {name}")

        self.data_labels[name] = self.current_data_address()
        self.data_bytes.extend(b"\x00" * size)

    def parse_pstr(self) -> None:
        self.expect_value("pstr")
        name = self.expect_kind("word").value
        text_token = self.expect_kind("string")

        if name in self.data_labels:
            raise ValueError(f"Data label is already defined: {name}")

        self.align_data_to_cell()
        self.data_labels[name] = self.current_data_address()
        text = text_token.value

        self.append_data_word(len(text))
        self.data_bytes.extend(ord(char) for char in text)
        self.align_data_to_cell()

    def parse_procedure(self) -> None:
        self.expect_value(":")
        name = self.expect_kind("word").value

        if name in self.procedures:
            raise ValueError(f"Procedure is already defined: {name}")

        body: list[Token] = []

        while not self.is_end() and self.peek().value != ";":
            body.append(self.advance())

        self.expect_value(";")
        self.procedures[name] = body

    def compile_all_procedures(self) -> None:
        self.mark_label("main")
        self.compile_tokens(self.procedures["main"])

        if not self.instructions or self.instructions[-1].mnemonic != "halt":
            self.emit(Instruction("halt"))

        for name, body in self.procedures.items():
            if name == "main":
                continue

            self.mark_label(name)

            self.emit(Instruction("sw", ("ra", 0, "rp")))
            self.emit(Instruction("addi", ("rp", "rp", WORD_SIZE_BYTES)))

            self.compile_tokens(body)

            self.emit(Instruction("addi", ("rp", "rp", -WORD_SIZE_BYTES)))
            self.emit(Instruction("lw", ("ra", 0, "rp")))
            self.emit(Instruction("jr", ("ra",)))

    def compile_tokens(self, tokens: list[Token]) -> None:
        control_stack: list[tuple[str, str, str | None]] = []
        i = 0

        while i < len(tokens):
            token = tokens[i]
            value = token.value

            if token.kind in {"number", "char"}:
                self.emit_push_number(token_to_number(token))

            elif value in self.constants:
                self.emit_push_number(self.constants[value])

            elif value in self.data_labels:
                self.emit_push_number(self.data_labels[value])

            elif value == "'":
                if i + 1 >= len(tokens):
                    raise ValueError("Execution token expects procedure name")

                procedure_name = tokens[i + 1].value

                if procedure_name not in self.procedures:
                    raise ValueError(f"Unknown procedure for execution token: {procedure_name}")

                self.emit_push_label_address(procedure_name)
                i += 1

            elif value in self.procedures:
                self.emit_call(value)

            elif value == "+":
                self.emit_binary_stack_op("add")

            elif value == "-":
                self.emit_binary_stack_op("sub")

            elif value == "*":
                self.emit_binary_stack_op("mul")

            elif value == "/":
                self.emit_binary_stack_op("div")

            elif value == "mod":
                self.emit_binary_stack_op("rem")

            elif value == "=":
                self.emit_binary_stack_op("seq")

            elif value == "<":
                self.emit_binary_stack_op("slt")

            elif value == "u<":
                self.emit_binary_stack_op("sltu")

            elif value == ">":
                self.emit_binary_stack_op("sgt")

            elif value == "<=":
                self.emit_binary_stack_op("sle")

            elif value == ">=":
                self.emit_binary_stack_op("sge")

            elif value == "dup":
                self.emit_dup()

            elif value == "drop":
                self.emit_drop()

            elif value == "swap":
                self.emit_swap()

            elif value == "over":
                self.emit_over()

            elif value == "cells":
                self.emit_cells()

            elif value == "cell+":
                self.emit_cell_plus()

            elif value == "@":
                self.emit_fetch()

            elif value == "!":
                self.emit_store()

            elif value == "c@":
                self.emit_c_fetch()

            elif value == "c!":
                self.emit_c_store()

            elif value == "read-char":
                self.emit_read_char()

            elif value == "emit":
                self.emit_emit_char()

            elif value == "type-pstr":
                self.emit_type_pstr()

            elif value == ".":
                self.need_print_int = True
                self.emit_print_int()

            elif value == "execute":
                self.emit_execute()

            elif value == "halt":
                self.emit(Instruction("halt"))

            elif value == "if":
                false_label = self.new_internal_label("if_false")
                end_label = self.new_internal_label("if_end")
                self.emit_pop_to("t0")
                self.emit_branch("beqz", "t0", false_label)
                control_stack.append(("if", false_label, end_label))

            elif value == "else":
                if not control_stack or control_stack[-1][0] != "if":
                    raise ValueError("'else' without matching 'if'")

                _, false_label, if_end_label = control_stack[-1]

                if if_end_label is None:
                    raise ValueError("Internal error: if end label is missing")

                self.emit_jump(if_end_label)
                self.mark_label(false_label)
                control_stack[-1] = ("else", false_label, if_end_label)

            elif value == "then":
                if not control_stack or control_stack[-1][0] not in {"if", "else"}:
                    raise ValueError("'then' without matching 'if'")

                kind, false_label, then_end_label = control_stack.pop()

                if kind == "if":
                    self.mark_label(false_label)
                else:
                    if then_end_label is None:
                        raise ValueError("Internal error: if end label is missing")
                    self.mark_label(then_end_label)

            elif value == "begin":
                begin_label = self.new_internal_label("begin")
                self.mark_label(begin_label)
                control_stack.append(("begin", begin_label, None))

            elif value == "until":
                if not control_stack or control_stack[-1][0] != "begin":
                    raise ValueError("'until' without matching 'begin'")

                _, begin_label, _ = control_stack.pop()
                self.emit_pop_to("t0")
                self.emit_branch("beqz", "t0", begin_label)

            elif value == "while":
                if not control_stack or control_stack[-1][0] != "begin":
                    raise ValueError("'while' without matching 'begin'")

                _, begin_label, _ = control_stack.pop()
                end_label = self.new_internal_label("while_end")
                self.emit_pop_to("t0")
                self.emit_branch("beqz", "t0", end_label)
                control_stack.append(("while", begin_label, end_label))

            elif value == "repeat":
                if not control_stack or control_stack[-1][0] != "while":
                    raise ValueError("'repeat' without matching 'while'")

                _, begin_label, repeat_end_label = control_stack.pop()

                if repeat_end_label is None:
                    raise ValueError("Internal error: while end label is missing")

                self.emit_jump(begin_label)
                self.mark_label(repeat_end_label)

            elif value == "vload":
                self.compile_vector_load(tokens, i)
                i += 2

            elif value == "vstore":
                self.compile_vector_store(tokens, i)
                i += 2

            elif value in {"vadd", "vsub", "vmul", "vdiv", "veq"}:
                self.compile_vector_operation(tokens, i, value)
                i += 3

            else:
                raise ValueError(f"Unknown token: {value}")

            i += 1

        if control_stack:
            raise ValueError(f"Unclosed control structure: {control_stack[-1][0]}")

    def compile_vector_load(self, tokens: list[Token], index: int) -> None:
        if index + 2 >= len(tokens):
            raise ValueError("vload syntax: vload v0 array_name")

        vector_register = tokens[index + 1].value
        array_name = tokens[index + 2].value

        if array_name not in self.data_labels:
            raise ValueError(f"Unknown data label for vload: {array_name}")

        self.emit_load_address("t0", self.data_labels[array_name])
        self.emit(Instruction("vlw", (vector_register, 0, "t0")))

    def compile_vector_store(self, tokens: list[Token], index: int) -> None:
        if index + 2 >= len(tokens):
            raise ValueError("vstore syntax: vstore v0 array_name")

        vector_register = tokens[index + 1].value
        array_name = tokens[index + 2].value

        if array_name not in self.data_labels:
            raise ValueError(f"Unknown data label for vstore: {array_name}")

        self.emit_load_address("t0", self.data_labels[array_name])
        self.emit(Instruction("vsw", (vector_register, 0, "t0")))

    def compile_vector_operation(
        self,
        tokens: list[Token],
        index: int,
        operation: str,
    ) -> None:
        if index + 3 >= len(tokens):
            raise ValueError(f"{operation} syntax: {operation} v2 v0 v1")

        destination = tokens[index + 1].value
        left = tokens[index + 2].value
        right = tokens[index + 3].value

        self.emit(Instruction(operation, (destination, left, right)))

    def ensure_print_int_buffer(self) -> None:
        if self.print_int_buffer_label in self.data_labels:
            return

        self.data_labels[self.print_int_buffer_label] = self.current_data_address()
        self.data_bytes.extend(b"\x00" * self.print_int_buffer_size)

    def emit_push_number(self, value: int) -> None:
        self.emit_load_address("t0", value)
        self.emit_push_register("t0")

    def emit_push_label_address(self, label: str) -> None:
        self.emit(Instruction("addi", ("t0", "zero", 0)))

        self.pending_addresses.append(
            PendingAddress(
                instruction_index=len(self.instructions) - 1,
                operand_index=2,
                label=label,
            )
        )

        self.emit_push_register("t0")

    def emit_load_address(self, register: str, value: int) -> None:
        if -32768 <= value <= 32767:
            self.emit(Instruction("addi", (register, "zero", value)))
            return

        high = (value >> 12) & 0xFFFFF
        low = value & 0xFFF

        self.emit(Instruction("lui", (register, high)))
        self.emit(Instruction("addi", (register, register, low)))

    def emit_print_int(self) -> None:
        self.ensure_print_int_buffer()

        zero_label = self.new_internal_label("print_int_zero")
        positive_label = self.new_internal_label("print_int_positive")
        collect_loop = self.new_internal_label("print_int_collect")
        collect_end = self.new_internal_label("print_int_collect_end")
        output_loop = self.new_internal_label("print_int_output")
        output_end = self.new_internal_label("print_int_output_end")

        buffer_address = self.data_labels[self.print_int_buffer_label]

        self.emit_pop_to("t0")

        self.emit_branch("beqz", "t0", zero_label)

        self.emit(Instruction("slt", ("t4", "t0", "zero")))
        self.emit_branch("beqz", "t4", positive_label)
        self.emit(Instruction("addi", ("t4", "zero", ord("-"))))
        self.emit_load_address("t6", OUTPUT_ADDR)
        self.emit(Instruction("sb", ("t4", 0, "t6")))
        self.emit(Instruction("sub", ("t0", "zero", "t0")))
        self.mark_label(positive_label)

        self.emit_load_address("t1", buffer_address)

        self.emit(Instruction("addi", ("t2", "zero", 0)))

        self.emit(Instruction("addi", ("t3", "zero", 10)))

        self.mark_label(collect_loop)

        self.emit_branch("beqz", "t0", collect_end)

        self.emit(Instruction("rem", ("t4", "t0", "t3")))

        self.emit(Instruction("addi", ("t4", "t4", ord("0"))))

        self.emit(Instruction("add", ("t5", "t1", "t2")))
        self.emit(Instruction("sb", ("t4", 0, "t5")))

        self.emit(Instruction("div", ("t0", "t0", "t3")))

        self.emit(Instruction("addi", ("t2", "t2", 1)))

        self.emit_jump(collect_loop)

        self.mark_label(collect_end)

        self.mark_label(output_loop)

        self.emit_branch("beqz", "t2", output_end)

        self.emit(Instruction("addi", ("t2", "t2", -1)))

        self.emit(Instruction("add", ("t5", "t1", "t2")))
        self.emit(Instruction("lb", ("t4", 0, "t5")))

        self.emit_load_address("t6", OUTPUT_ADDR)
        self.emit(Instruction("sb", ("t4", 0, "t6")))

        self.emit_jump(output_loop)

        self.mark_label(output_end)
        self.emit_jump(zero_label + "_after")

        self.mark_label(zero_label)
        self.emit(Instruction("addi", ("t4", "zero", ord("0"))))
        self.emit_load_address("t6", OUTPUT_ADDR)
        self.emit(Instruction("sb", ("t4", 0, "t6")))

        self.mark_label(zero_label + "_after")

    def emit_push_register(self, register: str) -> None:
        self.emit(Instruction("sw", (register, 0, "sp")))
        self.emit(Instruction("addi", ("sp", "sp", WORD_SIZE_BYTES)))

    def emit_pop_to(self, register: str) -> None:
        self.emit(Instruction("addi", ("sp", "sp", -WORD_SIZE_BYTES)))
        self.emit(Instruction("lw", (register, 0, "sp")))

    def emit_binary_stack_op(self, instruction_name: str) -> None:
        self.emit_pop_to("t1")
        self.emit_pop_to("t0")
        self.emit(Instruction(instruction_name, ("t0", "t0", "t1")))
        self.emit_push_register("t0")

    def emit_dup(self) -> None:
        self.emit(Instruction("lw", ("t0", -WORD_SIZE_BYTES, "sp")))
        self.emit_push_register("t0")

    def emit_drop(self) -> None:
        self.emit(Instruction("addi", ("sp", "sp", -WORD_SIZE_BYTES)))

    def emit_swap(self) -> None:
        self.emit(Instruction("lw", ("t0", -WORD_SIZE_BYTES, "sp")))
        self.emit(Instruction("lw", ("t1", -2 * WORD_SIZE_BYTES, "sp")))
        self.emit(Instruction("sw", ("t0", -2 * WORD_SIZE_BYTES, "sp")))
        self.emit(Instruction("sw", ("t1", -WORD_SIZE_BYTES, "sp")))

    def emit_over(self) -> None:
        self.emit(Instruction("lw", ("t0", -2 * WORD_SIZE_BYTES, "sp")))
        self.emit_push_register("t0")

    def emit_cells(self) -> None:
        self.emit_pop_to("t0")
        self.emit(Instruction("addi", ("t1", "zero", WORD_SIZE_BYTES)))
        self.emit(Instruction("mul", ("t0", "t0", "t1")))
        self.emit_push_register("t0")

    def emit_cell_plus(self) -> None:
        self.emit_pop_to("t0")
        self.emit(Instruction("addi", ("t0", "t0", WORD_SIZE_BYTES)))
        self.emit_push_register("t0")

    def emit_fetch(self) -> None:
        self.emit_pop_to("t0")
        self.emit(Instruction("lw", ("t1", 0, "t0")))
        self.emit_push_register("t1")

    def emit_store(self) -> None:
        self.emit_pop_to("t1")
        self.emit_pop_to("t0")
        self.emit(Instruction("sw", ("t0", 0, "t1")))

    def emit_c_fetch(self) -> None:
        self.emit_pop_to("t0")
        self.emit(Instruction("lb", ("t1", 0, "t0")))
        self.emit_push_register("t1")

    def emit_c_store(self) -> None:
        self.emit_pop_to("t1")
        self.emit_pop_to("t0")
        self.emit(Instruction("sb", ("t0", 0, "t1")))

    def emit_read_char(self) -> None:
        self.emit_load_address("t0", INPUT_ADDR)
        self.emit(Instruction("lb", ("t1", 0, "t0")))
        self.emit_push_register("t1")

    def emit_emit_char(self) -> None:
        self.emit_pop_to("t1")
        self.emit_load_address("t0", OUTPUT_ADDR)
        self.emit(Instruction("sb", ("t1", 0, "t0")))

    def emit_type_pstr(self) -> None:
        loop_label = self.new_internal_label("type_pstr_loop")
        end_label = self.new_internal_label("type_pstr_end")

        self.emit_pop_to("t0")
        self.emit(Instruction("lw", ("t1", 0, "t0")))
        self.emit(Instruction("addi", ("t2", "zero", 0)))
        self.emit(Instruction("addi", ("t4", "t0", WORD_SIZE_BYTES)))

        self.mark_label(loop_label)

        self.emit(Instruction("slt", ("t3", "t2", "t1")))
        self.emit_branch("beqz", "t3", end_label)

        self.emit(Instruction("lb", ("t5", 0, "t4")))

        self.emit_load_address("t6", OUTPUT_ADDR)
        self.emit(Instruction("sb", ("t5", 0, "t6")))

        self.emit(Instruction("addi", ("t2", "t2", 1)))
        self.emit(Instruction("addi", ("t4", "t4", 1)))
        self.emit_jump(loop_label)

        self.mark_label(end_label)

    def emit_execute(self) -> None:
        self.emit_pop_to("t0")
        self.emit(Instruction("jalr", ("ra", "t0")))

    def emit_call(self, label: str) -> None:
        self.emit(Instruction("jal", ("ra", 0)))
        self.pending_jumps.append(
            PendingJump(
                instruction_index=len(self.instructions) - 1,
                operand_index=1,
                label=label,
            )
        )

    def emit_jump(self, label: str) -> None:
        self.emit(Instruction("j", (0,)))
        self.pending_jumps.append(
            PendingJump(
                instruction_index=len(self.instructions) - 1,
                operand_index=0,
                label=label,
            )
        )

    def emit_branch(self, instruction_name: str, register: str, label: str) -> None:
        self.emit(Instruction(instruction_name, (register, 0)))
        self.pending_jumps.append(
            PendingJump(
                instruction_index=len(self.instructions) - 1,
                operand_index=1,
                label=label,
            )
        )

    def emit(self, instruction: Instruction) -> None:
        self.instructions.append(instruction)

    def mark_label(self, label: str) -> None:
        if label in self.labels:
            raise ValueError(f"Label is already defined: {label}")

        self.labels[label] = len(self.instructions)

    def new_internal_label(self, prefix: str) -> str:
        label = f"__{prefix}_{self.internal_label_counter}"
        self.internal_label_counter += 1
        return label

    def resolve_jumps(self) -> None:
        for pending in self.pending_jumps:
            if pending.label not in self.labels:
                raise ValueError(f"Unknown label: {pending.label}")

            target_address = self.labels[pending.label] * WORD_SIZE_BYTES
            next_address = (pending.instruction_index + 1) * WORD_SIZE_BYTES
            offset = target_address - next_address

            instruction = self.instructions[pending.instruction_index]
            operands = list(instruction.operands)
            operands[pending.operand_index] = offset

            self.instructions[pending.instruction_index] = Instruction(
                instruction.mnemonic,
                tuple(operands),
            )

    def resolve_addresses(self) -> None:
        for pending in self.pending_addresses:
            if pending.label not in self.labels:
                raise ValueError(f"Unknown label address: {pending.label}")

            address = self.labels[pending.label] * WORD_SIZE_BYTES

            instruction = self.instructions[pending.instruction_index]
            operands = list(instruction.operands)
            operands[pending.operand_index] = address

            self.instructions[pending.instruction_index] = Instruction(
                instruction.mnemonic,
                tuple(operands),
            )

    def build_memory_image(self, code_words: list[int]) -> bytes:
        code_bytes = words_to_bytes(code_words)

        if len(code_bytes) > DATA_START:
            raise ValueError(
                f"Code section is too large: {len(code_bytes)} bytes, "
                f"data starts at 0x{DATA_START:X}"
            )

        return code_bytes + bytes(DATA_START - len(code_bytes)) + bytes(self.data_bytes)

    def build_disasm(self, memory_image: bytes) -> str:
        code_size = len(self.instructions) * WORD_SIZE_BYTES
        code_words = [
            bytes_to_word(memory_image[index : index + WORD_SIZE_BYTES])
            for index in range(0, code_size, WORD_SIZE_BYTES)
        ]
        lines = [".text"]
        lines.append(disassemble_program(code_words))

        if self.data_bytes:
            lines.append("")
            lines.append(".data")

            for index in range(0, len(self.data_bytes), WORD_SIZE_BYTES):
                address = DATA_START + index
                chunk = bytes(self.data_bytes[index : index + WORD_SIZE_BYTES])

                if len(chunk) == WORD_SIZE_BYTES:
                    word = bytes_to_word(chunk)
                    lines.append(f"{address:04X} - {word & 0xFFFFFFFF:08X} - .word {word}")
                else:
                    hex_bytes = " ".join(f"{byte:02X}" for byte in chunk)
                    lines.append(f"{address:04X} - {hex_bytes:<11} - .bytes {list(chunk)}")

        return "\n".join(lines)

    def current_data_address(self) -> int:
        return DATA_START + len(self.data_bytes)

    def append_data_word(self, value: int) -> None:
        self.data_bytes.extend(word_to_bytes(value))

    def align_data_to_cell(self) -> None:
        padding = (-len(self.data_bytes)) % WORD_SIZE_BYTES

        if padding:
            self.data_bytes.extend(b"\x00" * padding)

    def is_end(self) -> bool:
        return self.position >= len(self.tokens)

    def peek(self) -> Token:
        if self.is_end():
            raise ValueError("Unexpected end of source")

        return self.tokens[self.position]

    def advance(self) -> Token:
        token = self.peek()
        self.position += 1
        return token

    def expect_value(self, value: str) -> Token:
        token = self.advance()

        if token.value != value:
            raise ValueError(f"Expected token {value!r}, got {token.value!r}")

        return token

    def expect_kind(self, kind: str) -> Token:
        token = self.advance()

        if token.kind != kind:
            raise ValueError(f"Expected {kind}, got {token.kind}: {token.value}")

        return token


def tokenize(source: str) -> list[Token]:
    tokens: list[Token] = []
    i = 0

    while i < len(source):
        char = source[i]

        if char.isspace():
            i += 1
            continue

        if char == "\\":
            while i < len(source) and source[i] != "\n":
                i += 1
            continue

        if char == '"':
            value, i = read_string(source, i)
            tokens.append(Token("string", value))
            continue

        if char == "'":
            if is_execution_token_marker(source, i):
                tokens.append(Token("word", "'"))
                i += 1
                continue

            char_value, i = read_char_literal(source, i)
            tokens.append(Token("char", str(char_value)))
            continue

        start = i

        while i < len(source) and not source[i].isspace():
            if source[i] == "\\":
                break
            i += 1

        text = source[start:i]

        if is_number(text):
            tokens.append(Token("number", text))
        else:
            tokens.append(Token("word", text))

    return tokens


def read_string(source: str, start: int) -> tuple[str, int]:
    result: list[str] = []
    i = start + 1

    while i < len(source):
        char = source[i]

        if char == '"':
            return "".join(result), i + 1

        if char == "\\":
            escaped, i = read_escape(source, i)
            result.append(escaped)
            continue

        result.append(char)
        i += 1

    raise ValueError("Unclosed string literal")


def read_char_literal(source: str, start: int) -> tuple[int, int]:
    i = start + 1

    if i >= len(source):
        raise ValueError("Unclosed char literal")

    if source[i] == "\\":
        char, i = read_escape(source, i)
    else:
        char = source[i]
        i += 1

    if i >= len(source) or source[i] != "'":
        raise ValueError("Char literal must contain exactly one character")

    return ord(char), i + 1


def is_execution_token_marker(source: str, index: int) -> bool:
    next_index = index + 1

    if next_index >= len(source):
        return False

    if not source[next_index].isspace():
        return False

    return not (next_index + 1 < len(source) and source[next_index + 1] == "'")


def read_escape(source: str, start: int) -> tuple[str, int]:
    if start + 1 >= len(source):
        raise ValueError("Invalid escape sequence")

    escaped = source[start + 1]

    if escaped == "n":
        return "\n", start + 2

    if escaped == "t":
        return "\t", start + 2

    if escaped == "r":
        return "\r", start + 2

    if escaped == "\\":
        return "\\", start + 2

    if escaped == '"':
        return '"', start + 2

    if escaped == "'":
        return "'", start + 2

    raise ValueError(f"Unknown escape sequence: \\{escaped}")


def is_number(text: str) -> bool:
    if not text:
        return False

    if text.startswith("-"):
        return text[1:].isdigit()

    if text.startswith("0x"):
        return len(text) > 2 and all(char in "0123456789abcdefABCDEF" for char in text[2:])

    return text.isdigit()


def token_to_number(token: Token) -> int:
    if token.kind == "char":
        return int(token.value)

    if token.kind not in {"number", "word"}:
        raise ValueError(f"Token cannot be converted to number: {token}")

    text = token.value

    if text.startswith("-0x"):
        return -int(text[3:], 16)

    if text.startswith("0x"):
        return int(text[2:], 16)

    return int(text)


def translate_source(source: str) -> BuildResult:
    return Translator(source).translate()


def translate_file(source_path: Path, binary_path: Path, disasm_path: Path) -> None:
    source = source_path.read_text(encoding="utf-8")
    result = translate_source(source)

    binary_path.write_bytes(result.memory_image)
    disasm_path.write_text(result.disasm + "\n", encoding="utf-8")


def main() -> None:
    parser = argparse.ArgumentParser(description="Translate Forth source to binary machine code")
    parser.add_argument("source", type=Path)
    parser.add_argument("binary", type=Path)
    parser.add_argument("disasm", type=Path)

    args = parser.parse_args()

    translate_file(args.source, args.binary, args.disasm)


if __name__ == "__main__":
    main()
