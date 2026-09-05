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

各命令只读取 `.specify/extensions.yml` 中的 `hooks.before_<command>` /
`hooks.after_<command>`。文件不存在或无对应条目时跳过。YAML 无效时报告配置诊断，
跳过扩展检查并继续命令本身；这不代表任何 hook 已执行或通过。此处保留原生命令的
扩展兼容策略。`enabled: false` 跳过，缺少 `enabled` 视为启用。

本命令跳过非空 `condition` 的 hook，报告交由外部 HookExecutor 处理，不求值、不发出
该 hook 的 `EXECUTE_COMMAND`，也不等待条件回调；外部运行时负责条件求值及调度。
仅空、null 或缺失的 `condition` 在本命令中视为可执行。
可执行 hook 保留 `extension`、`command`、`description` 和
`prompt`。`optional: true` 只报告给用户；`optional: false` 输出
`EXECUTE_COMMAND: {command}`，随后用当前会话支持的调用方式实际执行并等待完成，才进入
后续步骤。输出标记不代替调用，也不能作为已完成的证据。hook 受本命令的只读、产物及
既有授权边界约束；不兼容的 hook 不执行并报告原因。

## 命令结果

命令报告实际读取或写入的路径、跳过的 hook、验证结果和未解决项。checklist、外部 issue
写入和其他命令特有的授权边界由对应命令正文维护；不得从分析报告推导额外授权。
