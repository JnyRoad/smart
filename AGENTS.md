# AGENTS.md

裕同智慧园区 `smart` 的项目指令入口。模块说明和运行命令见 [README](README.md)。

## 规则归属

首次进行对应操作前读取下列权威来源；本入口不复制政策正文。

| 操作范围 | 权威来源 |
| --- | --- |
| 任何文件写入，或 worktree、分支、提交、推送、PR、合并及清理操作 | [Git 与 worktree](docs/agent-rules/git-worktree.md) |
| 规格、计划、任务清单、Spec Kit 命令或开发执行 | [规格工作流](docs/agent-rules/spec-workflow.md) |
| 实现、配置、数据库、部署、注释或验收 | [开发规则](docs/agent-rules/development.md) |
| 只读分析、评审 | 目标模块 README；涉及数据、验收或规格一致性时读取对应规则 |
| 模块定位、技术栈与运行方式 | [项目 README](README.md)及对应模块 README |
| 设计、接口和验收资料定位 | [文档目录](docs/README.md) |

项目政策按上述主题归属维护，具体功能的已确认目标和例外放在对应 `specs/` 中。用户当前明确的范围与授权优先；Skill 的通用流程不替代项目政策。
