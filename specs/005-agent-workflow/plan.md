# Implementation Plan: 项目规则与开发资料持久化

## Summary

保持业务代码不变，解除持久资料的整体忽略，建立根规则路由和三个详细规则文件，同步 Spec Kit 的规格复用及执行交接。

## Technical Context

- 格式：Markdown、Git ignore、YAML/JSON 工作流配置；现有 Bash/Python 标准库测试。
- 无新增依赖、数据库或对外 API。
- 实施目录：本任务 linked worktree，分支 `chore/project-agent-workflow`。
- 规格目录：`specs/005-agent-workflow`；不要求与 Git 分支同名。

## Constitution Check

- 保护主目录未提交删除，既有业务/数据边界不变。
- 新增规则和测试说明使用中文；资料按原文复制。
- 配置行为回归先建立失败检查，再调整工作流；纯文档采用链接与差异验证。
- 本地验证不宣称远端 CI 或真实业务通过。

## Project Structure

- `AGENTS.md`：简要项目导航、硬约束及按需加载路由。
- `docs/agent-rules/git-worktree.md`：首次写入门禁、复用决策、分支与提交边界。
- `docs/agent-rules/spec-workflow.md`：规格生命周期、续作和资料版本管理。
- `docs/agent-rules/development.md`：项目目录边界、开发和模块验证约定。
- `.specify/`：工作流、相关命令、模板及宪法保持一致；更新受影响摘要。
- `smart-ui/docs/superpowers/doorlock/`、`smart-h5/docs/superpowers/doorlock/`：保留 21 个原始文件。

## Implementation Boundaries

规则文档、工作流配置、资料复制由不同责任方处理；只有主 Agent 更新本规格及 Git 索引。现有 prerequisite 基线失败先定位，若属于本任务干净检出场景则做最小修复。

## Validation

运行现有 common、prerequisite、workflow、integration 测试；检查规则链接、Git 忽略、本机状态排除、首次导入的 21 个文件 SHA-256 一致性及干净检出规格复用。完成独立评审后，分别按提交、推送和创建 PR 的既有授权执行对应动作；不自动合并。

## PR #179 审查修订

复用当前规格与 worktree。逐条验证 9 条审查意见，只纠正有证据的问题：宪法与交付授权表述、门锁契约和页面资料，以及参考原型的不可用状态。业务源码、真实接口和设备行为不在此次修订范围。

- 主 Agent 维护宪法、本规格与审查记录；H5 与管理端资料按目录分配责任，不交叉写文件。
- H5 当前接口从 `smart-h5/src/features/dorm/api.ts` 核实；规划接口明确标为目标。原型先补不可用态编辑/刷新及恢复正常态的失败用例，再做最小修复。
- 管理端以已有证据核实操作开放条件、端口与凭据字段语义；未核实的能力保留待核验状态，不扩展业务功能。
- 原始 21 份资料已在首次导入提交中保存。后续修改不再要求与原始文件摘要相同，改用 Git 差异、链接和跨文档一致性验证，并记录每条审查判定。
- 修复经针对性检查和独立复核后，按本次修复现有 PR 的授权提交并更新原分支；不合并 PR、不部署。
