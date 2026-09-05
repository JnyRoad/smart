# Spec Kit 命令契约

命令文件只描述本命令特有的输入、产物和结果。共享事实按以下权威来源维护：

- 功能目录绑定、规格产物、阶段交接和禁用实现入口见
  [规格工作流](../../docs/agent-rules/spec-workflow.md)。
- 首次写入、worktree、分支和 Git 交付边界见
  [Git 与 worktree](../../docs/agent-rules/git-worktree.md)。
- 项目目录、数据权限和验证策略见
  [开发规则](../../docs/agent-rules/development.md)。

## 输入与路径

$ARGUMENTS 是调用者在命令后提供的原始内容；为空时按命令要求报告缺少输入。调用
.specify/scripts/bash/check-prerequisites.sh、setup-plan.sh 或 setup-tasks.sh 时解析
JSON 输出，不猜测分支名。只读路径解析使用 --no-persist。

命令读取 `.specify/memory/constitution.md` 时，继续读取该索引列出的适用规则正文；宪法索引
不能替代 Git、开发、规格或 README 中的具体约定。

## 扩展 hooks

各命令只读取自己的 hook 键（`before_<command>` / `after_<command>`）。`.specify/extensions.yml`
不存在、无对应条目或 YAML 无效时跳过。enabled: false 跳过；缺少 enabled 视为启用。

对非空 `condition` 不解释、不求值、不自行决定是否执行，交给调用运行时；空或缺失的
`condition` 才视为可执行。可执行 hook 保留 `extension`、`command`、`description` 和
`prompt`：optional hook 只报告给用户，mandatory hook 输出 `EXECUTE_COMMAND: {command}`
并等待结果。
输出标记不代替运行时执行；命令不能把 hook 文本当作已完成。

## 命令结果

命令报告实际读取或写入的路径、跳过的 hook、验证结果和未解决项。checklist、外部 issue
写入和其他命令特有的授权边界由对应命令正文维护；不得从分析报告推导额外授权。
