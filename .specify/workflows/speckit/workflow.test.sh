#!/usr/bin/env bash

set -euo pipefail

WORKFLOW_FILE="${WORKFLOW_FILE:-$(CDPATH="" cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)/workflow.yml}"

# 此契约测试只读取顶层 inputs 映射，使用 Python 标准库并禁用 site-packages，避免把 PyYAML
# 作为干净环境的隐式前置依赖。
python3 -S - "$WORKFLOW_FILE" <<'PY'
import pathlib
import re
import sys

workflow_path = pathlib.Path(sys.argv[1])
lines = workflow_path.read_text(encoding="utf-8").splitlines()
try:
    inputs_start = next(index for index, line in enumerate(lines) if line == "inputs:")
except StopIteration:
    raise SystemExit("FAIL: workflow is missing the top-level inputs mapping")

inputs = set()
for line in lines[inputs_start + 1:]:
    if line and not line.startswith((" ", "\t")):
        break
    match = re.fullmatch(r"  ([A-Za-z][A-Za-z0-9_-]*):", line)
    if match:
        inputs.add(match.group(1))

supported = {"spec", "integration"}
declared = inputs

if declared != supported:
    raise SystemExit(
        "FAIL: workflow inputs must be exactly "
        f"{sorted(supported)}, got {sorted(declared)}"
    )

print("PASS: workflow exposes only supported inputs")
PY
