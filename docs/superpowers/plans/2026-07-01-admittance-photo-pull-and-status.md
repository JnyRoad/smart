# 入厂申请照片拉取与下发状态回写实施计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 照片分发从审批链路同步推送改为 FileReceiver 定时拉取（含过期清理），`deviceStatus` 由 smart-schedule 按 ISC 任务终态以批次模型聚合回写，消除「ISC 成功却显示下发失败」的误报。

**Architecture:** smart-platform 新增两个 `@OpenApi` 照片接口（园区范围从 token claim 推导）；`updateStatus()` 照片推送降级为开关控制的尽力而为；新增批次模型（`smt_isc_device_task.apply_id/batch_id`、`smt_admittance_apply.isc_submit_batch`）承载提交协议、补偿边界、聚合回写；FileReceiver 升级为 OAuth2 拉取客户端。

**Tech Stack:** Java 8、Spring Boot、MyBatis-Plus、Hutool HttpUtil、Redis、Vitest 不涉及（H5 不改）。

**依据 spec：** `docs/superpowers/specs/2026-07-01-admittance-photo-pull-and-status-design.md`（已三轮 Codex 评审定稿）
**依赖：** 开放 API 鉴权计划（`2026-07-01-open-api-auth.md`）Task 1-5 已合并。

## Global Constraints

- 批次/聚合/取消**只覆盖 ISC 任务表**（`smt_isc_device_task`）；非 ISC 任务沿用现状（spec §3.4 边界）
- 聚合只回写终态（1/2），在途不回写；除 SUCCESS 外的所有任务终态按失败计
- 人员任一设备成功=该人成功；全员成功=单成功；任一人全败=单失败
- 照片落盘目录/命名 `D:\visitor\{photoId}.png` 不可变（b-PAC 打印硬约定）
- DDL/DML 脚本放 `smart-module/database/manual/`，含回滚
- 注释一律中文；commit message 英文 Conventional Commits
- 禁止吞错：所有失败路径必须有 ERROR/WARN 日志

---

### Task 1: 数据库批次模型 DDL

**Files:**
- Create: `smart-module/database/manual/2026-07-01-isc-batch-model.sql`
- Create: `smart-module/database/manual/2026-07-01-isc-batch-model-rollback.sql`

- [ ] **Step 1: 写 DDL 与回滚**

```sql
-- 2026-07-01-isc-batch-model.sql
-- 批次模型：任务归属申请单与批次；申请单记录最近一次完成提交的批次号
ALTER TABLE smt_isc_device_task
  ADD COLUMN apply_id BIGINT NULL COMMENT '入厂申请单ID（非入厂申请来源为NULL）',
  ADD COLUMN batch_id BIGINT NULL COMMENT '下发批次号（同一次提交的任务集共享）';
CREATE INDEX idx_isc_task_apply_batch ON smt_isc_device_task (apply_id, batch_id);

ALTER TABLE smt_admittance_apply
  ADD COLUMN isc_submit_batch BIGINT NULL COMMENT '最近一次成功提交ISC的批次号；NULL=从未完成提交（补偿边界依据）';
```

```sql
-- 2026-07-01-isc-batch-model-rollback.sql
DROP INDEX idx_isc_task_apply_batch ON smt_isc_device_task;
ALTER TABLE smt_isc_device_task DROP COLUMN apply_id, DROP COLUMN batch_id;
ALTER TABLE smt_admittance_apply DROP COLUMN isc_submit_batch;
```

- [ ] **Step 2: Commit** `git commit -m "chore(db): add isc batch model columns for admittance dispatch"`

---

### Task 2: 实体与 Mapper 字段

**Files:**
- Modify: `smart-module/smart-platform/smart-platform-core/src/main/java/com/tce/smart/platform/core/entity/SmtIscDeviceTask.java`（+`applyId`、`batchId`）
- Modify: `smart-module/smart-platform/smart-platform-core/src/main/java/com/tce/smart/platform/core/entity/admittance/SmtAdmittanceApply.java`（+`iscSubmitBatch`）

**Interfaces:**
- Produces: `SmtIscDeviceTask.getApplyId()/getBatchId()`、`SmtAdmittanceApply.getIscSubmitBatch()`，后续任务全部依赖这三个字段名。

- [ ] **Step 1:** 两个实体各加字段（`private Long applyId;` 等，MyBatis-Plus 驼峰映射无需注解），中文注释说明语义。
- [ ] **Step 2:** `mvn -pl smart-module/smart-platform/smart-platform-core -am package -DskipTests` 编译通过后 Commit `git commit -m "feat(platform): add batch model fields to isc task and admittance apply entities"`

