from __future__ import annotations

import argparse
from dataclasses import dataclass
from enum import IntEnum
from pathlib import Path

from isa import (
    INPUT_ADDR,
    OUTPUT_ADDR,
    VECTOR_LENGTH,
    WORD_MASK,
    Opcode,
    bytes_to_words,
    decode_instruction,
    disassemble_instruction,
    register_name,
    sign_extend,
    vector_register_name,
)

FETCH_0 = 0x000
FETCH_1 = 0x001
FETCH_2 = 0x002
DECODE = 0x003

HALT_0 = 0x010

LUI_0 = 0x020
LUI_1 = 0x021

ADDI_0 = 0x030
ADDI_1 = 0x031
ADDI_2 = 0x032
ADDI_3 = 0x033

R_0 = 0x040
R_1 = 0x041
R_2 = 0x042
R_3 = 0x043

LW_0 = 0x050
LW_1 = 0x051
LW_2 = 0x052
LW_3 = 0x053
LW_4 = 0x054

SW_0 = 0x060
SW_1 = 0x061
SW_2 = 0x062
SW_3 = 0x063
SW_4 = 0x064

J_0 = 0x070
J_1 = 0x071
J_2 = 0x072

BEQZ_0 = 0x080
BEQZ_1 = 0x081
BEQZ_2 = 0x082

BNEZ_0 = 0x090
BNEZ_1 = 0x091
BNEZ_2 = 0x092

JAL_0 = 0x0A0
JAL_1 = 0x0A1
JAL_2 = 0x0A2
JAL_3 = 0x0A3

JR_0 = 0x0B0
JR_1 = 0x0B1

JALR_0 = 0x0C0
JALR_1 = 0x0C1
JALR_2 = 0x0C2

VLW_0 = 0x0D0
VLW_1 = 0x0D1
VLW_2 = 0x0D2
VLW_3 = 0x0D3
VLW_4 = 0x0D4
VLW_5 = 0x0D5
VLW_6 = 0x0D6

VSW_0 = 0x0E0
VSW_1 = 0x0E1
VSW_2 = 0x0E2
VSW_3 = 0x0E3
VSW_4 = 0x0E4
VSW_5 = 0x0E5
VSW_6 = 0x0E6

V_0 = 0x0F0
V_1 = 0x0F1


R_OPCODES = {
    Opcode.ADD,
    Opcode.SUB,
    Opcode.MUL,
    Opcode.DIV,
    Opcode.REM,
    Opcode.SEQ,
    Opcode.SLT,
    Opcode.SGT,
    Opcode.SLE,
    Opcode.SGE,
    Opcode.SLTU,
}

V_OPCODES = {
    Opcode.VADD,
    Opcode.VSUB,
    Opcode.VMUL,
    Opcode.VDIV,
    Opcode.VEQ,
}


@dataclass(frozen=True)
class MicroInstruction:
    name: str
    microcommand: int
    next_upc: int | None = None


class ControlSignal(IntEnum):
    PC_TO_MAR = 0
    MEMORY_TO_MDR = 1
    MDR_TO_IR = 2
    INC_PC = 3
    DISPATCH = 4
    HALT = 5
    IMM20_SHIFT_TO_ALU = 6
    ALU_TO_RD = 7
    RS1_TO_A = 8
    RS2_TO_B = 9
    RD_TO_A = 10
    IMM16_TO_B = 11
    OFFSET20_TO_B = 12
    OFFSET24_TO_B = 13
    A_PLUS_B_TO_ALU = 14
    R_OPERATION_TO_ALU = 15
    ALU_TO_MAR = 16
    MDR_TO_RD = 17
    RD_TO_MDR = 18
    MDR_TO_MEMORY = 19
    PC_TO_A = 20
    ALU_TO_PC = 21
    PC_TO_RD = 22
    A_TO_PC = 23
    PC_PLUS_OFFSET20_IF_A_ZERO = 24
    PC_PLUS_OFFSET20_IF_A_NOT_ZERO = 25
    VECTOR_LOAD_0 = 26
    VECTOR_LOAD_1 = 27
    VECTOR_LOAD_2 = 28
    VECTOR_LOAD_3 = 29
    VECTOR_STORE_0 = 30
    VECTOR_STORE_1 = 31
    VECTOR_STORE_2 = 32
    VECTOR_STORE_3 = 33
    V_OPERATION_TO_VD = 34
    A_PLUS_B_TO_MAR = 35
    A_PLUS_B_TO_PC = 36


