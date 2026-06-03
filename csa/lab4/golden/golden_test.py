from __future__ import annotations

from pathlib import Path
from typing import Any

import pytest
import yaml

from machine import Machine
from translator import translate_source

GOLDEN_DIR = Path(__file__).resolve().parent
LAB4_DIR = GOLDEN_DIR.parent


class LiteralString(str):
    pass


def represent_literal_string(dumper: Any, data: LiteralString) -> Any:
    return dumper.represent_scalar("tag:yaml.org,2002:str", data, style="|")


yaml.add_representer(LiteralString, represent_literal_string, Dumper=yaml.SafeDumper)


CASES = [
    "hello_char",
    "hello_pstr",
    "hello_user_name",
    "cat",
    "execution_token",
    "vector_demo",
    "vector_ops",
    "vector_scalar_add",
    "vector_vector_add",
    "sort",
    "double_precision",
    "print_int",
]


SLOW_CASES = [
    "prob1",
]


def run_case(source: str, stdin: str, limit: int, log_limit: int) -> dict[str, str | int]:
    result = translate_source(source)

    machine = Machine(
        program=result.memory_image,
        input_text=stdin,
        log_limit=log_limit,
    )

    stdout = machine.run(limit=limit)
    log = "\n".join(machine.log)

    return {
        "out_disasm": result.disasm,
        "out_stdout": stdout,
        "out_log": log,
        "out_ticks": machine.tick_count,
    }


def load_golden(case_name: str) -> dict[str, str]:
    path = GOLDEN_DIR / f"{case_name}.yml"

    with path.open("r", encoding="utf-8") as file:
        data = yaml.safe_load(file)

    if not isinstance(data, dict):
        raise ValueError(f"Golden file must contain a mapping: {path}")

    return data


@pytest.mark.parametrize("case_name", CASES)
def test_golden(case_name: str) -> None:
    golden = load_golden(case_name)

    source = golden["in_source"]
    stdin = golden.get("in_stdin", "")
    limit = int(golden.get("limit", 100000))
    log_limit = int(golden.get("log_limit", 2000))

    actual = run_case(source, stdin, limit, log_limit)

    assert actual["out_disasm"] == golden["out_disasm"]
    assert actual["out_stdout"] == golden["out_stdout"]
    assert actual["out_log"] == golden["out_log"]
    assert actual["out_ticks"] == golden["out_ticks"]


@pytest.mark.slow
@pytest.mark.parametrize("case_name", SLOW_CASES)
def test_golden_slow(case_name: str) -> None:
    golden = load_golden(case_name)

    source = golden["in_source"]
    stdin = golden.get("in_stdin", "")
    limit = int(golden.get("limit", 100000000))
    log_limit = int(golden.get("log_limit", 2000))

    actual = run_case(source, stdin, limit, log_limit)

    assert actual["out_disasm"] == golden["out_disasm"]
    assert actual["out_stdout"] == golden["out_stdout"]
    assert actual["out_log"] == golden["out_log"]
    assert actual["out_ticks"] == golden["out_ticks"]


def update_golden(
    case_name: str, source_path: Path, stdin: str, limit: int, log_limit: int
) -> None:
    source = source_path.read_text(encoding="utf-8")
    actual = run_case(source, stdin, limit, log_limit)

    data: dict[str, Any] = {
        "in_source": source,
        "in_stdin": stdin,
        "limit": limit,
        "log_limit": log_limit,
        "out_disasm": actual["out_disasm"],
        "out_stdout": actual["out_stdout"],
        "out_log": actual["out_log"],
        "out_ticks": actual["out_ticks"],
    }
    data = {key: yaml_value(value) for key, value in data.items()}

    output_path = GOLDEN_DIR / f"{case_name}.yml"
    temp_path = output_path.with_suffix(output_path.suffix + ".tmp")

    with temp_path.open("w", encoding="utf-8") as file:
        yaml.dump(
            data,
            file,
            Dumper=yaml.SafeDumper,
            allow_unicode=True,
            sort_keys=False,
            width=120,
        )

    temp_path.replace(output_path)


def yaml_value(value: Any) -> Any:
    if isinstance(value, str) and "\n" in value:
        return LiteralString(value)

    return value


def test_placeholder() -> None:
    assert True
