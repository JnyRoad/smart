# Staff Auth Single-Table Takeover Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Restore the original security-application dispatch path and make employee permission reissue cancel only obsolete local ISC tasks in `SMT_ISC_DEVICE_TASK` before creating new tasks.

**Architecture:** Revert the PR #143 security-application batch/202/Feign/scheduler behavior to the parent implementation while retaining ordinary approval and dispatch. Add one shared employee-task cancellation operation that updates rows in the existing ISC task table from executable failure states to status `4`; existing task creation and existing scheduler submission remain unchanged.

**Tech Stack:** Java 8, Spring Boot 2.1, MyBatis-Plus, Oracle, Maven, Vue 2, pnpm.

## Global Constraints

- Only employee access-control permission operations are changed; visitors, vehicles, admission applications and temporary access remain untouched.
- Do not create a new table, new API, new Feign call, new scheduler, batch header, batch item, or new configuration switch.
- Preserve existing HTTP `200` responses, UI routes, task creation and ISC scheduler behavior.
- `SMT_ISC_DEVICE_TASK` is the only task record. Reissue must update old tasks; it must not physically delete task history.
- For the same employee card task scope, statuses `0`, `2`, and `6` become `4`; statuses `1`, `3`, `4`, and `5` remain unchanged. Do not call ISC cancellation.
- PR #143 source/batch fields and indexes are restored only through an explicit database rollback SQL after its read-only precheck succeeds. `BATCH_ID` is not dropped.
- All newly added or changed comments use Chinese. No logs may contain tokens, face data, or raw ISC responses with personal data.

---

### Task 1: Restore the security-application dispatch path

**Files:**
- Modify: `smart-module/smart-platform/smart-platform-api/src/main/java/com/tce/smart/platform/api/feign/securityzone/RemoteSecurityAuthService.java`
- Modify: `smart-module/smart-platform/smart-platform-biz/src/main/java/com/tce/smart/platform/controller/securityzone/SmtSecurityAuthApplyController.java`
- Modify: `smart-module/smart-platform/smart-platform-biz/src/main/java/com/tce/smart/platform/service/securityzone/SmtSecurityAuthApplyService.java`
- Modify: `smart-module/smart-platform/smart-platform-biz/src/main/java/com/tce/smart/platform/service/securityzone/impl/SmtSecurityAuthApplyServiceImpl.java`
- Modify: `smart-module/smart-schedule/src/main/java/com/tce/smart/schedule/config/TaskJob.java`
- Modify: `smart-module/smart-schedule/src/main/java/com/tce/smart/schedule/task/PlatformTimerTask.java`
- Modify: `smart-ui/src/router/axios.js`
- Modify: `smart-ui/src/views/platform/security_area/xc_guard_apply/index.vue`
- Modify: `smart-ui/src/views/platform/security_area/xc_guard_apply/detail.vue`
- Delete: PR #143-only DTO, VO, context, mapper methods, dedicated tests, runbook and plan files after checking they have no remaining caller.
- Test: `smart-module/smart-platform/smart-platform-biz/src/test/java/com/tce/smart/platform/controller/securityzone/SmtSecurityAuthApplyDispatchControllerTest.java`
- Test: `smart-ui/src/router/axios.test.js`

**Interfaces:**
- Produces the pre-PR security dispatch response contract from parent `cbb5bfeb`; it does not return HTTP `202` or a batch identifier.
- Removes `RemoteSecurityAuthService.processDispatch` and all callers before deleting the scheduler switch.

- [ ] **Step 1: Write failing regression tests for restored non-202 dispatch behavior.**

```java
@Test
public void dispatchMustUseTheOriginalSuccessResponse() throws Exception {
    mockMvc.perform(post("/security/auth/apply/100/dispatch"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.code").value(0))
        .andExpect(jsonPath("$.data.batchId").doesNotExist());
}
```

```javascript
it('keeps the original successful response handling', () => {
  expect(isSuccessStatus(200)).toBe(true)
  expect(isSuccessStatus(202)).toBe(false)
})
```

- [ ] **Step 2: Run the focused tests and confirm PR #143 behavior fails them.**

Run: `rtk mvn -pl smart-module/smart-platform/smart-platform-biz -Dtest=SmtSecurityAuthApplyDispatchControllerTest test && rtk pnpm --dir smart-ui test -- axios.test.js`

Expected: FAIL because the current dispatch route accepts asynchronously and the Axios layer treats every 2xx as success.

- [ ] **Step 3: Revert only PR #143 implementation files to parent `cbb5bfeb`, then retain unrelated post-PR fixes only if they do not use batch/source fields.**

```bash
rtk git diff --name-only cbb5bfeb 9b19cb16 -- smart-module/smart-platform smart-module/smart-schedule smart-ui
rtk git diff cbb5bfeb 9b19cb16 -- smart-module/smart-platform/smart-platform-biz/src/main/java/com/tce/smart/platform/service/securityzone
```

