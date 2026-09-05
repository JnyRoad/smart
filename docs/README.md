# docs

`docs/` 保存项目级规则、跨子项目说明和不属于单一子项目的资料。它不是 `smart/` 后端的开发文档目录；后端文档在 [smart/docs/](../smart/docs/)，H5 资料在 [smart-h5/docs/](../smart-h5/docs/)。

## 规则入口

从 [AGENTS.md](../AGENTS.md) 开始，按当前任务读取并复用：

- [Git 与 worktree](agent-rules/git-worktree.md)：首次写入前隔离、复用决策、分支和授权边界。
- [规格工作流](agent-rules/spec-workflow.md)：已有规格绑定、Spec Kit 与 superpowers 交接、资料版本管理。
- [开发与模块边界](agent-rules/development.md)：子项目职责、数据边界、中文注释及模块验证。

## 开发资料

- [裕慧家园门锁统一接入与 Oracle 基线](../specs/011-doorlock-oracle/spec.md)：当前跨模块规格、数据映射、契约、任务和验收；不表示已经实现或上线。
- [specs/](../specs/)：持久规格、计划、任务清单与验收记录。
- [.specify/](../.specify/)：项目工作流、命令、模板和宪法；本机指针与缓存按具体路径忽略。
- [管理端门锁资料](../smart-ui/docs/superpowers/doorlock/) 与 [H5 门锁资料](../smart-h5/docs/superpowers/doorlock/)：保留设计、计划和验收历史；内容是否已实现需核对当前代码与验证证据。
- 管理端页面功能清单文档：管理端页面、功能与对版参考。

## 维护规则

- 项目级文档放这里；子项目独占的设计、接口、原型和 ADR 放回该子项目的 `docs/`。
- `.specify/`、`specs/`、`.superpowers/` 和子项目 `docs/superpowers/` 的持久资料允许入库，不因任务完成删除设计历史；本机状态、缓存、日志和环境文件不提交。
- 清单类文档标明来源及适用范围，避免被误读为当前实现承诺。
- 涉及生产配置、账号、密钥、真实客户数据的材料不要提交。