class Machine:
    def __init__(
        self,
        program: list[int],
        input_text: str = "",
        memory_size: int = 4096,
        start_pc: int = 0,
        start_sp: int = 0x0800,
        start_rp: int = 0x0C00,
        log_limit: int = 10000,
    ) -> None:
        if len(program) > memory_size:
            raise ValueError("Program does not fit into memory")

        self.memory = [0] * memory_size

        for address, word in enumerate(program):
            self.memory[address] = word & WORD_MASK

        self.registers: dict[str, int] = {
            "zero": 0,
            "ra": 0,
            "sp": start_sp,
            "rp": start_rp,
            "t0": 0,
            "t1": 0,
            "t2": 0,
            "t3": 0,
            "t4": 0,
            "t5": 0,
            "t6": 0,
        }

        self.vector_registers: dict[str, list[int]] = {
            "v0": [0] * VECTOR_LENGTH,
            "v1": [0] * VECTOR_LENGTH,
            "v2": [0] * VECTOR_LENGTH,
            "v3": [0] * VECTOR_LENGTH,
        }

        self.pc = start_pc
        self.ir = 0
        self.mar = 0
        self.mdr = 0
        self.a = 0
        self.b = 0
        self.alu = 0

        self.upc = FETCH_0
        self.tick_count = 0
        self.halted = False
        self.input_exhausted = False

        self.input_buffer = [ord(char) for char in input_text]
        self.output_buffer: list[int] = []

        self.log: list[str] = []
        self.log_limit = log_limit

        self.microcode = build_microcode()
        self.dispatch_table = build_dispatch_table()

    def run(self, limit: int = 10000) -> str:
        while not self.halted and self.tick_count < limit:
            self.tick()

        if self.tick_count >= limit:
            raise RuntimeError(f"Simulation limit exceeded: {limit} ticks")

        return self.output_text()

    def tick(self) -> None:
        if self.halted:
            return

        if self.upc not in self.microcode:
            raise RuntimeError(f"Unknown microcode address: 0x{self.upc:03X}")

        micro_instruction = self.microcode[self.upc]

        before = self.short_state()
        next_upc = self.execute_microinstruction(micro_instruction)
        after = self.short_state()
        signals = microcommand_to_text(micro_instruction.microcommand)

        if self.log_limit > 0 and len(self.log) < self.log_limit:
            self.log.append(
                f"TICK {self.tick_count:06d} | "
                f"uPC={self.upc:03X} | "
                f"{micro_instruction.name:<10} | "
                f"signals={signals:<45} | "
                f"{before} -> {after}"
            )
        elif 0 < self.log_limit == len(self.log):
            self.log.append(f"... log truncated after {self.log_limit} ticks ...")

        if next_upc is not None:
            self.upc = next_upc

        self.registers["zero"] = 0
        self.tick_count += 1

    def execute_microinstruction(self, micro_instruction: MicroInstruction) -> int | None:
        microcommand = micro_instruction.microcommand

        if has_signal(microcommand, ControlSignal.PC_TO_MAR):
            self.mar = self.pc & WORD_MASK

        if has_signal(microcommand, ControlSignal.MEMORY_TO_MDR):
            self.mdr = self.read_memory(self.mar)

        if has_signal(microcommand, ControlSignal.MDR_TO_IR):
            self.ir = self.mdr & WORD_MASK

        if has_signal(microcommand, ControlSignal.INC_PC):
            self.pc = (self.pc + 1) & WORD_MASK

        if has_signal(microcommand, ControlSignal.DISPATCH):
            opcode = self.opcode()

            if opcode not in self.dispatch_table:
                raise RuntimeError(f"No microprogram for opcode: {opcode}")

            return self.dispatch_table[opcode]

        if has_signal(microcommand, ControlSignal.HALT):
            self.halted = True
            return None

        if has_signal(microcommand, ControlSignal.IMM20_SHIFT_TO_ALU):
            self.alu = (self.imm20() << 12) & WORD_MASK

        if has_signal(microcommand, ControlSignal.RS1_TO_A):
            self.a = self.get_register_by_code(self.rs1())

        if has_signal(microcommand, ControlSignal.RS2_TO_B):
            self.b = self.get_register_by_code(self.rs2())

        if has_signal(microcommand, ControlSignal.RD_TO_A):
            self.a = self.get_register_by_code(self.rd())

        if has_signal(microcommand, ControlSignal.IMM16_TO_B):
            self.b = self.imm16()

        if has_signal(microcommand, ControlSignal.OFFSET20_TO_B):
            self.b = self.offset20()

        if has_signal(microcommand, ControlSignal.OFFSET24_TO_B):
            self.b = self.offset24()

        if has_signal(microcommand, ControlSignal.A_PLUS_B_TO_ALU):
            self.alu = (self.a + self.b) & WORD_MASK

        if has_signal(microcommand, ControlSignal.R_OPERATION_TO_ALU):
            self.alu = self.run_r_operation() & WORD_MASK

        if has_signal(microcommand, ControlSignal.ALU_TO_MAR):
            self.mar = self.alu & WORD_MASK

        if has_signal(microcommand, ControlSignal.A_PLUS_B_TO_MAR):
            self.mar = (self.a + self.b) & WORD_MASK

        if has_signal(microcommand, ControlSignal.ALU_TO_RD):
            self.set_register_by_code(self.rd(), self.alu)

        if has_signal(microcommand, ControlSignal.MDR_TO_RD):
            self.set_register_by_code(self.rd(), self.mdr)

        if has_signal(microcommand, ControlSignal.RD_TO_MDR):
            self.mdr = self.get_register_by_code(self.rd())

        if has_signal(microcommand, ControlSignal.MDR_TO_MEMORY):
            self.write_memory(self.mar, self.mdr)

        if has_signal(microcommand, ControlSignal.PC_TO_A):
            self.a = self.pc

        if has_signal(microcommand, ControlSignal.ALU_TO_PC):
            self.pc = self.alu & WORD_MASK

        if has_signal(microcommand, ControlSignal.A_PLUS_B_TO_PC):
            self.pc = (self.a + self.b) & WORD_MASK

        if has_signal(microcommand, ControlSignal.PC_TO_RD):
            self.set_register_by_code(self.rd(), self.pc)

        if has_signal(microcommand, ControlSignal.A_TO_PC):
            self.pc = self.a & WORD_MASK

        if has_signal(microcommand, ControlSignal.PC_PLUS_OFFSET20_IF_A_ZERO) and self.a == 0:
            self.pc = (self.pc + self.offset20()) & WORD_MASK

        if has_signal(microcommand, ControlSignal.PC_PLUS_OFFSET20_IF_A_NOT_ZERO) and self.a != 0:
            self.pc = (self.pc + self.offset20()) & WORD_MASK

        self.execute_vector_signals(microcommand)

        if has_signal(microcommand, ControlSignal.V_OPERATION_TO_VD):
            self.set_vector_register_by_code(self.rd(), self.run_v_operation())

        return micro_instruction.next_upc

    def execute_vector_signals(self, microcommand: int) -> None:
        for signal, index in (
            (ControlSignal.VECTOR_LOAD_0, 0),
            (ControlSignal.VECTOR_LOAD_1, 1),
            (ControlSignal.VECTOR_LOAD_2, 2),
            (ControlSignal.VECTOR_LOAD_3, 3),
        ):
            if has_signal(microcommand, signal):
                values = self.get_vector_register_by_code(self.rd()).copy()
                values[index] = self.read_memory(self.mar + index)
                self.set_vector_register_by_code(self.rd(), values)

        for signal, index in (
            (ControlSignal.VECTOR_STORE_0, 0),
            (ControlSignal.VECTOR_STORE_1, 1),
            (ControlSignal.VECTOR_STORE_2, 2),
            (ControlSignal.VECTOR_STORE_3, 3),
        ):
            if has_signal(microcommand, signal):
                values = self.get_vector_register_by_code(self.rd())
                self.write_memory(self.mar + index, values[index])

    def short_state(self) -> str:
        instruction_text = self.current_instruction_text()

        return (
            f"PC={self.pc:04X} "
            f"IR={self.ir:08X} "
            f"MAR={self.mar:04X} "
            f"MDR={self.mdr:08X} "
            f"A={self.a:08X} "
            f"B={self.b:08X} "
            f"ALU={self.alu:08X} "
            f"SP={self.registers['sp']:04X} "
            f"RP={self.registers['rp']:04X} "
            f"INST={instruction_text}"
        )

    def current_instruction_text(self) -> str:
        if self.ir == 0:
            return "halt"

        try:
            return disassemble_instruction(decode_instruction(self.ir))
        except ValueError:
            return "?"

    def output_text(self) -> str:
        return "".join(chr(value & 0xFF) for value in self.output_buffer)

    def read_memory(self, address: int) -> int:
        address &= WORD_MASK

        if address == INPUT_ADDR:
            if not self.input_buffer:
                self.input_exhausted = True
                self.halted = True
                return 0

            return self.input_buffer.pop(0)

        self.check_memory_address(address)
        return self.memory[address]

    def write_memory(self, address: int, value: int) -> None:
        address &= WORD_MASK
        value &= WORD_MASK

        if address == OUTPUT_ADDR:
            self.output_buffer.append(value & 0xFF)
            return

        self.check_memory_address(address)
        self.memory[address] = value

    def check_memory_address(self, address: int) -> None:
        if not 0 <= address < len(self.memory):
            raise RuntimeError(f"Memory address is out of range: 0x{address:X}")

    def get_register_by_code(self, code: int) -> int:
        return self.registers[register_name(code)]

    def set_register_by_code(self, code: int, value: int) -> None:
        name = register_name(code)

        if name == "zero":
            return

        self.registers[name] = value & WORD_MASK

    def get_vector_register_by_code(self, code: int) -> list[int]:
        return self.vector_registers[vector_register_name(code)]

    def set_vector_register_by_code(self, code: int, values: list[int]) -> None:
        name = vector_register_name(code)

        if len(values) != VECTOR_LENGTH:
            raise RuntimeError("Invalid vector register length")

        self.vector_registers[name] = [value & WORD_MASK for value in values]

    def opcode(self) -> Opcode:
        value = (self.ir >> 24) & 0xFF

        try:
            return Opcode(value)
        except ValueError as error:
            raise RuntimeError(f"Unknown opcode: 0x{value:02X}") from error

    def rd(self) -> int:
        return (self.ir >> 20) & 0xF

    def rs1(self) -> int:
        return (self.ir >> 16) & 0xF

    def rs2(self) -> int:
        return (self.ir >> 12) & 0xF

    def imm16(self) -> int:
        return sign_extend(self.ir & 0xFFFF, 16)

    def imm20(self) -> int:
        return self.ir & 0xFFFFF

    def offset20(self) -> int:
        return sign_extend(self.ir & 0xFFFFF, 20)

    def offset24(self) -> int:
        return sign_extend(self.ir & 0xFFFFFF, 24)

    def run_r_operation(self) -> int:
        opcode = self.opcode()

        if opcode == Opcode.ADD:
            return self.a + self.b

        if opcode == Opcode.SUB:
            return self.a - self.b

        if opcode == Opcode.MUL:
            return self.a * self.b

        if opcode == Opcode.DIV:
            if self.b == 0:
                raise RuntimeError("Division by zero")
            return int(self.a / self.b)

        if opcode == Opcode.REM:
            if self.b == 0:
                raise RuntimeError("Remainder by zero")
            return self.a % self.b

        if opcode == Opcode.SEQ:
            return int(self.a == self.b)

        if opcode == Opcode.SLT:
            return int(to_signed32(self.a) < to_signed32(self.b))

        if opcode == Opcode.SLTU:
            return int((self.a & WORD_MASK) < (self.b & WORD_MASK))

        if opcode == Opcode.SGT:
            return int(to_signed32(self.a) > to_signed32(self.b))

        if opcode == Opcode.SLE:
            return int(to_signed32(self.a) <= to_signed32(self.b))

        if opcode == Opcode.SGE:
            return int(to_signed32(self.a) >= to_signed32(self.b))

        raise RuntimeError(f"Unsupported R operation: {opcode}")

    def run_v_operation(self) -> list[int]:
        opcode = self.opcode()
        left = self.get_vector_register_by_code(self.rs1())
        right = self.get_vector_register_by_code(self.rs2())
        result = []

        for i in range(VECTOR_LENGTH):
            a = left[i]
            b = right[i]

            if opcode == Opcode.VADD:
                result.append(a + b)
            elif opcode == Opcode.VSUB:
                result.append(a - b)
            elif opcode == Opcode.VMUL:
                result.append(a * b)
            elif opcode == Opcode.VDIV:
                if b == 0:
                    raise RuntimeError("Vector division by zero")
                result.append(int(a / b))
            elif opcode == Opcode.VEQ:
                result.append(int(a == b))
            else:
                raise RuntimeError(f"Unsupported vector operation: {opcode}")

        return [value & WORD_MASK for value in result]


