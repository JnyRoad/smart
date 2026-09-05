# AGENTS.md

裕同智慧园区 `smart` 项目的 Agent 入口。规则随仓库维护，不依赖机器上的个人规则文件；项目介绍与运行方式见 [README.md](README.md)。

## 必须遵守

- **任何开发的首次文件写入前**，必须已处于本任务独立 linked worktree 和本任务非 `main` 分支；写入包括规格、计划、配置、文档、代码和测试。只读分析可以留在 `main`。共享主 checkout 即使已切功能分支，也不得用于开发。
- 复用已属于本任务的 worktree / 分支；本任务 worktree 为 detached HEAD 时从当前 HEAD 建立任务分支。不得混用旧任务目录或强制重复检出已被其他 worktree 占用的分支。隔离失败就停止依赖写入，禁止退回 `main` 或共享目录；不得自动 stash、reset、clean 或覆盖他人未提交改动。
- 所有修改通过功能分支和 PR 合并 `main`，禁止直推 `main`。修改、提交、推送、建 PR、合并、部署分别按已有授权执行；不把前一动作视为后续动作授权，也不对已明确授权的动作重复审批。
- 先检索已有 `specs/`，显式设置 `SPECIFY_FEATURE_DIRECTORY` 绑定本任务规格；缺少本机 `feature.json` 不代表没有规格，不得据此重新创建功能或重建已有 `.specify/`。
- Spec Kit 负责 spec / plan / tasks / analyze；分析通过的 `tasks.md` 交给 superpowers 执行。禁止 `speckit.implement`（`speckit-implement`），不得用 `writing-plans` / `executing-plans` 再建一套计划。
- 持久规格、设计和验收资料允许入库；本机状态、密钥、环境文件、缓存、日志、构建产物和数据库快照不得夹带提交。完成任务不删除设计历史。
- 注释一律使用中文。只读分析不授权修改；真实数据写入、数据库变更、生产和部署须核实目标、范围及现有授权。worktree 只隔离文件，端口、数据库等外部资源须另行确认。

## 按任务加载

首次相关行动前读取下表对应规则；同一对话已读取的内容直接复用，任务变化时补读新增场景，内容因压缩丢失时重读。只读任务不因位于代码仓库而加载完整开发流程。所需文件缺失时报告具体路径并暂停依赖步骤，不自动重生成规则。

| 任务 | 必读规则 |
| --- | --- |
| 任意文件修改，或 worktree / 分支 / 提交 / 推送 / PR 操作 | [Git 与 worktree](docs/agent-rules/git-worktree.md) |
| 新功能、规格创建或续作、计划、任务拆分，以及进入开发执行 | [规格工作流](docs/agent-rules/spec-workflow.md) |
| 实现、修复、配置、文档修改或测试验证 | [开发与模块边界](docs/agent-rules/development.md) |
| 只读分析、评审 | 本入口与目标模块 README；涉及规格一致性或 Git 状态时追加对应规则 |

## 子项目入口

子项目平铺为同级目录，禁止在子项目内另建 `.git`。新增子项目同步维护本清单与根 README；设计文档放对应子项目自己的 `docs/`。

| 目录 | 范围 |
| --- | --- |
| `smart/` | 基础平台后端：网关、认证、UPMS、公共组件 |
| `smart-module/` | 业务微服务后端：App、平台、设备桥接、数据、调度等 |
| `smart-ui/` | Vue 2 管理后台 |
| [smart-print-renderer/](smart-print-renderer/README.md) | pdfme 单面模板、双面组合及私有 PDF 渲染；业务授权由平台服务负责，文件适配/实机通道待验收 |
| [smart-print-client/](smart-print-client/README.md) | Windows 打印工作站、持久命令去重与设备适配；不得自动重放提交结果不明的命令 |
| [smart-h5/](smart-h5/README.md) | 当前维护的 Next.js 微信 H5 |
| `smart-h5-vue2/` | 历史 Vue2 H5，只读参考，不再维护或发布 |
| [smart-app-uniapp/](smart-app-uniapp/README.md) | 「裕慧家园」App 客户端，与 H5 并行，对接 `smart-module/smart-app` 后端 |

模块技术栈与命令见 [根 README](README.md)，细分目录及验证约定见 [开发规则](docs/agent-rules/development.md)，项目级资料见 [docs/README.md](docs/README.md)。架构与功能判断以当前源码、README 和配置为准，历史设计不等于已实现行为。
