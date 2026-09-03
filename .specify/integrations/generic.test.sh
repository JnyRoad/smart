#!/usr/bin/env bash

set -euo pipefail

SCRIPT_DIR="$(CDPATH="" cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(CDPATH="" cd -- "$SCRIPT_DIR/../.." && pwd)"
INTEGRATION_FILE="$REPO_ROOT/.specify/integration.json"
MANIFEST_FILE="$SCRIPT_DIR/generic.manifest.json"

# 验证默认 generic 集成引用的命令文件均在仓库中受 Git 跟踪，确保干净检出可直接调用。
while IFS= read -r command_path; do
    if ! git -C "$REPO_ROOT" ls-files --error-unmatch "$command_path" >/dev/null 2>&1; then
        echo "FAIL: generic 命令未受 Git 跟踪: $command_path" >&2
        exit 1
    fi
    if [[ ! -f "$REPO_ROOT/$command_path" ]]; then
        echo "FAIL: generic 命令文件不存在: $command_path" >&2
        exit 1
    fi
done < <(python3 - "$INTEGRATION_FILE" "$MANIFEST_FILE" "$REPO_ROOT" <<'PY'
import hashlib
import json
import pathlib
import sys

integration = json.loads(pathlib.Path(sys.argv[1]).read_text(encoding="utf-8"))
manifest = json.loads(pathlib.Path(sys.argv[2]).read_text(encoding="utf-8"))
repo_root = pathlib.Path(sys.argv[3])
commands_dir = integration["integration_settings"]["generic"]["parsed_options"]["commands_dir"]

for command_path, expected_hash in manifest["files"].items():
    if not command_path.startswith(commands_dir + "/"):
        raise SystemExit(f"FAIL: 清单路径不在 commands_dir 下: {command_path}")
    command_file = repo_root / command_path
    if not command_file.is_file():
        raise SystemExit(f"FAIL: 清单命令文件不存在: {command_path}")
    actual_hash = hashlib.sha256(command_file.read_bytes()).hexdigest()
    if actual_hash != expected_hash:
        raise SystemExit(f"FAIL: 清单命令文件摘要不匹配: {command_path}")
    print(command_path)
PY
)

echo "PASS: generic 集成命令可在干净检出中获取"
