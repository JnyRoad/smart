#!/usr/bin/env bash
# 在独立临时项目中验证前置检查，避免依赖或改写开发者的当前功能指针。
set -euo pipefail

SCRIPT_DIR="$(CDPATH="" cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)"
FIXTURE_ROOT="$(mktemp -d)"
OUTPUT_FILE="$FIXTURE_ROOT/output.txt"
FEATURE_POINTER="$FIXTURE_ROOT/.specify/feature.json"
FEATURE_SNAPSHOT="$FIXTURE_ROOT/feature.snapshot.json"

# 仅删除本测试创建的临时项目，不读取或恢复真实工作区的功能状态。
cleanup() {
    rm -rf -- "$FIXTURE_ROOT"
}
trap cleanup EXIT

# 提供最小可用规格，显式项目和功能目录不受调用者 shell 状态影响。
mkdir -p "$FIXTURE_ROOT/.specify" "$FIXTURE_ROOT/specs/001-fixture"
printf '# 测试规格\n' > "$FIXTURE_ROOT/specs/001-fixture/spec.md"
printf '# 测试计划\n' > "$FIXTURE_ROOT/specs/001-fixture/plan.md"
printf '# 测试任务\n' > "$FIXTURE_ROOT/specs/001-fixture/tasks.md"
export SPECIFY_INIT_DIR="$FIXTURE_ROOT"
export SPECIFY_FEATURE_DIRECTORY="specs/001-fixture"
unset SPECIFY_FEATURE

# 组合参数仍须被拒绝，不能返回缺少请求模板内容的伪成功。
if bash "$SCRIPT_DIR/check-prerequisites.sh" --paths-only --template plan >"$OUTPUT_FILE" 2>&1; then
    echo "FAIL: --paths-only 与 --template 组合必须被拒绝" >&2
    exit 1
fi
if ! rg -q -- "--template is not supported with --paths-only" "$OUTPUT_FILE"; then
    echo "FAIL: 组合参数拒绝信息不明确" >&2
    exit 1
fi

# 干净检出没有本机指针时，显式绑定已有规格必须成功且不创建指针。
if ! bash "$SCRIPT_DIR/check-prerequisites.sh" --json --require-tasks --include-tasks --no-persist >"$OUTPUT_FILE" 2>&1; then
    cat "$OUTPUT_FILE" >&2
    echo "FAIL: --no-persist 应保留正常的前置条件验证" >&2
    exit 1
fi
if [[ -e "$FEATURE_POINTER" ]]; then
    echo "FAIL: --no-persist 不应创建 feature.json" >&2
    exit 1
fi

# 已有指针指向另一任务时，显式只读解析不得覆盖它。
printf '%s\n' '{"feature_directory":"specs/other-task"}' > "$FEATURE_POINTER"
cp "$FEATURE_POINTER" "$FEATURE_SNAPSHOT"
bash "$SCRIPT_DIR/check-prerequisites.sh" --json --require-tasks --include-tasks --no-persist >"$OUTPUT_FILE"
if ! cmp -s "$FEATURE_POINTER" "$FEATURE_SNAPSHOT"; then
    echo "FAIL: --no-persist 不应改写 feature.json" >&2
    exit 1
fi

# 只读模式仍要验证必需文件，不能以保持状态为由绕过前置条件。
mv "$FIXTURE_ROOT/specs/001-fixture/tasks.md" "$FIXTURE_ROOT/tasks.saved.md"
if bash "$SCRIPT_DIR/check-prerequisites.sh" --json --require-tasks --no-persist >"$OUTPUT_FILE" 2>&1; then
    echo "FAIL: 缺少必需 tasks.md 时必须失败" >&2
    exit 1
fi
if ! rg -q 'tasks.md not found' "$OUTPUT_FILE"; then
    echo "FAIL: 必需任务文件缺失的错误不明确" >&2
    exit 1
fi
cmp -s "$FEATURE_POINTER" "$FEATURE_SNAPSHOT"
echo "PASS: 参数互斥、无指针和已有指针的只读解析、必需任务文件验证"
