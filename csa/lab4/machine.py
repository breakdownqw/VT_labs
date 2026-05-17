from __future__ import annotations

import argparse
from collections.abc import Callable
from dataclasses import dataclass
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
    action: Callable[[Machine], int | None]


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
        next_upc = micro_instruction.action(self)
        after = self.short_state()

        if self.log_limit > 0 and len(self.log) < self.log_limit:
            self.log.append(
                f"TICK {self.tick_count:06d} | "
                f"uPC={self.upc:03X} | "
                f"{micro_instruction.name:<10} | "
                f"{before} -> {after}"
            )
        elif 0 < self.log_limit == len(self.log):
            self.log.append(f"... log truncated after {self.log_limit} ticks ...")

        if next_upc is not None:
            self.upc = next_upc

        self.registers["zero"] = 0
        self.tick_count += 1

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
        FETCH_0: MicroInstruction("FETCH_0", fetch_0),
        FETCH_1: MicroInstruction("FETCH_1", fetch_1),
        FETCH_2: MicroInstruction("FETCH_2", fetch_2),
        DECODE: MicroInstruction("DECODE", decode),
        HALT_0: MicroInstruction("HALT_0", halt_0),
        LUI_0: MicroInstruction("LUI_0", lui_0),
        LUI_1: MicroInstruction("LUI_1", lui_1),
        ADDI_0: MicroInstruction("ADDI_0", addi_0),
        ADDI_1: MicroInstruction("ADDI_1", addi_1),
        ADDI_2: MicroInstruction("ADDI_2", addi_2),
        ADDI_3: MicroInstruction("ADDI_3", addi_3),
        R_0: MicroInstruction("R_0", r_0),
        R_1: MicroInstruction("R_1", r_1),
        R_2: MicroInstruction("R_2", r_2),
        R_3: MicroInstruction("R_3", r_3),
        LW_0: MicroInstruction("LW_0", lw_0),
        LW_1: MicroInstruction("LW_1", lw_1),
        LW_2: MicroInstruction("LW_2", lw_2),
        LW_3: MicroInstruction("LW_3", lw_3),
        LW_4: MicroInstruction("LW_4", lw_4),
        SW_0: MicroInstruction("SW_0", sw_0),
        SW_1: MicroInstruction("SW_1", sw_1),
        SW_2: MicroInstruction("SW_2", sw_2),
        SW_3: MicroInstruction("SW_3", sw_3),
        SW_4: MicroInstruction("SW_4", sw_4),
        J_0: MicroInstruction("J_0", j_0),
        J_1: MicroInstruction("J_1", j_1),
        J_2: MicroInstruction("J_2", j_2),
        BEQZ_0: MicroInstruction("BEQZ_0", beqz_0),
        BEQZ_1: MicroInstruction("BEQZ_1", beqz_1),
        BEQZ_2: MicroInstruction("BEQZ_2", beqz_2),
        BNEZ_0: MicroInstruction("BNEZ_0", bnez_0),
        BNEZ_1: MicroInstruction("BNEZ_1", bnez_1),
        BNEZ_2: MicroInstruction("BNEZ_2", bnez_2),
        JAL_0: MicroInstruction("JAL_0", jal_0),
        JAL_1: MicroInstruction("JAL_1", jal_1),
        JAL_2: MicroInstruction("JAL_2", jal_2),
        JAL_3: MicroInstruction("JAL_3", jal_3),
        JR_0: MicroInstruction("JR_0", jr_0),
        JR_1: MicroInstruction("JR_1", jr_1),
        JALR_0: MicroInstruction("JALR_0", jalr_0),
        JALR_1: MicroInstruction("JALR_1", jalr_1),
        JALR_2: MicroInstruction("JALR_2", jalr_2),
        VLW_0: MicroInstruction("VLW_0", vlw_0),
        VLW_1: MicroInstruction("VLW_1", vlw_1),
        VLW_2: MicroInstruction("VLW_2", vlw_2),
        VLW_3: MicroInstruction("VLW_3", vlw_3),
        VLW_4: MicroInstruction("VLW_4", vlw_4),
        VLW_5: MicroInstruction("VLW_5", vlw_5),
        VLW_6: MicroInstruction("VLW_6", vlw_6),
        VSW_0: MicroInstruction("VSW_0", vsw_0),
        VSW_1: MicroInstruction("VSW_1", vsw_1),
        VSW_2: MicroInstruction("VSW_2", vsw_2),
        VSW_3: MicroInstruction("VSW_3", vsw_3),
        VSW_4: MicroInstruction("VSW_4", vsw_4),
        VSW_5: MicroInstruction("VSW_5", vsw_5),
        VSW_6: MicroInstruction("VSW_6", vsw_6),
        V_0: MicroInstruction("V_0", v_0),
        V_1: MicroInstruction("V_1", v_1),
    }


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


