#!/usr/bin/env bash

set -euo pipefail

SCRIPT_DIR="$(CDPATH="" cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)"
COMMON_SCRIPT="$SCRIPT_DIR/common.sh"
TEMP_REPO=""
TEMP_OUTPUT=""

cleanup() {
    if [ -n "$TEMP_REPO" ]; then
        rm -rf "$TEMP_REPO"
    fi
    if [ -n "$TEMP_OUTPUT" ]; then
        rm -f "$TEMP_OUTPUT"
    fi
}

trap cleanup EXIT

# 在限定时间内调用模板解析，避免回归测试本身被无限循环卡住。
resolve_plan_with_timeout() {
    local output_file="$1"
    local repo_root="$2"
    local child_pid
    local attempt

    (
        source "$COMMON_SCRIPT"
        resolve_template_content "plan" "$repo_root"
    ) >"$output_file" &
    child_pid=$!

    for (( attempt=0; attempt<20; attempt++ )); do
        if ! kill -0 "$child_pid" 2>/dev/null; then
            wait "$child_pid"
            return $?
        fi
        sleep 0.1
    done

    kill "$child_pid" 2>/dev/null || true
    wait "$child_pid" 2>/dev/null || true
    return 124
}

# 验证包装层只替换包装模板中原有的每个占位符，不能重复处理基础内容中的文字占位符。
test_wrap_replaces_original_placeholders_without_reprocessing_base_content() {
    local output_file
    local actual
    local expected

    TEMP_REPO="$(mktemp -d)"
    mkdir -p "$TEMP_REPO/.specify/templates" \
        "$TEMP_REPO/.specify/presets/wrapper/templates"
    printf '%s' '{"presets":{"wrapper":{"priority":1,"enabled":true}}}' \
        >"$TEMP_REPO/.specify/presets/.registry"
    printf '%s\n' 'provides:' '  templates:' '    - type: template' '      name: plan' \
        '      file: templates/plan.md' '      strategy: wrap' \
        >"$TEMP_REPO/.specify/presets/wrapper/preset.yml"
    printf '%s' '基础内容 {CORE_TEMPLATE}' >"$TEMP_REPO/.specify/templates/plan.md"
    printf '%s' '包装开始 {CORE_TEMPLATE} 中间 {CORE_TEMPLATE} 包装结束' \
        >"$TEMP_REPO/.specify/presets/wrapper/templates/plan.md"

    TEMP_OUTPUT="$(mktemp)"
    output_file="$TEMP_OUTPUT"
    if ! resolve_plan_with_timeout "$output_file" "$TEMP_REPO"; then
        echo "FAIL: 包装模板解析未在限定时间内结束" >&2
        return 1
    fi

    actual="$(cat "$output_file")"
    expected='包装开始 基础内容 {CORE_TEMPLATE} 中间 基础内容 {CORE_TEMPLATE} 包装结束'
    if [ "$actual" != "$expected" ]; then
        echo "FAIL: 包装模板替换结果不正确" >&2
        return 1
    fi
}

test_wrap_replaces_original_placeholders_without_reprocessing_base_content
echo "PASS: common.sh wrap composition"