Use `git show cbb5bfeb:<path>` as the source of the original implementation. Do not reset the branch, because Task 2 follows on this branch.

- [ ] **Step 4: Remove PR-only scheduler and internal-call wiring after its callers are gone.**

```java
// PlatformTimerTask must not contain a securityAuthDispatchProcess method.
// TaskJob must not expose securityAuthDispatchProcess.
```

- [ ] **Step 5: Run platform, scheduler, and frontend focused tests.**

Run: `rtk mvn -pl smart-module/smart-platform/smart-platform-biz -Dtest=SmtSecurityAuthApplyDispatchControllerTest test && rtk mvn -pl smart-module/smart-schedule -Dtest=PlatformTimerTaskSecurityDispatchTest test && rtk pnpm --dir smart-ui test -- axios.test.js`

Expected: PASS after obsolete test classes are replaced or removed; no source file contains `security-auth-dispatch-process`, `processDispatch`, `SecurityDispatchAcceptedVO`, or `SecurityAuthDispatchContext`.

- [ ] **Step 6: Commit the recovered dispatch path.**

```bash
rtk git add smart-module smart-ui
rtk git commit -m "revert(platform): remove security auth batch dispatch"
```

### Task 2: Cancel obsolete employee ISC tasks in place before reissue

**Files:**
- Modify: `smart-module/smart-platform/smart-platform-biz/src/main/java/com/tce/smart/platform/service/impl/SmtStaffDeviceAuthServiceImpl.java`
- Modify: `smart-module/smart-platform/smart-platform-core/src/main/java/com/tce/smart/platform/core/service/SmtIscDeviceTaskService.java`
- Modify: `smart-module/smart-platform/smart-platform-core/src/main/java/com/tce/smart/platform/core/service/impl/SmtIscDeviceTaskServiceImpl.java`
- Test: `smart-module/smart-platform/smart-platform-biz/src/test/java/com/tce/smart/platform/service/impl/SmtStaffDeviceAuthServiceImplTest.java`
- Test: `smart-module/smart-platform/smart-platform-core/src/test/java/com/tce/smart/platform/core/service/impl/SmtIscDeviceTaskServiceImplTest.java`

**Interfaces:**
- Produces `int cancelSupersededStaffAuthTasks(String staffId)`.
- The operation updates rows whose `CARD_NO`, `DEVICE_TYPE`, and `SERVICE_TYPE` match the existing employee cleanup predicate and whose status is `0`, `2`, or `6`.

- [ ] **Step 1: Write failing tests for status-only cancellation.**

```java
@Test
public void reissueCancelsOnlyExecutableOrRetryableEmployeeIscTasks() {
    createTask("8055883", DeviceTaskStatusEnum.INIT.getCode());
    createTask("8055883", DeviceTaskStatusEnum.FAIL.getCode());
    createTask("8055883", DeviceTaskStatusEnum.DEVICE_OFFLINE.getCode());
    createTask("8055883", DeviceTaskStatusEnum.DOING.getCode());
    createVisitorTask(DeviceTaskStatusEnum.INIT.getCode());

    service.cancelSupersededStaffAuthTasks("8055883");

    assertStatuses(4, 4, 4, 3);
    assertThat(visitorTaskStatus(), is(DeviceTaskStatusEnum.INIT.getCode()));
    assertThat(taskCount(), is(5));
}
```

- [ ] **Step 2: Run focused tests and verify current physical deletion fails the history assertion.**

Run: `rtk mvn -pl smart-module/smart-platform/smart-platform-biz -Dtest=SmtStaffDeviceAuthServiceImplTest test && rtk mvn -pl smart-module/smart-platform/smart-platform-core -Dtest=SmtIscDeviceTaskServiceImplTest test`

Expected: FAIL because existing reissue cleanup removes non-DOING ISC tasks.

- [ ] **Step 3: Add one shared status update using existing task columns and enum codes.**

```java
public int cancelSupersededStaffAuthTasks(String staffId) {
    return baseMapper.cancelSupersededStaffAuthTasks(
        staffId,
        DeviceTaskStatusEnum.CANCEL.getCode(),
        "已被重新下发权限替代",
        Arrays.asList(DeviceTaskStatusEnum.INIT.getCode(), DeviceTaskStatusEnum.FAIL.getCode(), DeviceTaskStatusEnum.DEVICE_OFFLINE.getCode()));
}
```

```java
// SmtStaffDeviceAuthServiceImpl：在每个员工新任务创建前调用，替代 ISC 任务 remove(queryWrapper)。
smtIscDeviceTaskService.cancelSupersededStaffAuthTasks(staffId);
```

- [ ] **Step 4: Route all existing employee triggers through the same method without changing their public response.**

