from __future__ import annotations

from collections.abc import Iterable
from dataclasses import dataclass
from enum import IntEnum

WORD_SIZE_BYTES = 4
WORD_MASK = 0xFFFFFFFF

VECTOR_LENGTH = 4

INPUT_ADDR = 0x3FC0
OUTPUT_ADDR = 0x3FC4


class Opcode(IntEnum):
    HALT = 0x00

    LUI = 0x10
    ADDI = 0x11

    ADD = 0x20
    SUB = 0x21
    MUL = 0x22
    DIV = 0x23
    REM = 0x24

    SEQ = 0x30
    SLT = 0x31
    SGT = 0x32
    SLE = 0x33
    SGE = 0x34
    SLTU = 0x35

    LW = 0x40
    SW = 0x41
    LB = 0x42
    SB = 0x43

    J = 0x50
    BEQZ = 0x51
    BNEZ = 0x52

    JAL = 0x60
    JR = 0x61
    JALR = 0x62

    VLW = 0x70
    VSW = 0x71
    VADD = 0x72
    VSUB = 0x73
    VMUL = 0x74
    VDIV = 0x75
    VEQ = 0x76


REGISTER_CODES: dict[str, int] = {
    "zero": 0x0,
    "ra": 0x1,
    "sp": 0x2,
    "rp": 0x3,
    "t0": 0x4,
    "t1": 0x5,
    "t2": 0x6,
    "t3": 0x7,
    "t4": 0x8,
    "t5": 0x9,
    "t6": 0xA,
}

CODE_TO_REGISTER: dict[int, str] = {code: name for name, code in REGISTER_CODES.items()}


VECTOR_REGISTER_CODES: dict[str, int] = {
    "v0": 0x0,
    "v1": 0x1,
    "v2": 0x2,
    "v3": 0x3,
}

CODE_TO_VECTOR_REGISTER: dict[int, str] = {
    code: name for name, code in VECTOR_REGISTER_CODES.items()
}


R_FORMAT = {
    "add",
    "sub",
    "mul",
    "div",
    "rem",
    "seq",
    "slt",
    "sgt",
    "sle",
    "sge",
    "sltu",
}

V_FORMAT = {
    "vadd",
    "vsub",
    "vmul",
    "vdiv",
    "veq",
}


MNEMONIC_TO_OPCODE: dict[str, Opcode] = {
    "halt": Opcode.HALT,
    "lui": Opcode.LUI,
    "addi": Opcode.ADDI,
    "add": Opcode.ADD,
    "sub": Opcode.SUB,
    "mul": Opcode.MUL,
    "div": Opcode.DIV,
    "rem": Opcode.REM,
    "seq": Opcode.SEQ,
    "slt": Opcode.SLT,
    "sgt": Opcode.SGT,
    "sle": Opcode.SLE,
    "sge": Opcode.SGE,
    "sltu": Opcode.SLTU,
    "lw": Opcode.LW,
    "sw": Opcode.SW,
    "lb": Opcode.LB,
    "sb": Opcode.SB,
    "j": Opcode.J,
    "beqz": Opcode.BEQZ,
    "bnez": Opcode.BNEZ,
    "jal": Opcode.JAL,
    "jr": Opcode.JR,
    "jalr": Opcode.JALR,
    "vlw": Opcode.VLW,
    "vsw": Opcode.VSW,
    "vadd": Opcode.VADD,
    "vsub": Opcode.VSUB,
    "vmul": Opcode.VMUL,
    "vdiv": Opcode.VDIV,
    "veq": Opcode.VEQ,
}

OPCODE_TO_MNEMONIC: dict[Opcode, str] = {
    opcode: mnemonic for mnemonic, opcode in MNEMONIC_TO_OPCODE.items()
}


@dataclass(frozen=True)
class Instruction:
    mnemonic: str
    operands: tuple[str | int, ...] = ()

    def encode(self) -> int:
        return encode_instruction(self)


