# Task 2：202 命令与批次受理报告

## 状态

已完成实现、聚焦回归与独立复审；可提交。

## RED / GREEN 证据

### RED

先新增 `SmtSecurityAuthApplyDispatchControllerTest` 与 `SecurityAuthDispatchAcceptanceTest`，执行：

```bash
cd smart-module
mvn -pl smart-platform/smart-platform-biz -am test \
  -Dtest=SmtSecurityAuthApplyDispatchControllerTest,SecurityAuthDispatchAcceptanceTest \
  -Dsurefire.failIfNoSpecifiedTests=false
```

预期在 test-compile 失败：`cannot find symbol SecurityDispatchAcceptedVO`；新增“受理人数去重”用例后，预期失败：`cannot find symbol countDispatchPeople(long,long)`。

### GREEN

实现后执行：

```bash
cd smart-module
mvn -pl smart-platform/smart-platform-biz -am test \
  -Dtest=SmtSecurityAuthApplyDispatchControllerTest,SecurityAuthDispatchAcceptanceTest,SecurityAuthManualDownTest,SmtSecurityAuthApplyClaimTest,SecurityAuthOaReconcileTest,SecurityAuthApplyCallbackHandlerTest \
  -Dsurefire.failIfNoSpecifiedTests=false
```

结果：`Tests run: 36, Failures: 0, Errors: 0, Skipped: 0`，`BUILD SUCCESS`。

独立复审曾指出两项阻断：空明细被错误置为 `SUCCESS`，以及旧批次进度可能覆盖新批次。已分别改为保持主单状态、和以 `id + currentDispatchBatchId` 条件更新，并补充回归测试；复审结论为 `APPROVE`。

后续验收发现：全部明细已经 `SUCCESS` 后再次受理仍会生成并持久化空批次。新增 `acceptDispatch_whenAllDetailsAreAlreadySuccessful_reusesCurrentSuccessfulBatch` 后，GREEN 前稳定失败（期望 `9004`、实际为新生成批次）；现改为返回既有 `currentDispatchBatchId`、`acceptedCount=0`，且断言不执行主单更新，不产生空批次。

复审继续发现主单仍为 `IN_WORK`、但旧批次明细已全部成功且尚未调用进度聚合接口时，上述保护失效。该回归用例再次 RED（期望 `9004`、实际为新批次）；现改为在申请单行锁内查询旧 `currentDispatchBatchId` 的明细，只有明细非空且全部 `SUCCESS` 才复用旧批次，完全不依赖主单状态。

## 变更

- 新增 POST `/security/auth/apply/{id}/dispatch`，返回 HTTP 202 与 `batchId`、去重受理人数、接管数（Task 2 固定 0）；保留旧 GET 兼容入口并返回相同 202 语义。
- 事务内 `SELECT ... FOR UPDATE` 锁申请单，只允许 OA 已通过；以新批次重绑所有非成功明细为 `WAIT + DISPATCH_BATCH_ID`，成功明细不重发。
- 当当前批次明细非空且全部成功时，重复受理复用既有成功批次并返回零受理人数，不依赖主单聚合状态，也不持久化空新批次。
- 新增按 `id + WAIT + dispatchBatchId` 的原子领取条件，旧批次无法领取；受理路径不调用 `updatePersonCard`、ISC 或旧 ISC 任务接管。
- 增加当前批次进度 VO/接口，并按当前批次明细聚合主表为 `IN_WORK`、`SUCCESS` 或 `FAIL`。
- 兼容回调和 OA 对账入口使用显式事务模板，避免内部调用绕过申请单行锁。

## 风险

- Task 3 必须在同一事务中实现旧 ISC 任务接管、实际 ISC 创建与真实 ISC 状态回写；当前 `takeoverCount=0` 是刻意边界，不能解释为已接管。
- 无 Oracle 集成环境；Oracle `COUNT(DISTINCT STAFF_ID)` 与 `FOR UPDATE` 仅通过 Mapper 编译、单元测试和现有 Oracle 方言约定验证，发布前应在目标库演练。
