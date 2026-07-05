# Task 2 Report: OA Callback Log Audit Infrastructure

**Status:** DONE  
**Branch:** feat/oa-callback-handler-isolation  
**Commit:** 387a5a47 feat(platform): add oa_callback_log table, entity and audit service  

---

## Execution Summary

All 6 steps completed successfully per TDD (RED → GREEN → REFACTOR → COMMIT) flow:

### Step 1: SQL Table Creation ✓
Created `smart-module/database/manual/oa_callback_log.sql` with:
- `oa_callback_log` table with 11 columns (id, request_id, payload, receive_time, status, resolved, succeeded_handlers, failed_handlers, last_error, retry_count, cost_ms)
- Composite index `idx_oa_cb_req` on (request_id, status, resolved, receive_time, id) for efficient unresolved-partial queries
- Unique index `ux_oa_cb_unresolved` to enforce at-most-one unresolved partial per request_id (database-layer invariant)
- Status enum values: 0=RECEIVED, 1=SUCCESS, 2=PARTIAL_FAIL
- Resolved enum values: 0=NO, 1=YES

### Step 2: Test-First (RED) ✓
Created `OaCallbackLogServiceImplTest.java` with 2 test cases:
- `saveReceived_success_returnsId()` — verifies successful insertion returns entity ID
- `saveReceived_insertThrows_returnsNullNotThrow()` — verifies exception swallowing (audit failure must not block business logic)

### Step 3: Confirmed Compilation Failure ✓
```
[ERROR] 找不到符号 (Symbol not found)
  符号:   类 OaCallbackLog
  位置: 程序包 com.tce.smart.platform.core.entity
```
Tests fail to compile → GREEN phase can proceed.

### Step 4: Implementation (GREEN) ✓

**Entity (`OaCallbackLog.java`)**
- Extends `Model<OaCallbackLog>` (MyBatis-Plus active record pattern used throughout platform-core)
- `@Data @EqualsAndHashCode(callSuper=true) @TableName("oa_callback_log")`
- Status constants: `STATUS_RECEIVED=0`, `STATUS_SUCCESS=1`, `STATUS_PARTIAL_FAIL=2`
- Resolved constants: `RESOLVED_NO=0`, `RESOLVED_YES=1`
- All fields mapped from table schema (LocalDateTime for receive_time per Java 8+ standard)

**Mapper (`OaCallbackLogMapper.java`)**
- Extends `BaseMapper<OaCallbackLog>` — standard MyBatis-Plus interface
- No custom XML; all CRUD via generator base

**Service Interface (`OaCallbackLogService.java`)**
- Extends `IService<OaCallbackLog>` (MyBatis-Plus standard service contract)
- `Long saveReceived(String requestId, String payload)` — saves received callback, returns ID or null on error
- `OaCallbackLog findLatestUnresolved(String requestId)` — queries latest unresolved partial for given request_id

**Service Implementation (`OaCallbackLogServiceImpl.java`)**
- `@Slf4j @Service` (Spring stereotype)
- `saveReceived()` decorated with `@Transactional(propagation=Propagation.REQUIRES_NEW)` to isolate audit logic
  - Creates entity with status=RECEIVED, resolved=NO, retryCount=0
  - Catches all exceptions, logs error, returns null (spec §3.3: audit failure ≠ business failure)
- `findLatestUnresolved()` constructs lambda-query with:
  - `.eq(requestId, ...)`
  - `.eq(status, STATUS_PARTIAL_FAIL=2)`
  - `.eq(resolved, RESOLVED_NO=0)`
  - `.orderByDesc(receiveTime).orderByDesc(id)`
  - Returns first or null

### Step 5: Test Verification (GREEN) ✓
```
[INFO] Tests run: 2, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
```
Both tests PASS after implementing entity, mapper, and service.

### Step 6: Commit ✓
```
387a5a47 feat(platform): add oa_callback_log table, entity and audit service
```
Files staged and committed with Conventional Commits format.

---

## Self-Review vs. Brief

**SQL**
- ✓ Table columns match spec exactly (all 11 fields present)
- ✓ Indices created: composite `idx_oa_cb_req` + unique `ux_oa_cb_unresolved`
- ✓ Comments on table in Chinese per project standard
- ✓ Default values set (status=0, resolved=0, retry_count=0)

**Entity**
- ✓ Extends `Model<OaCallbackLog>` (matches SmtSecurityAuthApply pattern in same package)
- ✓ `@EqualsAndHashCode(callSuper=true)` correctly chained to parent
- ✓ Status/resolved constants defined as static public ints
- ✓ Field types match schema: LocalDateTime for receive_time, Integer for status/resolved/retryCount, Long for id/costMs
- ✓ Comments in Chinese