def build_microcode() -> dict[int, MicroInstruction]:
    return {
        FETCH_0: MicroInstruction("FETCH_0", mc(ControlSignal.PC_TO_MAR), FETCH_1),
        FETCH_1: MicroInstruction("FETCH_1", mc(ControlSignal.MEMORY_TO_MDR), FETCH_2),
        FETCH_2: MicroInstruction(
            "FETCH_2",
            mc(ControlSignal.MDR_TO_IR, ControlSignal.INC_PC),
            DECODE,
        ),
        DECODE: MicroInstruction("DECODE", mc(ControlSignal.DISPATCH)),
        HALT_0: MicroInstruction("HALT_0", mc(ControlSignal.HALT)),
        LUI_0: MicroInstruction("LUI_0", mc(ControlSignal.IMM20_SHIFT_TO_ALU), LUI_1),
        LUI_1: MicroInstruction("LUI_1", mc(ControlSignal.ALU_TO_RD), FETCH_0),
        ADDI_0: MicroInstruction("ADDI_0", mc(ControlSignal.RS1_TO_A), ADDI_1),
        ADDI_1: MicroInstruction("ADDI_1", mc(ControlSignal.IMM16_TO_B), ADDI_2),
        ADDI_2: MicroInstruction("ADDI_2", mc(ControlSignal.A_PLUS_B_TO_ALU), ADDI_3),
        ADDI_3: MicroInstruction("ADDI_3", mc(ControlSignal.ALU_TO_RD), FETCH_0),
        R_0: MicroInstruction("R_0", mc(ControlSignal.RS1_TO_A), R_1),
        R_1: MicroInstruction("R_1", mc(ControlSignal.RS2_TO_B), R_2),
        R_2: MicroInstruction("R_2", mc(ControlSignal.R_OPERATION_TO_ALU), R_3),
        R_3: MicroInstruction("R_3", mc(ControlSignal.ALU_TO_RD), FETCH_0),
        LW_0: MicroInstruction("LW_0", mc(ControlSignal.RS1_TO_A), LW_1),
        LW_1: MicroInstruction("LW_1", mc(ControlSignal.IMM16_TO_B), LW_2),
        LW_2: MicroInstruction(
            "LW_2",
            mc(ControlSignal.A_PLUS_B_TO_MAR),
            LW_3,
        ),
        LW_3: MicroInstruction("LW_3", mc(ControlSignal.MEMORY_TO_MDR), LW_4),
        LW_4: MicroInstruction("LW_4", mc(ControlSignal.MDR_TO_RD), FETCH_0),
        SW_0: MicroInstruction("SW_0", mc(ControlSignal.RS1_TO_A), SW_1),
        SW_1: MicroInstruction("SW_1", mc(ControlSignal.IMM16_TO_B), SW_2),
        SW_2: MicroInstruction(
            "SW_2",
            mc(ControlSignal.A_PLUS_B_TO_MAR),
            SW_3,
        ),
        SW_3: MicroInstruction("SW_3", mc(ControlSignal.RD_TO_MDR), SW_4),
        SW_4: MicroInstruction("SW_4", mc(ControlSignal.MDR_TO_MEMORY), FETCH_0),
        J_0: MicroInstruction("J_0", mc(ControlSignal.PC_TO_A), J_1),
        J_1: MicroInstruction("J_1", mc(ControlSignal.OFFSET24_TO_B), J_2),
        J_2: MicroInstruction(
            "J_2",
            mc(ControlSignal.A_PLUS_B_TO_PC),
            FETCH_0,
        ),
        BEQZ_0: MicroInstruction("BEQZ_0", mc(ControlSignal.RD_TO_A), BEQZ_1),
        BEQZ_1: MicroInstruction(
            "BEQZ_1",
            mc(ControlSignal.PC_PLUS_OFFSET20_IF_A_ZERO),
            BEQZ_2,
        ),
        BEQZ_2: MicroInstruction("BEQZ_2", mc(), FETCH_0),
        BNEZ_0: MicroInstruction("BNEZ_0", mc(ControlSignal.RD_TO_A), BNEZ_1),
        BNEZ_1: MicroInstruction(
            "BNEZ_1",
            mc(ControlSignal.PC_PLUS_OFFSET20_IF_A_NOT_ZERO),
            BNEZ_2,
        ),
        BNEZ_2: MicroInstruction("BNEZ_2", mc(), FETCH_0),
        JAL_0: MicroInstruction("JAL_0", mc(ControlSignal.PC_TO_RD), JAL_1),
        JAL_1: MicroInstruction("JAL_1", mc(ControlSignal.PC_TO_A), JAL_2),
        JAL_2: MicroInstruction("JAL_2", mc(ControlSignal.OFFSET20_TO_B), JAL_3),
        JAL_3: MicroInstruction(
            "JAL_3",
            mc(ControlSignal.A_PLUS_B_TO_PC),
            FETCH_0,
        ),
        JR_0: MicroInstruction("JR_0", mc(ControlSignal.RD_TO_A), JR_1),
        JR_1: MicroInstruction("JR_1", mc(ControlSignal.A_TO_PC), FETCH_0),
        JALR_0: MicroInstruction("JALR_0", mc(ControlSignal.RS1_TO_A), JALR_1),
        JALR_1: MicroInstruction("JALR_1", mc(ControlSignal.PC_TO_RD), JALR_2),
        JALR_2: MicroInstruction("JALR_2", mc(ControlSignal.A_TO_PC), FETCH_0),
        VLW_0: MicroInstruction("VLW_0", mc(ControlSignal.RS1_TO_A), VLW_1),
        VLW_1: MicroInstruction("VLW_1", mc(ControlSignal.IMM16_TO_B), VLW_2),
        VLW_2: MicroInstruction(
            "VLW_2",
            mc(ControlSignal.A_PLUS_B_TO_MAR),
            VLW_3,
        ),
        VLW_3: MicroInstruction("VLW_3", mc(ControlSignal.VECTOR_LOAD_0), VLW_4),
        VLW_4: MicroInstruction("VLW_4", mc(ControlSignal.VECTOR_LOAD_1), VLW_5),
        VLW_5: MicroInstruction("VLW_5", mc(ControlSignal.VECTOR_LOAD_2), VLW_6),
        VLW_6: MicroInstruction("VLW_6", mc(ControlSignal.VECTOR_LOAD_3), FETCH_0),
        VSW_0: MicroInstruction("VSW_0", mc(ControlSignal.RS1_TO_A), VSW_1),
        VSW_1: MicroInstruction("VSW_1", mc(ControlSignal.IMM16_TO_B), VSW_2),
        VSW_2: MicroInstruction(
            "VSW_2",
            mc(ControlSignal.A_PLUS_B_TO_MAR),
            VSW_3,
        ),
        VSW_3: MicroInstruction("VSW_3", mc(ControlSignal.VECTOR_STORE_0), VSW_4),
        VSW_4: MicroInstruction("VSW_4", mc(ControlSignal.VECTOR_STORE_1), VSW_5),
        VSW_5: MicroInstruction("VSW_5", mc(ControlSignal.VECTOR_STORE_2), VSW_6),
        VSW_6: MicroInstruction("VSW_6", mc(ControlSignal.VECTOR_STORE_3), FETCH_0),
        V_0: MicroInstruction("V_0", mc(ControlSignal.V_OPERATION_TO_VD), V_1),
        V_1: MicroInstruction("V_1", mc(), FETCH_0),
    }


