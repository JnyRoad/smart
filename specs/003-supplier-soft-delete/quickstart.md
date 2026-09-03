# Quickstart: 保密供应商软删除验证

## 前置条件

- 在 `smart-module` 下使用已有 Maven 依赖缓存。
- 不连接生产 Oracle，也不执行发布 SQL。

## 自动化验证

```bash
cd /Users/lvtu/.codex/worktrees/51a3/smart/smart-module
mvn -pl smart-platform/smart-platform-core -am test -Dtest=SmtSecurityAreaSupplierSoftDeleteMapperXmlTest -Dsurefire.failIfNoSpecifiedTests=false
```

预期：测试验证两实体的逻辑删除值、供应商/人员各读取 SQL 的失效过滤，以及订单详情对失效供应商的隐藏规则。

## 发布前数据库核对（由受权数据库负责人执行）

1. 只读核对两张目标表是否已有 `DEL_FLAG` 字段、列默认值及空值数量。
2. 在生产快照或变更窗口预演正向脚本，确认历史行均为 `0`。
3. 用实际统计信息取得供应商树、人员分页和通知查询的执行计划；仅在证据表明需要时决定索引策略。
4. 应用发布后，用一条无在册人员的供应商验证单条/批量删除、树、选择器、人员页、导出和通知候选均不显示失效数据。
5. PDA/扫码提示不在本次验收范围。
