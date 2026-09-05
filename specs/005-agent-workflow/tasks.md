# Tasks: 项目规则与开发资料持久化

## Phase 1: Setup

- [x] T001 核对 main、原有删除与远端，创建本任务 worktree 和分支。
- [x] T002 固化 `specs/005-agent-workflow/` 的规格、计划和验收方式；规格分析确认范围、依赖与验收一一对应。

## Phase 2: 规则与资料

- [x] T003 [P] [US1] 调整 `.gitignore`，原样复制两组 `docs/superpowers/doorlock/` 共 21 个资料文件并验证摘要。
- [x] T004 [P] [US2] 编写 `AGENTS.md` 和 `docs/agent-rules/git-worktree.md`，覆盖首次写入前隔离与五类状态决策。
- [x] T005 [US3] 编写 `docs/agent-rules/spec-workflow.md`、`development.md`，同步 `README.md` 和 `docs/README.md` 导航。

## Phase 3: 工作流同步

- [x] T006 [P] [US3] 在 `.specify/workflows/speckit/workflow.test.sh` 先建立禁止自动实现及明确交接的失败检查，定位 prerequisite 基线失败。
- [x] T007 [US3] 同步 `.specify/commands/`、模板、workflow、constitution 和 integration 摘要，明确工作区门禁、规格复用与测试策略。

## Phase 4: 验证与交付

- [x] T008 [US1] 检查资料完整性、本机状态忽略、规则链接和干净检出规格复用。
- [x] T009 [US3] 运行相关现有测试并完成独立评审，修复已确认问题。
- [ ] T010 提交本任务文件并推送 origin 功能分支，创建 PR，报告验证与限制。

## Dependencies & Parallel Work

T001 → T002；T003 与 T004/T005 与 T006/T007 可并行但文件所有权不重叠；T006 的失败检查先于 T007；全部汇合到 T008/T009，最后 T010。纯文档不新增镜像实现的测试。