def fetch_0(machine: Machine) -> int:
    machine.mar = machine.pc & WORD_MASK
    return FETCH_1


def fetch_1(machine: Machine) -> int:
    machine.mdr = machine.read_memory(machine.mar)
    return FETCH_2


def fetch_2(machine: Machine) -> int:
    machine.ir = machine.mdr & WORD_MASK
    machine.pc = (machine.pc + 1) & WORD_MASK
    return DECODE


def decode(machine: Machine) -> int:
    opcode = machine.opcode()

    if opcode not in machine.dispatch_table:
        raise RuntimeError(f"No microprogram for opcode: {opcode}")

    return machine.dispatch_table[opcode]


def halt_0(machine: Machine) -> None:
    machine.halted = True
    return None


def lui_0(machine: Machine) -> int:
    machine.alu = (machine.imm20() << 12) & WORD_MASK
    return LUI_1


def lui_1(machine: Machine) -> int:
    machine.set_register_by_code(machine.rd(), machine.alu)
    return FETCH_0


def addi_0(machine: Machine) -> int:
    machine.a = machine.get_register_by_code(machine.rs1())
    return ADDI_1


def addi_1(machine: Machine) -> int:
    machine.b = machine.imm16()
    return ADDI_2


def addi_2(machine: Machine) -> int:
    machine.alu = (machine.a + machine.b) & WORD_MASK
    return ADDI_3


def addi_3(machine: Machine) -> int:
    machine.set_register_by_code(machine.rd(), machine.alu)
    return FETCH_0


def r_0(machine: Machine) -> int:
    machine.a = machine.get_register_by_code(machine.rs1())
    return R_1


def r_1(machine: Machine) -> int:
    machine.b = machine.get_register_by_code(machine.rs2())
    return R_2


def r_2(machine: Machine) -> int:
    machine.alu = machine.run_r_operation() & WORD_MASK
    return R_3


def r_3(machine: Machine) -> int:
    machine.set_register_by_code(machine.rd(), machine.alu)
    return FETCH_0


def lw_0(machine: Machine) -> int:
    machine.a = machine.get_register_by_code(machine.rs1())
    return LW_1


def lw_1(machine: Machine) -> int:
    machine.b = machine.imm16()
    return LW_2


def lw_2(machine: Machine) -> int:
    machine.mar = (machine.a + machine.b) & WORD_MASK
    return LW_3


def lw_3(machine: Machine) -> int:
    machine.mdr = machine.read_memory(machine.mar)
    return LW_4


def lw_4(machine: Machine) -> int:
    machine.set_register_by_code(machine.rd(), machine.mdr)
    return FETCH_0


def sw_0(machine: Machine) -> int:
    machine.a = machine.get_register_by_code(machine.rs1())
    return SW_1


def sw_1(machine: Machine) -> int:
    machine.b = machine.imm16()
    return SW_2


def sw_2(machine: Machine) -> int:
    machine.mar = (machine.a + machine.b) & WORD_MASK
    return SW_3


def sw_3(machine: Machine) -> int:
    machine.mdr = machine.get_register_by_code(machine.rd())
    return SW_4


def sw_4(machine: Machine) -> int:
    machine.write_memory(machine.mar, machine.mdr)
    return FETCH_0


def j_0(machine: Machine) -> int:
    machine.a = machine.pc
    return J_1


def j_1(machine: Machine) -> int:
    machine.b = machine.offset24()
    return J_2


def j_2(machine: Machine) -> int:
    machine.pc = (machine.a + machine.b) & WORD_MASK
    return FETCH_0


def beqz_0(machine: Machine) -> int:
    machine.a = machine.get_register_by_code(machine.rd())
    return BEQZ_1


def beqz_1(machine: Machine) -> int:
    if machine.a == 0:
        machine.pc = (machine.pc + machine.offset20()) & WORD_MASK

    return BEQZ_2


