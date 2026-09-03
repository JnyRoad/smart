#!/usr/bin/env bash

set -euo pipefail

SCRIPT_DIR="$(CDPATH="" cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)"
OUTPUT_FILE="$(mktemp)"

# 清理参数组合测试生成的临时输出文件。
cleanup() {
    rm -f "$OUTPUT_FILE"
}

trap cleanup EXIT

# 验证纯路径解析不能悄悄忽略模板请求，以免返回缺少模板内容的伪成功结果。
if "$SCRIPT_DIR/check-prerequisites.sh" --paths-only --template plan >"$OUTPUT_FILE" 2>&1; then
    echo "FAIL: --paths-only 与 --template 组合必须被拒绝" >&2
    exit 1
fi

if ! rg -q -- "--template is not supported with --paths-only" "$OUTPUT_FILE"; then
    echo "FAIL: 组合参数拒绝信息不明确" >&2
    exit 1
fi

echo "PASS: paths-only 模式拒绝模板请求"
