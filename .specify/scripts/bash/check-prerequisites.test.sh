#!/usr/bin/env bash

set -euo pipefail

SCRIPT_DIR="$(CDPATH="" cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)"
OUTPUT_FILE="$(mktemp)"
FEATURE_POINTER="$SCRIPT_DIR/../feature.json"
FEATURE_SNAPSHOT="$(mktemp)"
FEATURE_POINTER_EXISTED=false

if [[ -e "$FEATURE_POINTER" ]]; then
    cp "$FEATURE_POINTER" "$FEATURE_SNAPSHOT"
    FEATURE_POINTER_EXISTED=true
fi

# 清理参数组合测试生成的临时输出文件。
cleanup() {
    rm -f "$OUTPUT_FILE" "$FEATURE_SNAPSHOT"
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

# 分析和收敛工作流显式请求只读路径解析时，既要成功验证前置条件，也不能改写功能指针。
if ! "$SCRIPT_DIR/check-prerequisites.sh" --json --require-tasks --include-tasks --no-persist >"$OUTPUT_FILE" 2>&1; then
    echo "FAIL: --no-persist 应保留正常的前置条件验证" >&2
    exit 1
fi
if $FEATURE_POINTER_EXISTED && ! cmp -s "$FEATURE_POINTER" "$FEATURE_SNAPSHOT"; then
    echo "FAIL: --no-persist 不应改写 feature.json" >&2
    exit 1
fi
if ! $FEATURE_POINTER_EXISTED && [[ -e "$FEATURE_POINTER" ]]; then
    echo "FAIL: --no-persist 不应创建 feature.json" >&2
    exit 1
fi

echo "PASS: paths-only 模式拒绝模板请求；--no-persist 保持功能指针不变"