---

### Task 3: updateStatus 解耦 + 批次提交协议

**Files:**
- Modify: `smart-module/smart-platform/smart-platform-biz/src/main/java/com/tce/smart/platform/service/admittance/impl/SmtAdmittanceApplyServiceImpl.java`（`updateStatus` :581-618、`smbPutPhoto` :1479-1525、`addDeviceTask` 及其下游建任务处）
- Test: `smart-module/smart-platform/smart-platform-biz/src/test/java/com/tce/smart/platform/service/admittance/impl/SmtAdmittanceApplyServiceImplTest.java`

**Interfaces:**
- Consumes: Task 2 字段。
- Produces: `updateStatus` 新协议——非车辆审批通过时：生成 `batchId = IdWorker.getId()` → **同一事务**内创建该批次全部 ISC 任务（每个任务写 `applyId/batchId`）并 `UPDATE smt_admittance_apply SET isc_submit_batch=#{batchId}` → 置 `deviceStatus=已下发(4)`；照片推送在事务外、受 `spring.admittance.photo-push-enabled`（`@Value`，默认 true）控制且**失败只记 ERROR 不抛异常不改状态**。

- [ ] **Step 1: 写失败单测**：

```java
@Test public void updateStatus_photoPushFailure_doesNotFailApply() {
    // 开关开、smbPutPhoto 返回 false：deviceStatus 仍为 ALRAEDY(4)，不抛异常
}
@Test public void updateStatus_writesSubmitBatchAtomicallyWithTasks() {
    // 断言任务插入与 isc_submit_batch 更新同事务（spy 事务模板/顺序）且 batchId 一致
}
@Test public void updateStatus_pushDisabled_skipsPhotoPush() { }
@Test public void updateStatus_taskCreationFailure_throwsAndLeavesSubmitBatchNull() { }
```

- [ ] **Step 2: 跑测确认失败**：`mvn -pl smart-module/smart-platform/smart-platform-biz -am test -Dtest=SmtAdmittanceApplyServiceImplTest`
- [ ] **Step 3: 实现**：
  - 抽私有方法 `submitIscBatch(SmtAdmittanceApply apply)`：`@Transactional`（或 `TransactionTemplate`）包裹「建任务集 + 写 isc_submit_batch」；建任务链路把 `applyId/batchId` 传入 `DeviceTaskVO`→`smtIscDeviceTaskService.saveTask`（非 ISC 路由分支忽略这两个字段，维持现状）；
  - `updateStatus` 中原 `if (!smbPutPhoto(...)) throw ...` 替换为：

```java
// 照片推送为过渡期尽力而为行为：失败不影响权限下发状态（照片由 FileReceiver 拉取兜底）
if (photoPushEnabled) {
    try {
        if (!Boolean.TRUE.equals(this.smbPutPhoto(apply.getId()))) {
            log.error("【入厂申请照片推送】推送失败（不影响下发状态，等待客户端拉取），id={}", apply.getId());
        }
    } catch (Exception e) {
        log.error("【入厂申请照片推送】推送异常（不影响下发状态，等待客户端拉取），id={}", apply.getId(), e);
    }
}
```

  - 修正 `smbPutPhoto` 内成功日志 tag：`【入厂申请上传照片到远程电脑失败】上传图片成功` → `【入厂申请照片推送】上传图片成功`；推送 `filePath` 改传相对文件名 `fileName`（配合 FileReceiver `upload-root`，spec 推拉并行口径）。
- [ ] **Step 4: 跑测通过**，Expected: PASS。
- [ ] **Step 5: Commit** `git commit -m "feat(platform): decouple photo push from approval flow and add atomic isc batch submit"`

---

### Task 4: 补偿边界收紧

**Files:**
- Modify: `SmtAdmittanceApplyServiceImpl.java` 的 `failedPostApprovalPage`（:1949-1959）与 `failedPostApprovalCursorPage`（:1961-1977）
- Test: 同 Task 3 测试类

- [ ] **Step 1: 失败单测**：`compensation_skipsAppliesWithSubmittedBatch()`——构造 `deviceStatus=FAIL` 且 `isc_submit_batch` 非空的单，断言不进补偿分页。
- [ ] **Step 2: 实现**：两个查询各追加 `.isNull(SmtAdmittanceApply::getIscSubmitBatch)`，并加中文注释「聚合产生的真失败单必有批次号，只走人工重新下发（spec §3.4 补偿边界）」。
- [ ] **Step 3: 测试通过 → Commit** `git commit -m "fix(platform): compensation only claims applies that never completed isc submit"`