def beqz_2(machine: Machine) -> int:
    return FETCH_0


def bnez_0(machine: Machine) -> int:
    machine.a = machine.get_register_by_code(machine.rd())
    return BNEZ_1


def bnez_1(machine: Machine) -> int:
    if machine.a != 0:
        machine.pc = (machine.pc + machine.offset20()) & WORD_MASK

    return BNEZ_2


def bnez_2(machine: Machine) -> int:
    return FETCH_0


def jal_0(machine: Machine) -> int:
    machine.set_register_by_code(machine.rd(), machine.pc)
    return JAL_1


def jal_1(machine: Machine) -> int:
    machine.a = machine.pc
    return JAL_2


def jal_2(machine: Machine) -> int:
    machine.b = machine.offset20()
    return JAL_3


def jal_3(machine: Machine) -> int:
    machine.pc = (machine.a + machine.b) & WORD_MASK
    return FETCH_0


def jr_0(machine: Machine) -> int:
    machine.a = machine.get_register_by_code(machine.rd())
    return JR_1


def jr_1(machine: Machine) -> int:
    machine.pc = machine.a & WORD_MASK
    return FETCH_0


def jalr_0(machine: Machine) -> int:
    machine.a = machine.get_register_by_code(machine.rs1())
    return JALR_1


def jalr_1(machine: Machine) -> int:
    machine.set_register_by_code(machine.rd(), machine.pc)
    return JALR_2


def jalr_2(machine: Machine) -> int:
    machine.pc = machine.a & WORD_MASK
    return FETCH_0


def vlw_0(machine: Machine) -> int:
    machine.a = machine.get_register_by_code(machine.rs1())
    return VLW_1


def vlw_1(machine: Machine) -> int:
    machine.b = machine.imm16()
    return VLW_2


def vlw_2(machine: Machine) -> int:
    machine.mar = (machine.a + machine.b) & WORD_MASK
    return VLW_3


def vlw_3(machine: Machine) -> int:
    values = machine.get_vector_register_by_code(machine.rd()).copy()
    values[0] = machine.read_memory(machine.mar)
    machine.set_vector_register_by_code(machine.rd(), values)
    return VLW_4


def vlw_4(machine: Machine) -> int:
    values = machine.get_vector_register_by_code(machine.rd()).copy()
    values[1] = machine.read_memory(machine.mar + 1)
    machine.set_vector_register_by_code(machine.rd(), values)
    return VLW_5


def vlw_5(machine: Machine) -> int:
    values = machine.get_vector_register_by_code(machine.rd()).copy()
    values[2] = machine.read_memory(machine.mar + 2)
    machine.set_vector_register_by_code(machine.rd(), values)
    return VLW_6


def vlw_6(machine: Machine) -> int:
    values = machine.get_vector_register_by_code(machine.rd()).copy()
    values[3] = machine.read_memory(machine.mar + 3)
    machine.set_vector_register_by_code(machine.rd(), values)
    return FETCH_0


def vsw_0(machine: Machine) -> int:
    machine.a = machine.get_register_by_code(machine.rs1())
    return VSW_1


def vsw_1(machine: Machine) -> int:
    machine.b = machine.imm16()
    return VSW_2


def vsw_2(machine: Machine) -> int:
    machine.mar = (machine.a + machine.b) & WORD_MASK
    return VSW_3


def vsw_3(machine: Machine) -> int:
    values = machine.get_vector_register_by_code(machine.rd())
    machine.write_memory(machine.mar, values[0])
    return VSW_4


def vsw_4(machine: Machine) -> int:
    values = machine.get_vector_register_by_code(machine.rd())
    machine.write_memory(machine.mar + 1, values[1])
    return VSW_5


def vsw_5(machine: Machine) -> int:
    values = machine.get_vector_register_by_code(machine.rd())
    machine.write_memory(machine.mar + 2, values[2])
    return VSW_6


def vsw_6(machine: Machine) -> int:
    values = machine.get_vector_register_by_code(machine.rd())
    machine.write_memory(machine.mar + 3, values[3])
    return FETCH_0


def v_0(machine: Machine) -> int:
    machine.set_vector_register_by_code(machine.rd(), machine.run_v_operation())
    return V_1


def v_1(machine: Machine) -> int:
    return FETCH_0


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