def mc(*signals: ControlSignal) -> int:
    microcommand = 0

    for signal in signals:
        microcommand |= 1 << signal.value

    return microcommand


def has_signal(microcommand: int, signal: ControlSignal) -> bool:
    return bool(microcommand & (1 << signal.value))


def microcommand_to_text(microcommand: int) -> str:
    names = []

    for signal in ControlSignal:
        if has_signal(microcommand, signal):
            names.append(signal.name)

    if not names:
        return "NO_SIGNALS"

    return "|".join(names)


def build_dispatch_table() -> dict[Opcode, int]:
    table = {
        Opcode.HALT: HALT_0,
        Opcode.LUI: LUI_0,
        Opcode.ADDI: ADDI_0,
        Opcode.LW: LW_0,
        Opcode.SW: SW_0,
        Opcode.J: J_0,
        Opcode.BEQZ: BEQZ_0,
        Opcode.BNEZ: BNEZ_0,
        Opcode.JAL: JAL_0,
        Opcode.JR: JR_0,
        Opcode.JALR: JALR_0,
        Opcode.VLW: VLW_0,
        Opcode.VSW: VSW_0,
    }

    for opcode in R_OPCODES:
        table[opcode] = R_0

    for opcode in V_OPCODES:
        table[opcode] = V_0

    return table


