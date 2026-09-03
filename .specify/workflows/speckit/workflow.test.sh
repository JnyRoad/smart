#!/usr/bin/env bash

set -euo pipefail

WORKFLOW_FILE="${WORKFLOW_FILE:-$(CDPATH="" cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)/workflow.yml}"

# 验证工作流输入与当前四个命令实际支持的 spec、integration 契约完全一致。
python3 - "$WORKFLOW_FILE" <<'PY'
import pathlib
import sys

import yaml

workflow_path = pathlib.Path(sys.argv[1])
workflow = yaml.safe_load(workflow_path.read_text(encoding="utf-8")) or {}
inputs = workflow.get("inputs") or {}
supported = {"spec", "integration"}
declared = set(inputs)

if declared != supported:
    raise SystemExit(
        "FAIL: workflow inputs must be exactly "
        f"{sorted(supported)}, got {sorted(declared)}"
    )

print("PASS: workflow exposes only supported inputs")
PY
