.PHONY: format lint typecheck test test-fast test-slow test-golden test-golden-fast goldens run-prob1

format:
	ruff format .

lint:
	ruff check .

typecheck:
	mypy isa.py machine.py translator.py golden/golden_test.py golden/update_goldens.py

test:
	pytest -v

test-fast:
	pytest -v -m "not slow"

test-slow:
	pytest -v -m slow

test-golden:
	pytest -v golden/golden_test.py

test-golden-fast:
	pytest -v golden/golden_test.py -m "not slow"

goldens:
	python golden/update_goldens.py

run-prob1:
	python translator.py examples/prob1.fs out/prob1.bin out/prob1.disasm
	python machine.py out/prob1.bin --output out/prob1.txt --log out/prob1.log --limit 100000000 --log-limit 2000
	cat out/prob1.txt