**Mapper**
- ✓ Extends `BaseMapper<OaCallbackLog>` (standard platform pattern)
- ✓ No XML required (MyBatis-Plus auto-handles CRUD)
- ✓ Verified @MapperScan coverage: platform-biz startup class scans `com.tce.smart.platform.core.mapper`

**Service**
- ✓ Interface extends `IService<OaCallbackLog>` (MyBatis-Plus contract)
- ✓ `saveReceived()` signature: `Long saveReceived(String requestId, String payload)`
  - Decorated: `@Transactional(propagation=Propagation.REQUIRES_NEW)`
  - Catches Exception → logs → returns null (spec §3.3)
- ✓ `findLatestUnresolved()` signature: `OaCallbackLog findLatestUnresolved(String requestId)`
  - Query filters: requestId, status=2, resolved=0
  - Order: receiveTime DESC, id DESC (spec §3.2.2)
  - Returns first or null

**Tests**
- ✓ Both test cases present and passing
- ✓ Mocks mapper.insert()
- ✓ Exception swallowing verified
- ✓ JUnit4 + Mockito

---

## Files Changed

| File | Status | Lines |
|------|--------|-------|
| `smart-module/database/manual/oa_callback_log.sql` | NEW | 21 |
| `smart-module/smart-platform/smart-platform-core/src/main/java/com/tce/smart/platform/core/entity/OaCallbackLog.java` | NEW | 56 |
| `smart-module/smart-platform/smart-platform-core/src/main/java/com/tce/smart/platform/core/mapper/OaCallbackLogMapper.java` | NEW | 5 |
| `smart-module/smart-platform/smart-platform-biz/src/main/java/com/tce/smart/platform/service/oacallback/OaCallbackLogService.java` | NEW | 20 |
| `smart-module/smart-platform/smart-platform-biz/src/main/java/com/tce/smart/platform/service/oacallback/impl/OaCallbackLogServiceImpl.java` | NEW | 61 |
| `smart-module/smart-platform/smart-platform-biz/src/test/java/com/tce/smart/platform/service/oacallback/OaCallbackLogServiceImplTest.java` | NEW | 45 |

**Total:** 6 files created, 208 lines of code + SQL schema.

---

## Verification Checklist

- [x] SQL file in correct location: `smart-module/database/manual/`
- [x] Entity in correct package: `com.tce.smart.platform.core.entity`
- [x] Entity extends MyBatis-Plus `Model<T>` (active record pattern)
- [x] Mapper in correct package: `com.tce.smart.platform.core.mapper`
- [x] Service interface/impl in correct packages
- [x] Service impl extends `ServiceImpl<Mapper, Entity>`
- [x] `@Transactional(propagation=Propagation.REQUIRES_NEW)` on saveReceived
- [x] Exception swallowing with logging in saveReceived
- [x] Test class in correct path
- [x] Tests use JUnit4 + Mockito
- [x] 2 test cases present and PASSING
- [x] Comments in Chinese per project standard
- [x] Commit message in English, Conventional Commits format
- [x] All files staged and committed
- [x] Maven build successful with tests passing

---

## Concerns

None. Implementation adheres to brief spec, project patterns (MyBatis-Plus Model/ServiceImpl, Spring Boot 2.1), and test requirements. Exception handling ensures audit failures don't cascade to business logic (spec §3.3).

---

## Fix 追加

**Issue:** `OaCallbackLog.id` 字段缺少 `@TableId` 注解，导致 `OaCallbackLogServiceImpl.saveReceived` 插入后 `entity.getId()` 返回 null。

**Fix (8ecd5a77):** 
- 在 `id` 字段添加 `@TableId(value = "id", type = IdType.ID_WORKER)` 与对应 imports（`com.baomidou.mybatisplus.annotation.IdType`、`com.baomidou.mybatisplus.annotation.TableId`）
- 补注释：`/** 主键：MyBatis-Plus 雪花 ID（Oracle 无自增） */`
- 风格与同包 `SmtSecurityAuthApply.java` 一致

**Test Verification:**
```
cd smart-module && mvn -pl smart-platform/smart-platform-biz test -Dtest=OaCallbackLogServiceImplTest
→ Tests run: 2, Failures: 0, Errors: 0, Skipped: 0 — BUILD SUCCESS
```

**File Changed:**
| File | Change |
|------|--------|
| `smart-module/smart-platform/smart-platform-core/src/main/java/com/tce/smart/platform/core/entity/OaCallbackLog.java` | +4 lines (imports + @TableId + comment) |

