from __future__ import annotations

from pathlib import Path

import pytest

from machine import Machine
from translator import translate_source

LAB4_DIR = Path(__file__).resolve().parents[1]

EXAMPLES_DIR = LAB4_DIR / "examples"


def run_forth_example(filename: str, input_text: str = "", limit: int = 100000) -> str:
    source = (EXAMPLES_DIR / filename).read_text(encoding="utf-8")
    result = translate_source(source)

    machine = Machine(
        program=result.memory_image,
        input_text=input_text,
        log_limit=0,
    )

    return machine.run(limit=limit)


def test_hello_char() -> None:
    assert run_forth_example("hello_char.fth") == "A"


def test_cat() -> None:
    assert run_forth_example("cat.fth", input_text="abc\n") == "abc\n"


def test_double_precision() -> None:
    assert run_forth_example("double_precision.fth") == "2 0\n"


def test_print_int() -> None:
    assert run_forth_example("print_int.fth", limit=300000) == "0\n123\n-1\n906609"


@pytest.mark.slow
def test_prob1() -> None:
    assert run_forth_example("prob1.fth", limit=100000000) == "906609\n"