---

### Task 5: 重新下发批次化

**Files:**
- Modify: `SmtAdmittanceApplyServiceImpl.repeatVisitorDeviceAuth`（:1118 起）
- Test: 同 Task 3 测试类

**Interfaces:**
- Produces: 重发协议——旧批次（`apply.iscSubmitBatch`）非终态任务批量置 `CANCEL` → 调 Task 3 的 `submitIscBatch` 建新批次（新 batchId 覆盖 `isc_submit_batch`）→ `deviceStatus=已下发(4)`。

- [ ] **Step 1: 失败单测**：`repeatAuth_cancelsOldBatchAndCreatesNew()`（旧批次在途任务变 CANCEL、新批次任务生成、`isc_submit_batch` 更新为新值）；`repeatAuth_oldTerminalTasksUntouched()`。
- [ ] **Step 2: 实现**（取消旧批次用一条 UPDATE：`status IN (INIT, DOING) AND apply_id=? AND batch_id=?` → `CANCEL`）。
- [ ] **Step 3: 测试通过 → Commit** `git commit -m "feat(platform): batch-scoped re-dispatch closing stale isc tasks"`

---

### Task 6: ISC 终态聚合回写（smart-schedule）

**Files:**
- Modify: `smart-module/smart-schedule/src/main/java/com/tce/smart/schedule/service/platform/impl/ISCDeviceTaskServiceImpl.java`（终态落库点：`handleTaskResult`/`handleTaskResultBatch`/`markTaskFailureBatch`/`markTimedOutTaskResultBatch` 等，统一在任务状态写为终态后调用聚合）
- Create: `smart-module/smart-schedule/src/main/java/com/tce/smart/schedule/service/platform/impl/AdmittanceDispatchAggregator.java`
- Test: `smart-module/smart-schedule/src/test/java/com/tce/smart/schedule/service/platform/impl/AdmittanceDispatchAggregatorTest.java`

**Interfaces:**
- Consumes: Task 2 字段；`SmtAdmittanceApplyMapper`（schedule 已引入）。
- Produces: `AdmittanceDispatchAggregator.aggregate(Long applyId)`——读 `apply.iscSubmitBatch`，查该批次全部任务，按人（任务的 cardNo=fellowId 分组）聚合，规则见全局约束；终态才回写 `device_status`（成功 1 / 失败 2），回写失败立即重试 2 次后 ERROR。

- [ ] **Step 1: 失败单测（纯函数矩阵 + 回写行为）**：

```java
// 判定纯函数 verdict(Map<Long/*fellowId*/, List<Integer/*taskStatus*/>>) 的矩阵：
@Test public void singlePerson_oneDeviceSuccess_othersFailed_success() { }
@Test public void multiPerson_onePersonAllTerminalFailed_fail() { }
@Test public void anyInFlightAndNoPersonAllFailed_inProgress_noWriteback() { }
@Test public void cancelExpiredOffline_allCountAsFailure() { }   // 终态全集
@Test public void personWithoutIscTasks_excludedFromVerdict() { } // 非ISC边界
@Test public void oldBatchTasks_ignored() { }                     // 批次过滤
@Test public void writeback_retriesTwiceThenLogsError() { }
```

- [ ] **Step 2: 跑测失败** `mvn -pl smart-module/smart-schedule -am test -Dtest=AdmittanceDispatchAggregatorTest`
- [ ] **Step 3: 实现**：判定逻辑为纯函数（终态集合=`DeviceTaskStatusEnum` 中除 SUCCESS/INIT/DOING 外全部 + FAIL；以枚举实际取值为准写成具名常量集合）；聚合器在每个任务终态落库点后被调用（`task.getApplyId()!=null` 才触发）；一次 SQL 取批次任务（`SELECT card_no,status FROM smt_isc_device_task WHERE apply_id=? AND batch_id=?`）。
- [ ] **Step 4: 跑测通过。**
- [ ] **Step 5: Commit** `git commit -m "feat(schedule): aggregate isc terminal results into admittance device status by batch"`

---

### Task 7: 自查接口过渡态映射

