# 规格工作流

由 [根规则](../../AGENTS.md) 按任务加载。Spec Kit 固化需求、方案和任务；superpowers 根据通过分析的任务清单执行、验证和收尾。

## 先隔离，再定位规格

1. 按 [Git 与 worktree](git-worktree.md) 完成首次写入门禁；规格和计划文件也必须写在本任务独立 worktree 的非 `main` 分支。
2. 先查看 `specs/` 及相关子项目文档，结合任务内容找到已有 `spec.md`、`plan.md`、`tasks.md`；不能只按当前分支名称判断规格是否存在。
3. 在调用 Spec Kit 命令和脚本前，显式设置 `SPECIFY_FEATURE_DIRECTORY` 为当前 worktree 内选定的规格目录，并核实解析结果。分支名与规格目录名独立，不要求二者一致。
4. `feature.json` 仅是本机指针。缺失时继续显式绑定已有规格，不重跑 `speckit-specify` 创建重复功能，不重建已有 `.specify/`。
5. 只有确属新功能且无已有规格可复用时才分配新编号。并行任务先核对已有目录和其他任务已认领的编号，协调后只创建一份；不能仅凭本地最大编号判断全局空闲。

已有规格续作示例（从本任务 worktree 根目录运行，替换为实际已有目录）：

```bash
SPECIFY_FEATURE_DIRECTORY="$PWD/specs/005-agent-workflow" \
  bash .specify/scripts/bash/check-prerequisites.sh --json --paths-only --no-persist
```

每次调用都显式传入，或在当前任务 shell 中设置后复用；不要假设环境变量会自动跨工具会话保留。工具缺失、规则丢失或目录无法解析时说明具体问题，先解决依赖，不重新生成一套规格或工作流。

## 阶段与唯一事实源

| 阶段 | 产物与职责 |
| --- | --- |
| 需求澄清 | 复用现有结论；新功能可用 brainstorming 澄清，再由 `speckit-specify` / `speckit-clarify` 维护 `spec.md` |
| 技术方案 | `speckit-plan` 维护 `plan.md` 及必要配套设计，遵守 `.specify/memory/constitution.md` |
| 任务拆分 | `speckit-tasks` 维护 `tasks.md`，代码行为变更显式要求有意义的测试先于对应实现 |
| 一致性分析 | `speckit-analyze` 对照需求、计划、任务与验收边界；问题先在 artifact 中修正 |
| 实施交接 | 分析通过的 `tasks.md` 交给 superpowers 的执行、测试、评审和完成校验技能 |
| 收敛与交付 | 按实际验证回写任务状态；需要时用 `speckit-converge` 补齐剩余任务，提交与 PR 按授权执行 |

禁止调用 `speckit.implement` / `speckit-implement`，也不得从 workflow 或其他命令间接触发自动实现。不得使用 superpowers `writing-plans` / `executing-plans` 再建立第二套计划；执行阶段以本规格的 `plan.md` 和 `tasks.md` 为准。

原因明确、单文件且不改变行为的小修复可不新增规格，但仍须独立 worktree、任务分支、已有规格检索和适当验证。纯文档、低风险配置按可观察风险使用链接、语法、差异及相关现有检查，不写只复述文档或镜像实现的测试。配置改变运行行为时仍须相应回归验证。

每项任务完成后及时更新 `tasks.md`；多 Agent 协作时由明确指定的任务清单负责人汇总更新，避免同时改写状态。发现需求或方案错误时，先修正对应 artifact，再继续依赖实现；涉及新决策时按现有授权确认。

## 资料版本管理

- `.specify/` 的项目运行时、模板、工作流和宪法，以及 `specs/` 的规格、计划、任务与验收记录，是持久项目资产。
- `.superpowers/` 和各子项目 `docs/superpowers/` 中的设计、计划、评审与验收资料允许入库；完成任务不删除设计历史。后续任务引用既有资料，避免复制出多份事实源。
- `feature.json`、`local-config` 等本机配置、缓存、日志、环境文件、密钥、临时状态及生成产物按具体路径精确忽略；不要整体忽略 `.specify/`、`specs/`、`.superpowers/` 或子项目 `docs/superpowers/`。
- 是否忽略、是否已跟踪、是否在 HEAD、是否已推送是不同状态，分别核对；放开忽略不等于资料已提交或可从新检出取得。
- 新检出应能读取已提交资料；使用当前 worktree 的显式规格目录，不依赖另一台机器的绝对路径或本机指针。历史设计描述目标，不单独证明代码、数据库、设备或生产已经实现。
