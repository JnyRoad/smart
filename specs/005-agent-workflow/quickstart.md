# 验证指南

在本任务 worktree 根目录执行：

```bash
bash .specify/scripts/bash/common.test.sh
bash .specify/scripts/bash/check-prerequisites.test.sh
bash .specify/workflows/speckit/workflow.test.sh
bash .specify/integrations/generic.test.sh
node --test smart-h5/docs/superpowers/doorlock/reference/mockups/lock/index.test.mjs
git diff --check
```

通过 Git 索引/提交核对两组门锁资料的 21 个原文件均存在。首次导入提交
`e43d826a0eb769e61e1d71ed890e4936d0200eec` 与当时源文件逐一比较 SHA-256；
后续审查修订通过 Git 差异追溯，不要求修订后的文件继续与原始摘要相同。
检查 `.specify/feature.json`、本机扩展配置、缓存仍被忽略，规格和设计文件未被忽略。

在不含 `.specify/feature.json` 的干净检出中，以 `SPECIFY_FEATURE_DIRECTORY=specs/004-permission-date` 调用 `bash .specify/scripts/bash/check-prerequisites.sh --json --no-persist`，应解析已有规格且不写本机指针、不新增规格目录。

根 `AGENTS.md` 的规则路由与详细文档相对链接应全部存在。对照五类工作区状态评审动作，确认不存在创建隔离失败后继续 main 写入的回退。

## 首次交付验证记录

- common、prerequisite、workflow、generic integration 四组 Bash 检查通过；两个清单共 22 项摘要匹配。
- `specify workflow info speckit` 成功解析本地 1.1.0 工作流的七个步骤；未运行整条生成流程或真实业务构建。
- 回归检查能拒绝基线中自动调用 implement 的配置。前置检查测试改为临时项目，覆盖没有本机指针、已有其他任务指针和缺少必需任务文件的情况。
- 从暂存索引重建干净目录，21 份资料摘要一致；显式绑定 004 已有规格成功，没有新增规格或写入指针。
- 原样导入的 19 份资料有既存 EOF 空行，保留原文。全量 `git diff --cached --check` 会提示这些空行；本次新改文件单独检查通过，导入文件忽略 `blank-at-eof` 后无其他空白问题。
- 独立评审发现 specify 续作分支可能被后续全文生成步骤覆盖，已补明确的只读返回和局部修订分支，保留已有规格及未受影响的清单状态。

## PR #179 审查修订验证

- [逐条审查判定](review-pr179.md)：8 条缺陷或歧义已修正；README 扩写属于可选改进，未采纳。
- 原型首先复现未入住确认后仍能改码和触发刷新，初始 3 项用例中 2 项失败；修复后增加直接调用入口、切换时关闭编辑层的覆盖。恢复态新增遮罩断言又复现 1 项失败，补齐恢复逻辑后最终 4 项全部通过。
- 最终用例执行真实 HTML 内联脚本：不可用时点击与直接调用均不改码、不刷新；恢复可用时关闭遮罩，能够实际提交新动态码并触发刷新演示。
- common、prerequisite、workflow、generic integration 四组 Bash 检查通过；22 项清单摘要仍匹配。首次导入提交的 21 份原始资料摘要再次核对一致，当前仍保留全部原文件。
- 检查 24 个持久资料路径可入库、9 个本机状态路径仍被忽略（包括 `.codebase-memory/graph.db.zst`）；本轮修改和导航文档的本地链接可解析。审查记录仅增加已有本地文件链接与远端评论链接。
- 新增及本轮修改差异检查通过；治理文档与门锁修复均经过独立复核，最后的入口断言、遮罩恢复和客户端身份说明也已定点复核。
- 未运行全业务构建、pnpm/Vitest/Playwright 或整条交互式 Spec Kit 生成流程；本轮不修改业务源码，原型验证只使用 Node 内置模块，未安装前端依赖。真实浏览器、微信、接口、服务端鉴权及设备验收未验证。