def to_signed32(value: int) -> int:
    value &= WORD_MASK

    if value & 0x80000000:
        return value - 0x100000000

    return value


def load_program_from_file(path: str) -> list[int]:
    with open(path, "rb") as file:
        return bytes_to_words(file.read())


def run_program(
    program: list[int],
    input_text: str = "",
    limit: int = 10000,
    memory_size: int = 4096,
) -> tuple[str, str]:
    machine = Machine(program, input_text=input_text, memory_size=memory_size)
    stdout = machine.run(limit=limit)
    log = "\n".join(machine.log)
    return stdout, log


def run_binary_file(
    binary_path: Path,
    input_path: Path | None,
    output_path: Path | None,
    log_path: Path | None,
    limit: int,
    memory_size: int,
    log_limit: int,
) -> None:
    program = load_program_from_file(str(binary_path))

    input_text = ""

    if input_path is not None:
        input_text = input_path.read_text(encoding="utf-8")

    machine = Machine(
        program=program,
        input_text=input_text,
        memory_size=memory_size,
        log_limit=log_limit,
    )

    stdout = machine.run(limit=limit)
    log = "\n".join(machine.log)

    if output_path is None:
        print(stdout, end="")
    else:
        output_path.write_text(stdout, encoding="utf-8")

    if log_path is not None:
        log_path.write_text(log + "\n", encoding="utf-8")


def main() -> None:
    parser = argparse.ArgumentParser(description="Run binary machine code")

    parser.add_argument(
        "binary",
        type=Path,
        help="Path to binary machine code file",
    )

    parser.add_argument(
        "--input",
        type=Path,
        default=None,
        help="Path to input text file",
    )

    parser.add_argument(
        "--output",
        type=Path,
        default=None,
        help="Path to output text file",
    )

    parser.add_argument(
        "--log",
        type=Path,
        default=None,
        help="Path to processor log file",
    )

    parser.add_argument(
        "--limit",
        type=int,
        default=10000,
        help="Maximum number of ticks",
    )
    parser.add_argument(
        "--log-limit",
        type=int,
        default=10000,
        help="Maximum number of log lines to keep",
    )

    parser.add_argument(
        "--memory-size",
        type=int,
        default=4096,
        help="Memory size in machine words",
    )

    args = parser.parse_args()

    run_binary_file(
        binary_path=args.binary,
        input_path=args.input,
        output_path=args.output,
        log_path=args.log,
        limit=args.limit,
        memory_size=args.memory_size,
        log_limit=args.log_limit,
    )


if __name__ == "__main__":
    main()
