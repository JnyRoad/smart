# Git 与 worktree

本文件维护项目开发隔离和 Git 交付边界，由 [AGENTS](../../AGENTS.md) 路由。

## 开发隔离

任何开发文件首次写入前，必须位于本任务独立 linked worktree 和非 `main` 分支；包括规格、计划、配置、文档、代码及测试。共享主 checkout 即使在功能分支上也只用于读取。

核对当前工作区、分支、未提交改动与任务归属：

```bash
git rev-parse --show-toplevel
git status --short
git branch --show-current
git worktree list --porcelain
```

| 当前状态 | 处理 |
| --- | --- |
| 共享主 checkout 或 `main` | 从用户指定或已核实的基线创建本任务 linked worktree 与任务分支 |
| 本任务 linked worktree 和分支 | 复用 |
| 本任务 linked worktree 为 detached HEAD | 从当前 HEAD 创建任务分支，保留目录 |
| 其他任务的 worktree，或分支被其他 worktree 占用 | 仅在确认属于本任务时复用；否则另建，不强制双重检出 |
| 创建或进入工作区失败 | 停止依赖写入，不回退到共享目录开发 |

创建隔离目录和任务分支属于已授权开发的准备步骤。仓库内的 worktree 目录必须已被忽略；不得为绕过隔离失败自动提交 `.gitignore`，也不得自动 stash、reset、clean、删除旧 worktree 或覆盖他人未提交改动。

## Git 交付

- 修改在任务功能分支完成，经 PR 合并 `main`；禁止直推 `main`。
- 修改、提交、推送、创建 PR、合并、部署和删除分支/worktree 分别按会话已有授权执行。已覆盖的目标和动作不重复审批；授权不明确时只暂停依赖该授权的操作。
- 提交前核对完整差异，只纳入本任务的改动；同一文件中的其他任务改动也不得夹带。推送核对实际 remote、目标分支和待推送提交。资产排除规则见 [资料归属](spec-workflow.md#资料归属)。
- PR 和合并状态依据远端实际结果。合并后验证目标分支包含预期提交；本地主目录有其他任务改动时保留原状，不为同步 main 自动处理这些改动。

## 外部资源

worktree 仅隔离文件。应用、容器或测试使用的端口、Compose 项目名、数据库、测试数据、挂载和真实设备必须另行核实归属；真实环境操作按 [开发规则](development.md#数据与权限) 执行。
