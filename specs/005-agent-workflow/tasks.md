# Tasks: 项目规则与开发资料持久化

> T001–T016 和原交付结果是历史记录；当前修订从 T017 开始。

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
- [x] T010 核实提交、推送和创建 PR 各自已有授权后，执行对应交付动作，报告验证与限制；不自动合并。

## Phase 5: PR #179 审查修订

- [x] T011 [FR-002/FR-007/FR-008] 核对最新 PR 与 9 条意见，澄清宪法首次写入范围及交付授权，同步本规格、计划和任务并完成一致性分析。
- [x] T012 [P] [FR-008] 在 H5 门锁参考原型中先补不可用态禁止编辑/刷新与正常态恢复的失败回归用例。
- [x] T013 [FR-008] 依据当前 H5 适配器修订接口契约，修复原型状态门禁并通过 T012 回归。
- [x] T014 [P] [FR-008] 核实并修正管理端资料中的来源引用、操作开放条件、端口和凭据语义；README 建议按实际规则与文档角色判定。
- [x] T015 [FR-006/FR-007/FR-008] 记录全部审查判定，验证差异、链接及相关回归，完成独立复核后按已有授权提交并更新 PR #179。
- [x] T016 [FR-006/FR-008] 修正验证指南的完整差异检查命令：以 `git diff HEAD --check` 同时覆盖暂存与未暂存改动，保留首次导入资料的 EOF 空行记录。

## Dependencies & Parallel Work

T001 → T002；T003 与 T004/T005 与 T006/T007 可并行但文件所有权不重叠；T006 的失败检查先于 T007；全部汇合到 T008/T009，最后 T010。纯文档不新增镜像实现的测试。

审查修订：T011 → T012 → T013；T014 可与 T012/T013 并行；全部汇合到 T015。仅主 Agent 更新本任务清单与 Git 索引。

## 交付结果

按本会话已有的资料提交及批准方案交付授权，规则、资料和工作流已在 `chore/project-agent-workflow` 提交并推送至 origin，PR：
[PR #179](https://github.com/JnyRoad/smart/pull/179)。独立评审与验证见 [quickstart.md](quickstart.md)。
PR 尚未合并；合并后 main 才包含本次资料及规则。

审查修复提交 `3bfcc464095b414c24acb8a7aa5cf0eb38134275` 已推送原分支，PR 描述已同步实际改动与验证范围。
9 条意见的处理结论见 [review-pr179.md](review-pr179.md)；本地回归与独立复核通过，远端新一轮审查尚在运行，不视为已经通过。

## Phase 6: 2026-09-05 指令精简

- [x] T017 [FR-009/FR-012] 建立独立 worktree，复用本规格，记录本轮授权、范围和文件所有权。
- [x] T018 [FR-009/FR-010] 精简 `AGENTS.md`、`docs/agent-rules/` 与 `.specify/memory/constitution.md`，集中项目保护和验收要求。
- [x] T019 [P] [FR-010/FR-012] 在临时目录准备全局 `AGENTS.md` 与 `agent-rules/*.md` 的备份和草稿，修复指定全局问题。
- [x] T020 [P] [FR-009/FR-010/FR-011] 精简 `.specify/commands/`、`templates/`，同步 workflow 与受影响的完整性校验。
- [x] T021 [P] [FR-009/FR-011/FR-012] 集中 `README.md`、`docs/README.md` 的知识和导航，删除 `smart-ui/.cursorrules`。
- [x] T022 [FR-012] 独立核验全局草稿后写回已授权文件，核对 Skills 摘要未变。
- [x] T023 [FR-009/FR-010/FR-011/FR-012] 运行现有相关检查、链接与差异检查，完成独立评审，记录实际结果和未处理的 Skill 限制。

依赖：T017 → T018/T019/T020/T021；T019 → T022；全部汇合到 T023。仅主 Agent 更新本任务清单。无提交、推送、PR、合并或部署步骤。

本轮结果见 [指令精简验证](validation-20260905.md)。

后续 Git 交付已获旅途明确授权：提交并创建 PR，监控当前提交，修复有效审查问题后重新推送，满足条件后合并。Phase 6 的完成状态只记录本地精简结果；远端交付状态以本次 PR 为准。
