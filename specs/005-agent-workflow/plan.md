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

运行现有 common、prerequisite、workflow、integration 测试；检查规则链接、Git 忽略、本机状态排除、21 个文件 SHA-256 一致性及干净检出规格复用；完成独立评审后提交和推送。
