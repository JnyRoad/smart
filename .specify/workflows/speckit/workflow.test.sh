#!/usr/bin/env bash

set -euo pipefail

WORKFLOW_FILE="${WORKFLOW_FILE:-$(CDPATH="" cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)/workflow.yml}"

# 校验工作流输入、分析与交接顺序，防止恢复直接实现入口；仅使用 Python 标准库。
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

step_starts = []
for index, line in enumerate(lines):
    match = re.fullmatch(r"  - id: ([A-Za-z][A-Za-z0-9_-]*)", line)
    if match:
        step_starts.append((index, match.group(1)))

if not step_starts:
    raise SystemExit("FAIL: workflow must declare at least one step")

step_blocks = {}
for position, (start, step_id) in enumerate(step_starts):
    end = step_starts[position + 1][0] if position + 1 < len(step_starts) else len(lines)
    step_blocks[step_id] = "\n".join(lines[start:end])
step_ids = [step_id for _, step_id in step_starts]

if "implement" in step_ids or re.search(r"^    command: speckit\.implement$", "\n".join(lines), re.MULTILINE):
    raise SystemExit("FAIL: workflow must not invoke speckit.implement automatically")

if "tasks" not in step_ids:
    raise SystemExit("FAIL: workflow must generate tasks before analysis")
if "analyze" not in step_ids:
    raise SystemExit("FAIL: workflow must analyze artifacts before implementation handoff")
if step_ids.index("analyze") <= step_ids.index("tasks"):
    raise SystemExit("FAIL: workflow analysis must run after task generation")

analyze_position = step_ids.index("analyze")
if analyze_position + 1 >= len(step_ids):
    raise SystemExit("FAIL: workflow must gate the handoff after analysis")
handoff_block = step_blocks[step_ids[analyze_position + 1]]
if "type: gate" not in handoff_block or "superpowers" not in handoff_block.lower():
    raise SystemExit("FAIL: analysis must hand off to superpowers through an existing gate")

specify_dir = workflow_path.parents[2]
tasks_command = (specify_dir / "commands" / "speckit.tasks.md").read_text(encoding="utf-8")
if re.search(r"^\s*agent:\s*speckit\.implement\s*$", tasks_command, re.MULTILINE):
    raise SystemExit("FAIL: tasks command must not hand off to the disabled implementation entrypoint")

print("PASS: 工作流输入、任务分析顺序和 superpowers 交接，无自动实现入口")
PY
