# 规格工作流

本文件维护功能目录绑定、规格产物与执行交接。任何文件写入先满足 [Git 与 worktree](git-worktree.md)。

## 功能绑定

- 从 `specs/` 和相关模块资料定位既有功能；分支名不决定规格目录名。已有规格按本轮授权续作，不复制模板覆盖已有结论。
- 每次调用 Spec Kit 显式传入当前 worktree 的 `SPECIFY_FEATURE_DIRECTORY`。`feature.json` 是可缺失的本机指针，不能据此重复建功能或重建 `.specify/`。
- 确认没有可复用规格的新功能才分配新目录；并行任务协调编号，不能把本地最大编号当成全局空闲证明。

只读定位示例：

```bash
SPECIFY_FEATURE_DIRECTORY="$PWD/specs/005-agent-workflow" \
  bash .specify/scripts/bash/check-prerequisites.sh --json --paths-only --no-persist
```

涉及读取和解析的其他参数、模板与 hook 契约见 [.specify/commands](../../.specify/commands/README.md)。未带 `--no-persist` 的路径解析可能更新本机指针，不能用于只读分析。

## 产物与交接

| 产物 | 维护入口 |
| --- | --- |
| `spec.md`：已确认需求和验收边界 | `speckit.specify` / `speckit.clarify` |
| `plan.md` 与必要的配套设计 | `speckit.plan` |
| `tasks.md`：任务、依赖与实际完成状态 | `speckit.tasks`，续作可用 `speckit.converge` |
| 规格、计划、任务之间的一致性报告 | `speckit.analyze` |

项目命令正文位于 `.specify/commands/`。外部同名 Skill 的通用默认与本项目不一致时，使用项目命令的契约，不改写已确认规格来迁就旧副本。

分析通过的 `tasks.md` 交给适用的 superpowers 执行能力；执行以该规格的 `plan.md` 和 `tasks.md` 为输入。项目禁用 `speckit.implement` / `speckit-implement`，不得间接调用；不使用 `writing-plans` / `executing-plans` 创建第二套计划。通用 Skill 的测试顺序、审批与收尾步骤不改变 [项目验证](development.md#验证) 和 Git 授权。

范围明确的局部修复可更新既有规格中的相关条目，无需补建不涉及的设计产物。规格记录意图；当前源码、配置和运行证据记录实现状态。修正不涉及新决策的文档错误可在已授权任务中完成，新增范围或未授权行为另行确认。多 Agent 修改任务状态时由指定负责人汇总。

## 资料归属

`.specify/` 的项目运行时、模板、工作流和宪法，`specs/` 的规格与验收，以及 `.superpowers/` 和模块 `docs/superpowers/` 的持久设计与评审记录属于项目资产，不因任务完成删除或整体忽略。

本机功能指针、local-config、缓存、日志、环境文件、凭据、证书、构建/测试产物和数据库快照不入库；按具体路径排除，忽略项以仓库及模块 `.gitignore` 为准。受版本管理的数据库变更与回滚脚本属于交付资产，执行条件见 [数据与权限](development.md#数据与权限)。

文档目录归属见 [docs README](../README.md)。声称资料可从新检出或远端取得前，核对其未被忽略且已进入对应提交；工作区存在或已暂存不等于已交付。历史设计与验收只证明其记录的版本和范围。