def encode_instruction(instruction: Instruction) -> int:
    mnemonic = instruction.mnemonic.lower()
    operands = instruction.operands

    if mnemonic not in MNEMONIC_TO_OPCODE:
        raise ValueError(f"Unknown instruction: {mnemonic}")

    opcode = MNEMONIC_TO_OPCODE[mnemonic]

    if mnemonic == "halt":
        require_operand_count(mnemonic, operands, 0)
        return int(opcode) << 24

    if mnemonic == "lui":
        require_operand_count(mnemonic, operands, 2)
        rd = register_code(operands[0])
        imm20 = unsigned_immediate(operands[1], bits=20)
        return ((int(opcode) << 24) | (rd << 20) | imm20) & WORD_MASK

    if mnemonic == "addi":
        require_operand_count(mnemonic, operands, 3)
        rd = register_code(operands[0])
        rs1 = register_code(operands[1])
        imm16 = signed_immediate(operands[2], bits=16)
        return ((int(opcode) << 24) | (rd << 20) | (rs1 << 16) | imm16) & WORD_MASK

    if mnemonic in R_FORMAT:
        require_operand_count(mnemonic, operands, 3)
        rd = register_code(operands[0])
        rs1 = register_code(operands[1])
        rs2 = register_code(operands[2])
        return ((int(opcode) << 24) | (rd << 20) | (rs1 << 16) | (rs2 << 12)) & WORD_MASK

    if mnemonic in {"lw", "lb"}:
        require_operand_count(mnemonic, operands, 3)
        rd = register_code(operands[0])
        offset = signed_immediate(operands[1], bits=16)
        rs1 = register_code(operands[2])
        return ((int(opcode) << 24) | (rd << 20) | (rs1 << 16) | offset) & WORD_MASK

    if mnemonic in {"sw", "sb"}:
        require_operand_count(mnemonic, operands, 3)
        rs2 = register_code(operands[0])
        offset = signed_immediate(operands[1], bits=16)
        rs1 = register_code(operands[2])
        return ((int(opcode) << 24) | (rs2 << 20) | (rs1 << 16) | offset) & WORD_MASK

    if mnemonic == "j":
        require_operand_count(mnemonic, operands, 1)
        offset24 = signed_immediate(operands[0], bits=24)
        return ((int(opcode) << 24) | offset24) & WORD_MASK

    if mnemonic in {"beqz", "bnez"}:
        require_operand_count(mnemonic, operands, 2)
        rs = register_code(operands[0])
        offset20 = signed_immediate(operands[1], bits=20)
        return ((int(opcode) << 24) | (rs << 20) | offset20) & WORD_MASK

    if mnemonic == "jal":
        require_operand_count(mnemonic, operands, 2)
        rd = register_code(operands[0])
        offset20 = signed_immediate(operands[1], bits=20)
        return ((int(opcode) << 24) | (rd << 20) | offset20) & WORD_MASK

    if mnemonic == "jr":
        require_operand_count(mnemonic, operands, 1)
        rs = register_code(operands[0])
        return ((int(opcode) << 24) | (rs << 20)) & WORD_MASK

    if mnemonic == "jalr":
        require_operand_count(mnemonic, operands, 2)
        rd = register_code(operands[0])
        rs = register_code(operands[1])
        return ((int(opcode) << 24) | (rd << 20) | (rs << 16)) & WORD_MASK

    if mnemonic in V_FORMAT:
        require_operand_count(mnemonic, operands, 3)
        vd = vector_register_code(operands[0])
        vs1 = vector_register_code(operands[1])
        vs2 = vector_register_code(operands[2])
        return ((int(opcode) << 24) | (vd << 20) | (vs1 << 16) | (vs2 << 12)) & WORD_MASK

    if mnemonic == "vlw":
        require_operand_count(mnemonic, operands, 3)
        vd = vector_register_code(operands[0])
        offset = signed_immediate(operands[1], bits=16)
        rs1 = register_code(operands[2])
        return ((int(opcode) << 24) | (vd << 20) | (rs1 << 16) | offset) & WORD_MASK

    if mnemonic == "vsw":
        require_operand_count(mnemonic, operands, 3)
        vs = vector_register_code(operands[0])
        offset = signed_immediate(operands[1], bits=16)
        rs1 = register_code(operands[2])
        return ((int(opcode) << 24) | (vs << 20) | (rs1 << 16) | offset) & WORD_MASK

    raise ValueError(f"Encoding is not implemented for instruction: {mnemonic}")


