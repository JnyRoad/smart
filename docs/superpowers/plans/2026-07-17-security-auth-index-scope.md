# Security Auth Index Scope Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 防止保密区权限迁移的 ISC 专用索引因历史任务的非空状态字段而覆盖整张任务表。

**Architecture:** 保留来源、申请单/意图键和批次的索引前缀，删除尾部 `STATUS`。查询继续先按来源定位小集合，再在 SQL 中过滤状态；历史任务的来源字段均为空，不写入新索引。

**Tech Stack:** Oracle SQL、JUnit 4、Maven。

## Global Constraints

- 不改业务代码、实体或 Mapper 查询。
- 不改 `BATCH_ID` 兼容逻辑和 rollback 语义。
- 新增与修改的注释使用中文。
- 先测试失败，再修改迁移 SQL。

---

### Task 1: Restrict ISC security indexes to source keys

**Files:**

- Modify: `smart-module/smart-platform/smart-platform-core/src/test/java/com/tce/smart/platform/core/mapper/SecurityAuthDispatchBatchModelTest.java`
- Modify: `smart-module/database/manual/2026-07-16-security-auth-dispatch-batch.sql`

- [ ] **Step 1: Write the failing test**

断言迁移中的两个 ISC 专用索引分别为 `(SOURCE_TYPE, SOURCE_ID, BATCH_ID)` 和 `(SOURCE_TYPE, INTENT_KEY)`，且索引定义不包含 `STATUS`。

- [ ] **Step 2: Run the focused test to verify RED**

Run: `mvn -pl smart-platform/smart-platform-core -am test -Dtest=SecurityAuthDispatchBatchModelTest -Dsurefire.failIfNoSpecifiedTests=false`

Expected: FAIL，因为当前两个索引定义仍含 `STATUS`。

- [ ] **Step 3: Apply the minimal migration change**

移除两个 `CREATE INDEX` 语句的尾部 `STATUS`，保留索引名称、幂等检测和 rollback 删除逻辑。

- [ ] **Step 4: Run focused and related regression tests**

Run: `mvn -pl smart-platform/smart-platform-core -am test -Dtest=SecurityAuthDispatchBatchModelTest,SmtIscDeviceTaskMapperXmlTest -Dsurefire.failIfNoSpecifiedTests=false`

Expected: PASS。

- [ ] **Step 5: Commit and open a GitHub PR**

提交迁移与测试，推送 `feat/security-auth-latest-dispatch` 到 `origin`，创建面向 `main` 的正式 PR。
