# 2026-09-05 指令精简验证

本记录对应 [本轮规格](spec.md#2026-09-05-指令精简修订) 和 T017–T023。调整长期指令、Spec Kit 命令与 workflow、模板、integration manifests、资料归属和本规格目录的记录；不改业务代码，不修改 Skills。

## 指定问题处理

| 原规则 | 处理结果与权威来源 |
| --- | --- |
| 不保留人工脚本目录 | 删除目录禁令；版本化数据库变更与回滚脚本按 [资料归属](../../docs/agent-rules/spec-workflow.md#资料归属) 保留，执行仍受数据授权限制。 |
| 所有行为修改严格先失败测试 | [开发验证](../../docs/agent-rules/development.md#验证) 保留回归要求，测试顺序由任务决定；宪法、模板、命令及 workflow 改为引用该政策。 |
| 每函数、每步骤注释 | 全局删除机械注释要求；[项目代码约定](../../docs/agent-rules/development.md#代码与资料) 保留中文注释及需要说明的业务约束。 |
| 禁用命名词、按“和”拆函数、调度函数限制 | 从全局开发规则删除。 |
| 冲突取更严、artifact 唯一事实源 | 改为明确的指令优先级和主题权威来源；规格表示意图，实现状态依据源码、配置和验证。 |
| dirty 内容一律再次确认 | 全局通用规则沿用已有授权；保留其他任务的改动，越出授权才暂停对应操作。 |
| PR 三轮无变化停止、正面 reaction 即通过 | 删除固定轮次停止；仅项目明确定义且对应最新 SHA 的信号可用，仍核对 required checks 和未解决审查意见。 |
| 完成后清理过时 checklist | [checklist 命令](../../.specify/commands/speckit.checklist.md) 保留 reviewer 归属与追加语义，禁止生成器自动清理既有条目。 |

## A、B、C 的归属

| 内容 | 单一维护位置 |
| --- | --- |
| 开发隔离、任务分支、Git 授权、他人改动和外部资源归属 | [Git 与 worktree](../../docs/agent-rules/git-worktree.md) |
| 平台权限、Oracle 约束、凭据与诊断隐私、中文注释、验收政策 | [开发规则](../../docs/agent-rules/development.md) |
| 功能绑定、产物归属、Spec Kit / superpowers 交接 | [规格工作流](../../docs/agent-rules/spec-workflow.md) |
| 模块、版本、目录与运行事实 | [项目 README](../../README.md)及其链接的模块 README |
| 设计和资料放置位置 | [docs README](../../docs/README.md) |
| Spec Kit hook 和公共命令接口 | [命令契约](../../.specify/commands/README.md)；各命令仅维护自身输入、输出和权限边界 |
| 本轮需求、例外与历史 | 本规格目录；历史记录不重新生成 |

`AGENTS.md` 和宪法作为主题索引，不再复制政策正文。已删除非 Skill 内容中的通用思考、固定分析步骤、机械拆分、重复规划与测试训诫。按授权删除 `smart-ui/.cursorrules`；`CLAUDE.md` 继续只引用 `AGENTS.md`。

全局规则作为没有适用项目规则时的默认。12 份全局文件由 461 行精简至 162 行，已经写回并核验；原文备份保存在仓库外。

## 验证结果

- `common.test.sh`：通过，验证公共脚本组合。
- `check-prerequisites.test.sh`：通过，覆盖互斥参数、有/无本机指针的只读解析及任务文件要求。
- `workflow.test.sh`：通过，验证输入、tasks → analyze 顺序及 superpowers 交接，无自动实现入口。
- `generic.test.sh`：通过，验证 10 份命令文件的清单摘要和原有 Git 跟踪状态。
- 两份 integration manifest 共 22 个文件的 SHA-256 与当前内容一致。
- 显式绑定本规格的 `--no-persist` 解析通过，未生成本机 `feature.json`。
- 已核验的 128 个 Skill 入口 SHA-256 与修改前一致；本轮未编辑 Skill 文件。
- Markdown 本地链接、锚点和 `git diff --check` 检查通过。未新增镜像文本测试。

测试验证的是当前工作区的脚本、文件契约和结构。未运行新一轮完整 Agent 工作流、外部 hook、GitHub issue 创建、PR 监控或生产操作；这些结果不作为端到端执行通过的证明。现有测试的“干净检出”输出仅验证原有命令跟踪，不能证明本轮新增公共契约已提交。

## 提交前验证快照

初次验证时，项目修改位于独立任务 worktree，尚未提交、推送或合并；新增文档尚未进入 Git 提交。2026-09-05（UTC），用户旅途在当前会话明确授权 PR 交付、问题修复和条件合并，范围见 [规格](spec.md#pr-交付授权)。共享主 checkout 的原有删除和未跟踪目录保持原状。Skills 中审计发现的旧默认仍保留，项目入口已明确以本项目权威规则处理冲突。

全局文件已完成独立复核，按已授权的 12 个路径写回，写前逐字核对原文，写后 SHA-256 与审查稿一致。评审中恢复了凭据与诊断隐私、同文件的他人改动保护、精确命令标识和 `UNVERIFIED` 输出字段；未恢复绝对测试先行、机械拆分或“冲突取更严”。