def decode_instruction(word: int) -> Instruction:
    word &= WORD_MASK

    opcode_value = (word >> 24) & 0xFF

    try:
        opcode = Opcode(opcode_value)
    except ValueError as error:
        raise ValueError(f"Unknown opcode: 0x{opcode_value:02X}") from error

    mnemonic = OPCODE_TO_MNEMONIC[opcode]

    if mnemonic == "halt":
        return Instruction("halt")

    if mnemonic == "lui":
        rd = (word >> 20) & 0xF
        imm20 = word & 0xFFFFF
        return Instruction("lui", (register_name(rd), imm20))

    if mnemonic == "addi":
        rd = (word >> 20) & 0xF
        rs1 = (word >> 16) & 0xF
        imm16 = sign_extend(word & 0xFFFF, bits=16)
        return Instruction("addi", (register_name(rd), register_name(rs1), imm16))

    if mnemonic in R_FORMAT:
        rd = (word >> 20) & 0xF
        rs1 = (word >> 16) & 0xF
        rs2 = (word >> 12) & 0xF
        return Instruction(
            mnemonic,
            (register_name(rd), register_name(rs1), register_name(rs2)),
        )

    if mnemonic in {"lw", "lb"}:
        rd = (word >> 20) & 0xF
        rs1 = (word >> 16) & 0xF
        offset = sign_extend(word & 0xFFFF, bits=16)
        return Instruction(mnemonic, (register_name(rd), offset, register_name(rs1)))

    if mnemonic in {"sw", "sb"}:
        rs2 = (word >> 20) & 0xF
        rs1 = (word >> 16) & 0xF
        offset = sign_extend(word & 0xFFFF, bits=16)
        return Instruction(mnemonic, (register_name(rs2), offset, register_name(rs1)))

    if mnemonic == "j":
        offset = sign_extend(word & 0xFFFFFF, bits=24)
        return Instruction("j", (offset,))

    if mnemonic in {"beqz", "bnez"}:
        rs = (word >> 20) & 0xF
        offset = sign_extend(word & 0xFFFFF, bits=20)
        return Instruction(mnemonic, (register_name(rs), offset))

    if mnemonic == "jal":
        rd = (word >> 20) & 0xF
        offset = sign_extend(word & 0xFFFFF, bits=20)
        return Instruction("jal", (register_name(rd), offset))

    if mnemonic == "jr":
        rs = (word >> 20) & 0xF
        return Instruction("jr", (register_name(rs),))

    if mnemonic == "jalr":
        rd = (word >> 20) & 0xF
        rs = (word >> 16) & 0xF
        return Instruction("jalr", (register_name(rd), register_name(rs)))

    if mnemonic in V_FORMAT:
        vd = (word >> 20) & 0xF
        vs1 = (word >> 16) & 0xF
        vs2 = (word >> 12) & 0xF
        return Instruction(
            mnemonic,
            (
                vector_register_name(vd),
                vector_register_name(vs1),
                vector_register_name(vs2),
            ),
        )

    if mnemonic == "vlw":
        vd = (word >> 20) & 0xF
        rs1 = (word >> 16) & 0xF
        offset = sign_extend(word & 0xFFFF, bits=16)
        return Instruction("vlw", (vector_register_name(vd), offset, register_name(rs1)))

    if mnemonic == "vsw":
        vs = (word >> 20) & 0xF
        rs1 = (word >> 16) & 0xF
        offset = sign_extend(word & 0xFFFF, bits=16)
        return Instruction("vsw", (vector_register_name(vs), offset, register_name(rs1)))

    raise ValueError(f"Decoding is not implemented for opcode: {opcode}")


def disassemble_instruction(instruction: Instruction) -> str:
    mnemonic = instruction.mnemonic
    operands = instruction.operands

    if mnemonic == "halt":
        return "halt"

    if mnemonic in R_FORMAT:
        rd, rs1, rs2 = operands
        return f"{mnemonic} {rd}, {rs1}, {rs2}"

    if mnemonic in V_FORMAT:
        vd, vs1, vs2 = operands
        return f"{mnemonic} {vd}, {vs1}, {vs2}"

    if mnemonic == "lui":
        rd, imm20 = operands
        return f"lui {rd}, {format_number(imm20)}"

    if mnemonic == "addi":
        rd, rs1, imm16 = operands
        return f"addi {rd}, {rs1}, {format_number(imm16)}"

    if mnemonic in {"lw", "lb"}:
        rd, offset, rs1 = operands
        return f"{mnemonic} {rd}, {format_number(offset)}({rs1})"

    if mnemonic in {"sw", "sb"}:
        rs2, offset, rs1 = operands
        return f"{mnemonic} {rs2}, {format_number(offset)}({rs1})"

    if mnemonic == "j":
        (offset,) = operands
        return f"j {format_number(offset)}"

    if mnemonic in {"beqz", "bnez"}:
        rs, offset = operands
        return f"{mnemonic} {rs}, {format_number(offset)}"

    if mnemonic == "jal":
        rd, offset = operands
        return f"jal {rd}, {format_number(offset)}"

    if mnemonic == "jr":
        (rs,) = operands
        return f"jr {rs}"

    if mnemonic == "jalr":
        rd, rs = operands
        return f"jalr {rd}, {rs}"

    if mnemonic == "vlw":
        vd, offset, rs1 = operands
        return f"vlw {vd}, {format_number(offset)}({rs1})"

    if mnemonic == "vsw":
        vs, offset, rs1 = operands
        return f"vsw {vs}, {format_number(offset)}({rs1})"

    raise ValueError(f"Cannot disassemble instruction: {instruction}")


