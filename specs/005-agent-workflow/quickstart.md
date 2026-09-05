# 验证指南

在本任务 worktree 根目录执行：

```bash
bash .specify/scripts/bash/common.test.sh
bash .specify/scripts/bash/check-prerequisites.test.sh
bash .specify/workflows/speckit/workflow.test.sh
bash .specify/integrations/generic.test.sh
git diff --check
```

通过 Git 索引/提交核对两组门锁资料共 21 个文件；与主目录源文件逐一比较 SHA-256。检查 `.specify/feature.json`、本机扩展配置、缓存仍被忽略，规格和设计文件未被忽略。

在不含 `.specify/feature.json` 的干净检出中，以 `SPECIFY_FEATURE_DIRECTORY=specs/004-permission-date` 调用 `bash .specify/scripts/bash/check-prerequisites.sh --json --no-persist`，应解析已有规格且不写本机指针、不新增规格目录。

根 `AGENTS.md` 的规则路由与详细文档相对链接应全部存在。对照五类工作区状态评审动作，确认不存在创建隔离失败后继续 main 写入的回退。

## 本次验证记录

- common、prerequisite、workflow、generic integration 四组 Bash 检查通过；两个清单共 22 项摘要匹配。
- `specify workflow info speckit` 成功解析本地 1.1.0 工作流的七个步骤；未运行整条生成流程或真实业务构建。
- 回归检查能拒绝基线中自动调用 implement 的配置。前置检查测试改为临时项目，覆盖没有本机指针、已有其他任务指针和缺少必需任务文件的情况。
- 从暂存索引重建干净目录，21 份资料摘要一致；显式绑定 004 已有规格成功，没有新增规格或写入指针。
- 原样导入的 19 份资料有既存 EOF 空行，保留原文。全量 `git diff --cached --check` 会提示这些空行；本次新改文件单独检查通过，导入文件忽略 `blank-at-eof` 后无其他空白问题。
- 独立评审发现 specify 续作分支可能被后续全文生成步骤覆盖，已补明确的只读返回和局部修订分支，保留已有规格及未受影响的清单状态。