**Files:**
- Modify: `smart-module/smart-platform/smart-platform-biz/src/main/java/com/tce/smart/platform/service/admittance/impl/VisitorSelfQueryServiceImpl.java`（:451-459 映射处）
- Test: `smart-module/smart-platform/smart-platform-biz/src/test/java/com/tce/smart/platform/service/admittance/impl/VisitorSelfQueryServiceImplTest.java`

- [ ] **Step 1: 失败单测**：`deviceStatusAlready_mapsToIssuing()`（4→`ISSUING`）、`deviceStatusSuccess_mapsToSuccess()`（1→`SUCCESS` 不变）。
- [ ] **Step 2: 实现**：映射分支中把 `ALRAEDY(4)` 从 SUCCESS 组挪到 ISSUING 组（H5 端 `DispatchStatus` 已有 `ISSUING`，前端零改动）。
- [ ] **Step 3: 测试通过 → Commit** `git commit -m "fix(platform): map transitional ALRAEDY device status to ISSUING in self query"`

---

### Task 8: 照片开放接口

**Files:**
- Create: `smart-module/smart-platform/smart-platform-biz/src/main/java/com/tce/smart/platform/controller/admittance/AdmittancePhotoOpenController.java`
- Create: `smart-module/smart-platform/smart-platform-biz/src/main/java/com/tce/smart/platform/service/admittance/AdmittancePhotoOpenService.java`（接口+实现同包 impl）
- Test: `smart-module/smart-platform/smart-platform-biz/src/test/java/com/tce/smart/platform/service/admittance/impl/AdmittancePhotoOpenServiceImplTest.java`

**Interfaces:**
- Consumes: 鉴权计划 Task 3 的 `@OpenApi` 与 `OpenApiAuthenticationAdapter.appParkIds(auth)`；`smtImageService.getImageBinaryByCode`。
- Produces:
  - `GET /platform/admittance/photo/pending` → `Result<List<String>>`（photoId 列表），`@OpenApi("open:admittance:photo:read")`，园区范围=token 的 `app_park_ids`（空列表→返回空，不查库）；
  - `GET /platform/admittance/photo/download/{photoId}` → `ResponseEntity<byte[]>`（image/png），同 scope；photoId 必须匹配 `^[0-9a-fA-F-]{32,36}$`，否则 400；无图 404。

- [ ] **Step 1: 失败单测**：pending 过滤条件（Status_0、endTime>now、非 CAR、园区 in app_park_ids、photoId 非空且图片存在）；download 的 UUID 校验 400、缺图 404；appParkIds 空 → 空列表。
- [ ] **Step 2: 实现**（pending 查询 join `smt_admittance_fellow`，只取 `fellow_photo_id IS NOT NULL AND fellow_photo_id != ''`；图片存在性以 `getImageBinaryByCode` 惰性校验放 download，pending 只过滤空值——避免清单接口逐张读库，KISS）。

> 注意与 spec §3.1 的差异：spec 要求清单「只返回图片实际存在的 photoId」；实现上逐张校验代价高，折衷为清单过滤空值、download 对缺图 404 且 FileReceiver 对 404 跳过不重试循环（Task 9 配合）。此差异已在 spec 允许的错误处理框架内（缺图=WARN 数据质量问题）。

- [ ] **Step 3: 测试通过 → Commit** `git commit -m "feat(platform): open api endpoints for admittance photo pull"`

---

### Task 9: FileReceiver 拉取客户端 + 过期清理

**Files:**
- Create: `smart-module/FileReceiver/src/main/java/com/example/demo/pull/PhotoPullProperties.java`
- Create: `smart-module/FileReceiver/src/main/java/com/example/demo/pull/OpenApiTokenClient.java`
- Create: `smart-module/FileReceiver/src/main/java/com/example/demo/pull/PhotoPullTask.java`
- Create: `smart-module/FileReceiver/src/main/java/com/example/demo/pull/PhotoCleanupTask.java`
- Modify: `smart-module/FileReceiver/src/main/java/com/example/demo/controller/FileController.java`（/upload 加废弃 WARN 日志）
- Modify: `smart-module/FileReceiver/src/main/resources/application.yml`、`README.md`
- Test: `smart-module/FileReceiver/src/test/java/com/example/demo/pull/PhotoPullTaskTest.java`、`PhotoCleanupTaskTest.java`

**Interfaces:**
- Consumes: Task 8 两接口；auth `/oauth/token`。
- Produces: 配置键（spec §3.2）：`file-receiver.pull.enabled/server-url/app-id/app-secret/interval-seconds(默认30)`、`file-receiver.photo-dir(默认 D:/visitor)`、`file-receiver.cleanup.retention-days(默认7，0=关闭)`。

