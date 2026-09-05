# docs

`docs/` 保存项目级规则、跨子项目说明和不属于单一子项目的资料。它不是 `smart/` 后端的开发文档目录：后端资料在 [smart/docs/](../smart/docs/)，当前 H5 资料在 [smart-h5/docs/](../smart-h5/docs/)，各业务模块的专项资料在对应模块目录的 `docs/`。

## 规则入口

从 [AGENTS.md](../AGENTS.md) 开始，按当前任务读取并复用：

- [Git 与 worktree](agent-rules/git-worktree.md)：工作区隔离、分支和交付边界。
- [规格工作流](agent-rules/spec-workflow.md)：规格复用、Spec Kit 产物和执行交接。
- [开发规则](agent-rules/development.md)：代码中文约定、权限 / Oracle 边界和验证政策。

技术栈、模块目录和运行命令集中在[项目 README](../README.md)；App 资源和打包细节见 [App README](../smart-app-uniapp/README.md)。

## 资料导航

- [裕慧家园门锁统一接入与 Oracle 基线](../specs/011-doorlock-oracle/spec.md)：当前跨模块规格、数据映射、契约、任务和验收；不表示已经实现或上线。
- [项目规格](../specs/)：持久规格、计划、任务清单与验收记录。
- [Spec Kit 工作流资料](../.specify/)：命令、模板、工作流和项目宪法。
- [管理端页面功能清单](legacy-smart-ui-页面功能清单.md)：从旧版 smart-ui 逆向整理的页面与组件参考，用于对照和 H5 重写；它不单独证明当前代码已实现这些能力。
- [管理端门锁资料](../smart-ui/docs/superpowers/doorlock/) 与 [H5 门锁资料](../smart-h5/docs/superpowers/doorlock/)：设计、计划和验收历史；当前实现状态需回到对应源码和验证记录核对。
- [H5 文档](../smart-h5/docs/README.md)：当前 H5 的评估、设计、原型与实施资料。
- [Docker 本地环境说明](../docker/README.md)：本地 Compose 服务、profile 和配置说明。
- [发布脚本说明](../scripts/README.md)：Jar 汇总、发布清单和脚本校验。

## 文档归属

项目级规则、跨模块约定和资料索引放在本目录；单一子项目独占的设计、接口、原型和 ADR 放回该子项目的 `docs/`。设计、计划和验收材料描述其对应阶段的结论，是否已进入当前实现以源码、配置和验证证据为准。