def disassemble_word(address: int, word: int) -> str:
    instruction = decode_instruction(word)
    text = disassemble_instruction(instruction)
    return f"{address:04X} - {word & WORD_MASK:08X} - {text}"


def disassemble_program(words: Iterable[int]) -> str:
    lines = []

    for index, word in enumerate(words):
        address = index * WORD_SIZE_BYTES
        lines.append(disassemble_word(address, word))

    return "\n".join(lines)


def encode_program(instructions: Iterable[Instruction]) -> list[int]:
    return [instruction.encode() for instruction in instructions]


def words_to_bytes(words: Iterable[int]) -> bytes:
    result = bytearray()

    for word in words:
        result.extend(word_to_bytes(word))

    return bytes(result)


def bytes_to_words(data: bytes) -> list[int]:
    if len(data) % WORD_SIZE_BYTES != 0:
        raise ValueError("Binary code size must be divisible by 4 bytes")

    result = []

    for i in range(0, len(data), WORD_SIZE_BYTES):
        result.append(bytes_to_word(data[i : i + WORD_SIZE_BYTES]))

    return result


def word_to_bytes(word: int) -> bytes:
    return (word & WORD_MASK).to_bytes(WORD_SIZE_BYTES, byteorder="big", signed=False)


def bytes_to_word(data: bytes) -> int:
    if len(data) != WORD_SIZE_BYTES:
        raise ValueError("One machine word must contain exactly 4 bytes")

    return int.from_bytes(data, byteorder="big", signed=False)


def register_code(value: str | int) -> int:
    if isinstance(value, int):
        if not 0 <= value <= 0xF:
            raise ValueError(f"Register code is out of range: {value}")
        return value

    if value not in REGISTER_CODES:
        raise ValueError(f"Unknown register: {value}")

    return REGISTER_CODES[value]


def vector_register_code(value: str | int) -> int:
    if isinstance(value, int):
        if not 0 <= value <= 0xF:
            raise ValueError(f"Vector register code is out of range: {value}")
        return value

    if value not in VECTOR_REGISTER_CODES:
        raise ValueError(f"Unknown vector register: {value}")

    return VECTOR_REGISTER_CODES[value]


def register_name(code: int) -> str:
    if code not in CODE_TO_REGISTER:
        raise ValueError(f"Unknown register code: 0x{code:X}")

    return CODE_TO_REGISTER[code]


def vector_register_name(code: int) -> str:
    if code not in CODE_TO_VECTOR_REGISTER:
        raise ValueError(f"Unknown vector register code: 0x{code:X}")

    return CODE_TO_VECTOR_REGISTER[code]


def signed_immediate(value: str | int, bits: int) -> int:
    number = parse_number(value)

    min_value = -(1 << (bits - 1))
    max_value = (1 << (bits - 1)) - 1

    if not min_value <= number <= max_value:
        raise ValueError(
            f"Signed immediate {number} does not fit into {bits} bits ({min_value}..{max_value})"
        )

    return number & ((1 << bits) - 1)


def unsigned_immediate(value: str | int, bits: int) -> int:
    number = parse_number(value)

    min_value = 0
    max_value = (1 << bits) - 1

    if not min_value <= number <= max_value:
        raise ValueError(
            f"Unsigned immediate {number} does not fit into {bits} bits ({min_value}..{max_value})"
        )

    return number


def sign_extend(value: int, bits: int) -> int:
    sign_bit = 1 << (bits - 1)
    mask = (1 << bits) - 1

    value &= mask

    if value & sign_bit:
        return value - (1 << bits)

    return value


def parse_number(value: str | int) -> int:
    if isinstance(value, int):
        return value

    value = value.strip()

    if value.startswith("-0x"):
        return -int(value[3:], 16)

    if value.startswith("0x"):
        return int(value[2:], 16)

    return int(value, 10)


def format_number(value: str | int) -> str:
    if isinstance(value, str):
        return value

    if value < 0:
        return str(value)

    if value >= 10:
        return f"0x{value:X}"

    return str(value)


def require_operand_count(
    mnemonic: str,
    operands: tuple[str | int, ...],
    expected_count: int,
) -> None:
    if len(operands) != expected_count:
        raise ValueError(
            f"Instruction {mnemonic} expects {expected_count} operands, got {len(operands)}"
        )
