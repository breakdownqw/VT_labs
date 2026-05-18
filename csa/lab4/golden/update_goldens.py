from __future__ import annotations

import sys
from dataclasses import dataclass
from pathlib import Path

GOLDEN_DIR = Path(__file__).resolve().parent
LAB4_DIR = GOLDEN_DIR.parent
sys.path.insert(0, str(LAB4_DIR))


@dataclass(frozen=True)
class GoldenCase:
    name: str
    source: Path
    stdin: str
    limit: int
    log_limit: int


def main() -> None:
    from golden_test import update_golden

    GOLDEN_DIR.mkdir(exist_ok=True)

    cases = [
        GoldenCase("hello_char", LAB4_DIR / "examples" / "hello_char.fs", "", 100000, 500),
        GoldenCase("hello_pstr", LAB4_DIR / "examples" / "hello_pstr.fs", "", 100000, 800),
        GoldenCase(
            "hello_user_name",
            LAB4_DIR / "examples" / "hello_user_name.fs",
            (LAB4_DIR / "examples" / "hello_user_name_input.txt").read_text(encoding="utf-8"),
            100000,
            1200,
        ),
        GoldenCase("cat", LAB4_DIR / "examples" / "cat.fs", "abc\n", 100000, 1000),
        GoldenCase(
            "execution_token",
            LAB4_DIR / "examples" / "execution_token.fs",
            "",
            100000,
            1000,
        ),
        GoldenCase("vector_demo", LAB4_DIR / "examples" / "vector_demo.fs", "", 300000, 1500),
        GoldenCase("vector_ops", LAB4_DIR / "examples" / "vector_ops.fs", "", 300000, 1500),
        GoldenCase(
            "vector_scalar_add",
            LAB4_DIR / "examples" / "vector_scalar_add.fs",
            "",
            300000,
            1500,
        ),
        GoldenCase(
            "vector_vector_add",
            LAB4_DIR / "examples" / "vector_vector_add.fs",
            "",
            300000,
            1500,
        ),
        GoldenCase(
            "sort",
            LAB4_DIR / "examples" / "sort.fs",
            (LAB4_DIR / "examples" / "sort_input.txt").read_text(encoding="utf-8"),
            300000,
            1500,
        ),
        GoldenCase(
            "double_precision",
            LAB4_DIR / "examples" / "double_precision.fs",
            "",
            100000,
            1500,
        ),
        GoldenCase("print_int", LAB4_DIR / "examples" / "print_int.fs", "", 300000, 1500),
        GoldenCase("prob1", LAB4_DIR / "examples" / "prob1.fs", "", 100000000, 2000),
    ]

    for case in cases:
        print(f"Updating golden/{case.name}.yml")

        update_golden(
            case_name=case.name,
            source_path=case.source,
            stdin=case.stdin,
            limit=case.limit,
            log_limit=case.log_limit,
        )


if __name__ == "__main__":
    main()