Use `updateAuthNew`, `updateAuth`, and `applyAuthDiff` as the only employee entry methods. Do not call the method from visitor, vehicle, or admission services.

- [ ] **Step 5: Run focused and module tests.**

Run: `rtk mvn -pl smart-module/smart-platform/smart-platform-biz -Dtest=SmtStaffDeviceAuthServiceImplTest test && rtk mvn -pl smart-module/smart-platform/smart-platform-core -Dtest=SmtIscDeviceTaskServiceImplTest test`

Expected: PASS; task rows remain, statuses are `4`, and no ISC cancellation client is invoked.

- [ ] **Step 6: Commit the single-table takeover.**

```bash
rtk git add smart-module/smart-platform/smart-platform-biz smart-module/smart-platform/smart-platform-core
rtk git commit -m "fix(platform): cancel superseded staff auth tasks"
```

### Task 3: Provide database rollback and release verification without automatic execution

**Files:**
- Modify: `smart-module/database/manual/2026-07-16-security-auth-dispatch-batch-rollback.sql`
- Create: `docs/runbooks/security-auth-dispatch-rollback.md`
- Test: `smart-module/smart-platform/smart-platform-core/src/test/java/com/tce/smart/platform/core/mapper/SmtIscDeviceTaskMapperXmlTest.java`

**Interfaces:**
- Produces an idempotent Oracle rollback script that aborts before any `DROP` statement if prechecks find non-null PR #143 fields or active security-dispatch tasks.
- Does not drop `SMT_ISC_DEVICE_TASK.BATCH_ID`.

- [ ] **Step 1: Write a failing static SQL test for mandatory prechecks and protected BATCH_ID.**

```java
@Test
public void rollbackMustCheckForLiveDataAndKeepBatchId() {
    String sql = readRollbackSql();
    assertThat(sql, containsString("RAISE_APPLICATION_ERROR"));
    assertThat(sql, containsString("SOURCE_TYPE IS NOT NULL"));
    assertThat(sql, containsString("CURRENT_DISPATCH_BATCH_ID IS NOT NULL"));
    assertThat(sql, not(containsString("DROP COLUMN BATCH_ID")));
}
```

- [ ] **Step 2: Run the focused test and confirm the old unconditional rollback fails it.**

Run: `rtk mvn -pl smart-module/smart-platform/smart-platform-core -Dtest=SmtIscDeviceTaskMapperXmlTest test`

Expected: FAIL because the old script drops fields without aborting on live data.

- [ ] **Step 3: Add read-only prechecks before every drop.**

```sql
SELECT COUNT(1) INTO V_COUNT FROM SMT_ISC_DEVICE_TASK
WHERE SOURCE_TYPE IS NOT NULL OR SOURCE_ID IS NOT NULL OR SOURCE_DETAIL_ID IS NOT NULL OR INTENT_KEY IS NOT NULL;
IF V_COUNT > 0 THEN
    RAISE_APPLICATION_ERROR(-20001, '存在 PR #143 来源任务数据，禁止删列');
END IF;
```

Also check non-null values in both security tables and active `SECURITY_AUTH` tasks before dropping indexes or columns.

- [ ] **Step 4: Write the runbook with read-only precheck, code deployment, postcheck, and rollback order.**

The runbook must require: deploy code that no longer reads the fields; restart platform/schedule services; verify security applications dispatch normally; execute the rollback SQL only after all prechecks return zero; confirm columns and indexes are absent with `USER_TAB_COLUMNS` and `USER_INDEXES`.

- [ ] **Step 5: Run SQL/static checks and full affected builds.**

Run: `rtk mvn -pl smart-module/smart-platform/smart-platform-core -Dtest=SmtIscDeviceTaskMapperXmlTest test && rtk mvn -pl smart-module/smart-platform/smart-platform-biz,smart-module/smart-schedule -am package -DskipTests && rtk pnpm --dir smart-ui build`

Expected: PASS; no command executes SQL against a database.

- [ ] **Step 6: Commit the operational rollback package.**

```bash
rtk git add smart-module/database/manual/2026-07-16-security-auth-dispatch-batch-rollback.sql docs/runbooks/security-auth-dispatch-rollback.md smart-module/smart-platform/smart-platform-core/src/test
rtk git commit -m "docs(platform): safeguard security dispatch rollback"
```

## Final Verification

- [ ] `rtk git diff origin/main HEAD --check` exits 0.
- [ ] Search finds no remaining PR #143 runtime contract: `rtk rg -n 'security-auth-dispatch-process|processDispatch|SecurityDispatchAcceptedVO|SecurityAuthDispatchContext' smart-module smart-ui` returns no production code results.
- [ ] Employee reissue tests prove rows are cancelled rather than removed and that `DOING` tasks remain intact.
- [ ] Database rollback script is reviewed but not executed by the development workflow.
