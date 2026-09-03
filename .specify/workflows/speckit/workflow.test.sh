#!/usr/bin/env bash

set -euo pipefail

WORKFLOW_FILE="$(CDPATH="" cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)/workflow.yml"

# 验证工作流不会声明尚未被任何步骤消费的 scope 输入。
python3 - "$WORKFLOW_FILE" <<'PY'
import pathlib
import sys

import yaml

workflow_path = pathlib.Path(sys.argv[1])
workflow = yaml.safe_load(workflow_path.read_text(encoding="utf-8"))
inputs = workflow["inputs"]

if "scope" in inputs:
    raise SystemExit("FAIL: workflow must not expose an unused scope input")

print("PASS: workflow exposes only supported inputs")
PY