- [ ] **Step 1: 失败单测**：
  - `pullTask_downloadsOnlyMissingPhotos()`（本地已有的跳过）；
  - `pullTask_writesTmpThenAtomicRename()`（目录中不出现半成品 `.png`；断言先 `{id}.png.tmp` 后 `Files.move(..., ATOMIC_MOVE)`）；
  - `pullTask_singleFailureDoesNotAbortRound()`；`pullTask_404Skipped()`；
  - `tokenClient_refreshesOn401Once()`；
  - `cleanupTask_deletesOnlyStaleAndNotPending()`（双条件）；`cleanupTask_skippedWhenPendingFetchFails()`；`cleanupTask_disabledWhenRetentionZero()`。
- [ ] **Step 2: 跑测失败**：`cd smart-module/FileReceiver && mvn test`
- [ ] **Step 3: 实现要点**：
  - `OpenApiTokenClient`：内存缓存 token 与过期时间（提前 60s 刷新）；`fetch()` 用 Hutool `HttpUtil.createPost(serverUrl + "/oauth/token")` Basic 认证（app-id/app-secret），超时 5s；
  - `PhotoPullTask`：`@Scheduled(fixedDelayString = "${file-receiver.pull.interval-seconds:30}000")`，`enabled=false` 直接 return；流程=取 token → GET pending（读超时 30s）→ diff 本地目录 → 逐张 download（读超时 30s）→ 临时文件+原子改名；401 刷新 token 重试本轮一次；所有异常按张隔离，ERROR 日志含 photoId；
  - `PhotoCleanupTask`：`@Scheduled(cron = "0 0 3 * * ?")`，双条件删除（`mtime < now-retentionDays` 且 不在最新 pending 清单），pending 拉取失败跳过并 WARN；
  - `/upload` 首行加 `log.warn("【已废弃】/file/upload 推送接口将在下个版本移除，请迁移到拉取模式")`；README 更新部署与配置说明。
- [ ] **Step 4: 跑测通过。**
- [ ] **Step 5: Commit** `git commit -m "feat(file-receiver): oauth2 photo pull with atomic writes and retention cleanup"`

---

### Task 10: Nacos 配置与上线材料

**Files:**
- Modify: `docker/nacos/config/dev/smart-platform.yml`（`spring.admittance.photo-push-enabled: true`；`spring.admittance.save-path` 改相对目录口径与 Task 3 对齐）
- Create: `docs/superpowers/plans/2026-07-01-photo-pull-rollout-checklist.md`

- [ ] **Step 1: 配置项落 dev 配置**（生产 Nacos 由运维按 checklist 同步）。
- [ ] **Step 2: 写上线 checklist**（照 spec §3.5 四步展开：合并顺序、`file-receiver-xc` 重置 secret、**核对许昌机器现役 FileReceiver jar 版本与 upload-root**、DDL 低峰执行、观察 1-2 天的日志关键字清单、关推送开关、回退手段=开关回 true + 回滚 SQL）。
- [ ] **Step 3: Commit** `git commit -m "chore(config): photo push switch and rollout checklist"`

---

### Task 11: 现场验收（许昌）

- [ ] 提测试申请 → 审批通过 → 30s 内照片落盘 `D:\visitor` → b-PAC 打印正常；
- [ ] 页面/H5 状态跟随 ISC 结果：全成功→「下发成功」；模拟单人全设备失败→「下发失败」且不被自动补偿重建（观察日志）；「重新下发」后旧批次任务变 CANCEL；
- [ ] 拔网线 5 分钟恢复 → 缺失照片自动补齐；
- [ ] 清理任务：放置超期且不在 pending 的测试文件被删除，pending 中的不删；
- [ ] 全部通过后按 checklist 关闭推送开关，观察 1-2 天。

## Self-Review 结论

- spec §3.1→T8、§3.2→T9、§3.3→T3(+T10 开关)、§3.4→T1/T2/T4/T5/T6、展示映射→T7、§3.5→T10/T11、§5 测试→各任务 TDD 步骤 + T11；
- 类型一致性：`applyId/batchId/iscSubmitBatch` 命名在 T2 定义，T3-T6 统一引用；`submitIscBatch` 在 T3 定义、T5 复用；
- 与 spec 的一处显式折衷（pending 不逐张校验图片存在）已在 T8 标注理由与配套措施。
