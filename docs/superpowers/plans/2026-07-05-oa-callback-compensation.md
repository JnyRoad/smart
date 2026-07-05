# OA 回调补偿与监听器隔离改造 — 实施计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 按已定稿 spec（`docs/superpowers/specs/2026-07-04-oa-callback-compensation-design.md` v6）实现：OA 回调 Handler 化隔离 + 回调报文落库/重放（PR1），保密门禁申请 OA 拉取对账 + 手动下发加固（PR2）。

**Architecture:** PR1 把 `LeaveApplicationListener` 的 12 个业务分支原样搬迁为独立 `OaWorkflowCallbackHandler`，由 `OaCallbackDispatcher` 在 request_id 级 Redis 锁内循环调用（独立 try/catch、独立事务），报文落 `oa_callback_log`，部分失败向 OA 返回显式 HTTP 500 并支持按 logId 重放。PR2 为保密门禁申请增加每 2 分钟的 OA 拉取对账（`CURRENTNODETYPE` 判终态 + CAS 抢占 + 明细级原子抢占），并重写手动下发。

**Tech Stack:** Java 8、Spring Boot 2.1、MyBatis-Plus、Oracle、Redis（StringRedisTemplate）、OpenFeign、JUnit4 + Mockito、hutool。

## Global Constraints

- 所有新增/修改代码注释一律中文（含 JavaDoc、SQL、测试注释）。
- 提交遵循 Conventional Commits，message 英文，`<type>(scope): <summary>`。
- 测试框架：JUnit4 + Mockito 纯单测（不起 Spring 上下文），参考 `SmtAdmittanceApplyServiceImplTest` 写法；controller 用 MockMvc `standaloneSetup`。
- 编译/测试命令在 `smart-module/` 目录执行：`mvn -pl smart-platform/smart-platform-biz -am test -Dtest=<TestClass> -DfailIfNoTests=false`。
- 锁 TTL 常量 `LOCK_TTL_SECONDS = 600`；硬校验断言 `LOCK_TTL_SECONDS > HANDLER_COUNT(12) × MAX_HANDLER_SECONDS(30) = 360`（单测强制，见 Task 19）。
- 行为等价原则：PR1 中除"转发加超时、失败返回 500、日志占位符修复"外，12 个业务 handler 逻辑逐行等价搬迁，不做任何"顺手优化"。
- spec 实现层微调（已有结论，不再讨论）：锁 helper 落 `smart-platform-biz`（smart-tool 无 Redis 依赖，硬引会扩散依赖；类设计为无业务耦合的独立组件，将来上移 smart-common 成本为零）；保密门禁 handler 接 claim 的行为变更放 PR2（PR1 保持全量行为等价，降低回归面）。
- 禁止直推 main；两个 PR 分别走功能分支。

## 文件结构总览

**PR1 新增：**
```
smart-module/smart-platform/smart-platform-biz/src/main/java/com/tce/smart/platform/
├── support/RedisMutexLock.java                          # request_id 级互斥锁（token+setIfAbsent+Lua）
├── service/oacallback/
│   ├── OaWorkflowCallbackHandler.java                   # handler 接口
│   ├── OaCallbackDispatcher.java                        # 分发器（锁/跳过集合/落库/回写/转发）
│   ├── DispatchResult.java                              # 分发聚合结果
│   ├── ProcessRecordItem.java                           # 过程记录归一化 DTO
│   ├── ProcessRecordWriter.java                         # 判重写入组件（收敛重复）
│   ├── OaFlowRecordSupport.java                         # 标准"回退判断+记录循环"复用组件
│   ├── OaCallbackLogService.java / impl/OaCallbackLogServiceImpl.java
│   └── handler/                                         # 12 个业务 handler
│       ├── LeaveApplicationCallbackHandler.java         # 离职
│       ├── AskLeaveCallbackHandler.java                 # 请假
│       ├── OvertimeCallbackHandler.java                 # 加班
│       ├── ReplaceCardCallbackHandler.java              # 补卡
│       ├── BreakoffCallbackHandler.java                 # 调休
│       ├── OutDormitoryCallbackHandler.java             # 外宿
│       ├── CallowanceCancelCallbackHandler.java         # 外宿补贴撤销
│       ├── SecurityAreaOrderCallbackHandler.java        # 保密区预约
│       ├── SecurityAuthApplyCallbackHandler.java        # 保密区权限申请（门禁）
│       ├── AdmittanceApplyCallbackHandler.java          # 入厂申请
│       ├── HfVisitorCallbackHandler.java                # HF 访客
│       └── ArticlesReleaseCallbackHandler.java          # 物品放行
smart-module/smart-platform/smart-platform-core/src/main/java/com/tce/smart/platform/core/
├── entity/OaCallbackLog.java
└── mapper/OaCallbackLogMapper.java
smart-module/database/manual/oa_callback_log.sql         # 建表 + 索引 + 函数唯一索引
```

**PR2 新增/修改：**
```
smart-module/smart-platform/smart-platform-biz/.../service/oacallback/OaFinalStatusResolver.java   # 终态解析
smart-module/smart-platform/smart-platform-biz/.../service/securityzone/impl/SmtSecurityAuthApplyServiceImpl.java  # claim/对账/下发修正/手动下发
smart-module/smart-platform/smart-platform-biz/.../service/securityzone/impl/SmtSecurityTaskDetailsServiceImpl.java # 明细级抢占
smart-module/smart-platform/smart-platform-api/.../feign/securityzone/RemoteSecurityAuthService.java # +updateOaStatusTask
smart-module/smart-schedule/.../task/PlatformTimerTask.java + config/TaskJob.java
smart-module/smart-tool/.../enums/TimerTaskEnum.java     # +SECURITY_AUTH_UPDATE_OA
docs/superpowers/runbooks/oa-callback-runbook.md         # 巡检 SQL/重放/上线 SOP
```

---

# Phase 1（PR1，分支 `feat/oa-callback-handler-isolation`）

### Task 1: RedisMutexLock 互斥锁组件

**Files:**
- Create: `smart-module/smart-platform/smart-platform-biz/src/main/java/com/tce/smart/platform/support/RedisMutexLock.java`
- Test: `smart-module/smart-platform/smart-platform-biz/src/test/java/com/tce/smart/platform/support/RedisMutexLockTest.java`

**Interfaces:**
- Produces: `String acquire(String key, long ttlSeconds)`（成功返回随机 token，失败返回 null）；`void release(String key, String token)`（Lua 原子释放，token 不匹配不删）。

- [ ] **Step 1: 写失败测试**

```java
package com.tce.smart.platform.support;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.script.RedisScript;

import java.util.Collections;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/** RedisMutexLock 单测：验证抢占与原子释放语义 */
public class RedisMutexLockTest {

	private StringRedisTemplate redisTemplate;
	private ValueOperations<String, String> valueOps;
	private RedisMutexLock lock;

	@Before
	@SuppressWarnings("unchecked")
	public void setUp() {
		redisTemplate = mock(StringRedisTemplate.class);
		valueOps = mock(ValueOperations.class);
		when(redisTemplate.opsForValue()).thenReturn(valueOps);
		lock = new RedisMutexLock(redisTemplate);
	}

	@Test
	public void acquire_success_returnsToken() {
		// setIfAbsent 成功 → 返回非空 token
		when(valueOps.setIfAbsent(eq("k"), anyString(), eq(600L), eq(TimeUnit.SECONDS))).thenReturn(true);
		String token = lock.acquire("k", 600);
		assertNotNull(token);
	}

	@Test
	public void acquire_held_returnsNull() {
		// 已被占用 → 返回 null
		when(valueOps.setIfAbsent(eq("k"), anyString(), eq(600L), eq(TimeUnit.SECONDS))).thenReturn(false);
		assertNull(lock.acquire("k", 600));
	}

	@Test
	public void release_executesLuaWithToken() {
		// 释放走 Lua 脚本，key/token 原样传入
		lock.release("k", "t1");
		verify(redisTemplate).execute(any(RedisScript.class), eq(Collections.singletonList("k")), eq("t1"));
	}

	@Test
	public void release_nullToken_noop() {
		// token 为空直接返回，不触发 Redis 调用
		lock.release("k", null);
		verify(redisTemplate, never()).execute(any(RedisScript.class), anyList(), any());
	}
}
```

- [ ] **Step 2: 运行确认失败**

Run: `mvn -pl smart-platform/smart-platform-biz -am test -Dtest=RedisMutexLockTest -DfailIfNoTests=false`
Expected: 编译失败（`RedisMutexLock` 不存在）。

- [ ] **Step 3: 最小实现**

```java
package com.tce.smart.platform.support;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * 通用 raw-key Redis 互斥锁（token + setIfAbsent + Lua 原子释放）。
 * 与 smart-schedule 的 ISwitchService 同模式，但不绑定 TimerTaskEnum，可按任意 key 加锁。
 * 注意：TTL 到期自动失效，调用方必须保证 TTL 大于临界区耗时上界（见 spec §3.2.2 终审 High 条目）。
 */
@Slf4j
@Component
public class RedisMutexLock {

	/** token 匹配才删除，避免误删他人锁 */
	private static final DefaultRedisScript<Long> RELEASE_SCRIPT = new DefaultRedisScript<>(
			"if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end",
			Long.class);

	private final StringRedisTemplate redisTemplate;

	public RedisMutexLock(StringRedisTemplate redisTemplate) {
		this.redisTemplate = redisTemplate;
	}

	/**
	 * 尝试抢占锁。
	 * @return 成功返回持锁 token；已被占用返回 null
	 */
	public String acquire(String key, long ttlSeconds) {
		String token = UUID.randomUUID().toString();
		Boolean ok = redisTemplate.opsForValue().setIfAbsent(key, token, ttlSeconds, TimeUnit.SECONDS);
		return Boolean.TRUE.equals(ok) ? token : null;
	}

	/** 原子释放：仅当 value 与 token 一致时删除 */
	public void release(String key, String token) {
		if (token == null || token.isEmpty()) {
			return;
		}
		try {
			redisTemplate.execute(RELEASE_SCRIPT, Collections.singletonList(key), token);
		} catch (Exception e) {
			log.error("释放互斥锁失败，等待TTL自动过期：key={}", key, e);
		}
	}
}
```

- [ ] **Step 4: 运行确认通过**

Run: `mvn -pl smart-platform/smart-platform-biz -am test -Dtest=RedisMutexLockTest -DfailIfNoTests=false`
Expected: 4 个测试 PASS。

- [ ] **Step 5: Commit**

```bash
git add smart-module/smart-platform/smart-platform-biz/src/main/java/com/tce/smart/platform/support/RedisMutexLock.java \
        smart-module/smart-platform/smart-platform-biz/src/test/java/com/tce/smart/platform/support/RedisMutexLockTest.java
git commit -m "feat(platform): add raw-key redis mutex lock with token release"
```

---

### Task 2: oa_callback_log 表 + 实体 + Mapper + Service

**Files:**
- Create: `smart-module/database/manual/oa_callback_log.sql`
- Create: `smart-module/smart-platform/smart-platform-core/src/main/java/com/tce/smart/platform/core/entity/OaCallbackLog.java`
- Create: `smart-module/smart-platform/smart-platform-core/src/main/java/com/tce/smart/platform/core/mapper/OaCallbackLogMapper.java`
- Create: `smart-module/smart-platform/smart-platform-biz/src/main/java/com/tce/smart/platform/service/oacallback/OaCallbackLogService.java`
- Create: `smart-module/smart-platform/smart-platform-biz/src/main/java/com/tce/smart/platform/service/oacallback/impl/OaCallbackLogServiceImpl.java`
- Test: `smart-module/smart-platform/smart-platform-biz/src/test/java/com/tce/smart/platform/service/oacallback/OaCallbackLogServiceImplTest.java`

**Interfaces:**
- Produces: 实体 `OaCallbackLog`（字段见下）；`OaCallbackLogService extends IService<OaCallbackLog>`，新增方法 `OaCallbackLog findLatestUnresolved(String requestId)`、`Long saveReceived(String requestId, String payload)`（`REQUIRES_NEW` 独立事务，失败返回 null 不抛出）。
- 状态常量（定义在实体内）：`STATUS_RECEIVED=0`、`STATUS_SUCCESS=1`、`STATUS_PARTIAL_FAIL=2`；`RESOLVED_NO=0`、`RESOLVED_YES=1`。

- [ ] **Step 1: 写建表 SQL**

```sql
-- OA 回调报文审计表：每次 /oa/workflow/over 回调先落库再分发（spec §3.3）
create table oa_callback_log (
    id                 number(19)     primary key,
    request_id         varchar2(64)   not null,           -- OA requestid
    payload            clob,                              -- 完整回调报文 JSON
    receive_time       date           not null,
    status             number(1)      default 0 not null, -- 0=已接收 1=处理成功 2=部分失败
    resolved           number(1)      default 0 not null, -- 0=未解决 1=已解决
    succeeded_handlers varchar2(512),                     -- 成功 handler 名逗号分隔（跳过集合，含合并值）
    failed_handlers    varchar2(512),                     -- 失败 handler 名逗号分隔
    last_error         varchar2(2000),                    -- 最后一次失败摘要
    retry_count        number(3)      default 0 not null, -- 重放次数
    cost_ms            number(10)                         -- 分发耗时毫秒
);

comment on table oa_callback_log is 'OA工作流回调审计与重放日志';

-- 未解决 partial 查询 + 排序支撑索引（spec §3.3）
create index idx_oa_cb_req on oa_callback_log (request_id, status, resolved, receive_time, id);

-- 不变量兜底：任一 request_id 至多一条未解决 partial（仅兜底日志层不变量，不防副作用重复，spec §3.2.2）
create unique index ux_oa_cb_unresolved
    on oa_callback_log (case when status = 2 and resolved = 0 then request_id end);
```

- [ ] **Step 2: 写失败测试（saveReceived 吞异常 + findLatestUnresolved 查询条件）**

```java
package com.tce.smart.platform.service.oacallback;

import com.tce.smart.platform.core.entity.OaCallbackLog;
import com.tce.smart.platform.core.mapper.OaCallbackLogMapper;
import com.tce.smart.platform.service.oacallback.impl.OaCallbackLogServiceImpl;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/** OaCallbackLogService 单测 */
public class OaCallbackLogServiceImplTest {

	private OaCallbackLogServiceImpl service;
	private OaCallbackLogMapper mapper;

	@Before
	public void setUp() {
		mapper = mock(OaCallbackLogMapper.class);
		service = new OaCallbackLogServiceImpl() {
			// ServiceImpl.save 依赖 Spring 注入的 baseMapper，这里覆写为直接走 mock mapper
			@Override
			public boolean save(OaCallbackLog entity) {
				entity.setId(9L);
				return mapper.insert(entity) > 0;
			}
		};
	}

	@Test
	public void saveReceived_success_returnsId() {
		when(mapper.insert(any())).thenReturn(1);
		Long id = service.saveReceived("28753680", "{\"requestid\":\"28753680\"}");
		assertNotNull(id);
	}

	@Test
	public void saveReceived_insertThrows_returnsNullNotThrow() {
		// 落库失败仅记日志不阻断分发（spec §3.3）
		when(mapper.insert(any())).thenThrow(new RuntimeException("db down"));
		assertNull(service.saveReceived("28753680", "{}"));
	}
}
```

- [ ] **Step 3: 运行确认失败**

Run: `mvn -pl smart-platform/smart-platform-biz -am test -Dtest=OaCallbackLogServiceImplTest -DfailIfNoTests=false`
Expected: 编译失败（类不存在）。

- [ ] **Step 4: 实现实体/Mapper/Service**

`OaCallbackLog.java`（platform-core，包 `com.tce.smart.platform.core.entity`）：

```java
package com.tce.smart.platform.core.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * OA 工作流回调审计与重放日志（spec §3.3）
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("oa_callback_log")
public class OaCallbackLog extends Model<OaCallbackLog> {

	/** 处理状态：已接收 */
	public static final int STATUS_RECEIVED = 0;
	/** 处理状态：全部成功 */
	public static final int STATUS_SUCCESS = 1;
	/** 处理状态：部分失败 */
	public static final int STATUS_PARTIAL_FAIL = 2;
	/** 未解决 */
	public static final int RESOLVED_NO = 0;
	/** 已解决 */
	public static final int RESOLVED_YES = 1;

	private Long id;
	/** OA requestid */
	private String requestId;
	/** 完整回调报文 JSON */
	private String payload;
	private LocalDateTime receiveTime;
	/** 0=已接收 1=处理成功 2=部分失败 */
	private Integer status;
	/** 0=未解决 1=已解决 */
	private Integer resolved;
	/** 成功 handler 名逗号分隔（跳过集合，含合并值） */
	private String succeededHandlers;
	/** 失败 handler 名逗号分隔 */
	private String failedHandlers;
	/** 最后一次失败摘要 */
	private String lastError;
	/** 重放次数 */
	private Integer retryCount;
	/** 分发耗时毫秒 */
	private Long costMs;
}
```

`OaCallbackLogMapper.java`（platform-core，包 `com.tce.smart.platform.core.mapper`）：

```java
package com.tce.smart.platform.core.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tce.smart.platform.core.entity.OaCallbackLog;

/** OA 回调日志 Mapper（MyBatis-Plus 通用 CRUD，无 XML） */
public interface OaCallbackLogMapper extends BaseMapper<OaCallbackLog> {
}
```

`OaCallbackLogService.java` / `OaCallbackLogServiceImpl.java`：

```java
package com.tce.smart.platform.service.oacallback;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tce.smart.platform.core.entity.OaCallbackLog;

/** OA 回调日志服务 */
public interface OaCallbackLogService extends IService<OaCallbackLog> {

	/**
	 * 回调入口先落库（独立事务，失败仅记日志不阻断分发）。
	 * @return 新记录 id；落库失败返回 null
	 */
	Long saveReceived(String requestId, String payload);

	/**
	 * 查询同 request_id 最近一条未解决 partial（status=2 and resolved=0，
	 * order by receive_time desc, id desc，spec §3.2.2）。
	 */
	OaCallbackLog findLatestUnresolved(String requestId);
}
```

```java
package com.tce.smart.platform.service.oacallback.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tce.smart.platform.core.entity.OaCallbackLog;
import com.tce.smart.platform.core.mapper.OaCallbackLogMapper;
import com.tce.smart.platform.service.oacallback.OaCallbackLogService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/** OA 回调日志服务实现 */
@Slf4j
@Service
public class OaCallbackLogServiceImpl extends ServiceImpl<OaCallbackLogMapper, OaCallbackLog> implements OaCallbackLogService {

	@Override
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public Long saveReceived(String requestId, String payload) {
		try {
			OaCallbackLog entity = new OaCallbackLog();
			entity.setRequestId(requestId);
			entity.setPayload(payload);
			entity.setReceiveTime(LocalDateTime.now());
			entity.setStatus(OaCallbackLog.STATUS_RECEIVED);
			entity.setResolved(OaCallbackLog.RESOLVED_NO);
			entity.setRetryCount(0);
			this.save(entity);
			return entity.getId();
		} catch (Exception e) {
			// 审计落库失败不能影响业务处理（spec §3.3）
			log.error("OA回调日志落库失败：requestId={}", requestId, e);
			return null;
		}
	}

	@Override
	public OaCallbackLog findLatestUnresolved(String requestId) {
		List<OaCallbackLog> list = this.list(Wrappers.<OaCallbackLog>query().lambda()
				.eq(OaCallbackLog::getRequestId, requestId)
				.eq(OaCallbackLog::getStatus, OaCallbackLog.STATUS_PARTIAL_FAIL)
				.eq(OaCallbackLog::getResolved, OaCallbackLog.RESOLVED_NO)
				.orderByDesc(OaCallbackLog::getReceiveTime)
				.orderByDesc(OaCallbackLog::getId));
		return list.isEmpty() ? null : list.get(0);
	}
}
```

- [ ] **Step 5: 运行确认通过**

Run: `mvn -pl smart-platform/smart-platform-biz -am test -Dtest=OaCallbackLogServiceImplTest -DfailIfNoTests=false`
Expected: 2 个测试 PASS。

- [ ] **Step 6: Commit**

```bash
git add smart-module/database/manual/oa_callback_log.sql \
        smart-module/smart-platform/smart-platform-core/src/main/java/com/tce/smart/platform/core/entity/OaCallbackLog.java \
        smart-module/smart-platform/smart-platform-core/src/main/java/com/tce/smart/platform/core/mapper/OaCallbackLogMapper.java \
        smart-module/smart-platform/smart-platform-biz/src/main/java/com/tce/smart/platform/service/oacallback/ \
        smart-module/smart-platform/smart-platform-biz/src/test/java/com/tce/smart/platform/service/oacallback/
git commit -m "feat(platform): add oa_callback_log table, entity and audit service"
```

---

### Task 3: ProcessRecordItem 归一化 + ProcessRecordWriter + OaFlowRecordSupport

**Files:**
- Create: `.../service/oacallback/ProcessRecordItem.java`
- Create: `.../service/oacallback/ProcessRecordWriter.java`
- Create: `.../service/oacallback/OaFlowRecordSupport.java`
- Test: `.../test/.../service/oacallback/ProcessRecordWriterTest.java`

**Interfaces:**
- Consumes: `SmtProcessRecordService`（既有）、`WorkFlowRecordAO`（回调 DTO）、`WorkFlowLogDataDTO`（OA 查询 DTO）。
- Produces:
  - `ProcessRecordItem.fromCallback(WorkFlowRecordAO)` / `ProcessRecordItem.fromOaLog(WorkFlowLogDataDTO)` 静态转换器；字段 `workcode/lastname/nodename/logtype/operatedate/operatetime/remark`（均 String）。
  - `ProcessRecordWriter.write(String processId, ProcessRecordItem item)`：判重写入 `smt_process_record`（等价搬迁 `LeaveApplicationListener.processRecord` 398-437 行 + `htmlHandle` 440-447 行逻辑）。
  - `OaFlowRecordSupport.processAndDetectReturn(String processId, List<WorkFlowRecordAO> flowRecords)`：循环写记录并返回"是否未回退"flag（等价搬迁监听器各分支的标准循环：`flag = !NodeStatusEnum.RETURN.getCode().equals(logtype)`，一旦为 false 保持 false）。

- [ ] **Step 1: 写失败测试**

```java
package com.tce.smart.platform.service.oacallback;

import com.tce.smart.platform.core.ao.WorkFlowRecordAO;
import com.tce.smart.platform.core.entity.SmtProcessRecord;
import com.tce.smart.platform.service.SmtProcessRecordService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import java.util.Arrays;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/** 过程记录归一化写入组件单测 */
public class ProcessRecordWriterTest {

	private SmtProcessRecordService recordService;
	private ProcessRecordWriter writer;
	private OaFlowRecordSupport support;

	@Before
	public void setUp() {
		recordService = mock(SmtProcessRecordService.class);
		writer = new ProcessRecordWriter(recordService);
		support = new OaFlowRecordSupport(writer);
	}

	private WorkFlowRecordAO record(String logtype) {
		WorkFlowRecordAO ao = new WorkFlowRecordAO();
		ao.setLogtype(logtype);
		ao.setWorkcode("8033365");
		ao.setLastname("测试");
		ao.setNodename("01 提交申请");
		ao.setOperatedate("2026-07-02");
		ao.setOperatetime("09:46:09");
		ao.setRemark("<p>同意</p>");
		return ao;
	}

	@Test
	public void write_newRecord_savedWithHtmlStripped() {
		// 无重复记录 → 新建，remark 去 HTML
		when(recordService.getOne(any())).thenReturn(null);
		writer.write("28753680", ProcessRecordItem.fromCallback(record("2")));
		ArgumentCaptor<SmtProcessRecord> captor = ArgumentCaptor.forClass(SmtProcessRecord.class);
		verify(recordService).save(captor.capture());
		assertEquals("同意", captor.getValue().getRemark());
		assertEquals("28753680", captor.getValue().getProcessId());
	}

	@Test
	public void write_interventionRecord_skipped() {
		// 流程干预节点（logtype=i）不写入，等价原 processRecord 首行判断
		writer.write("28753680", ProcessRecordItem.fromCallback(record("i")));
		verify(recordService, never()).save(any());
	}

	@Test
	public void processAndDetectReturn_noReturn_flagTrue() {
		boolean flag = support.processAndDetectReturn("28753680",
				Arrays.asList(record("2"), record("0")));
		assertTrue(flag);
	}

	@Test
	public void processAndDetectReturn_hasReturn_flagFalse() {
		// 任一节点 logtype=3（退回）→ flag=false，且后续节点仍写记录
		boolean flag = support.processAndDetectReturn("28753680",
				Arrays.asList(record("2"), record("3"), record("0")));
		assertFalse(flag);
	}
}
```

- [ ] **Step 2: 运行确认失败**

Run: `mvn -pl smart-platform/smart-platform-biz -am test -Dtest=ProcessRecordWriterTest -DfailIfNoTests=false`
Expected: 编译失败。

- [ ] **Step 3: 实现三个类**

`ProcessRecordItem.java`：

```java
package com.tce.smart.platform.service.oacallback;

import com.tce.smart.platform.core.ao.WorkFlowRecordAO;
import com.tce.smart.platform.core.dto.WorkFlowLogDataDTO;
import lombok.Builder;
import lombok.Data;

/**
 * 过程记录归一化 DTO：统一回调（WorkFlowRecordAO）与 OA 查询（WorkFlowLogDataDTO）两套入参（spec §3.2.4）
 */
@Data
@Builder
public class ProcessRecordItem {
	private String workcode;
	private String lastname;
	private String nodename;
	private String logtype;
	private String operatedate;
	private String operatetime;
	private String remark;

	/** 从 OA 回调记录转换 */
	public static ProcessRecordItem fromCallback(WorkFlowRecordAO ao) {
		return ProcessRecordItem.builder()
				.workcode(ao.getWorkcode()).lastname(ao.getLastname())
				.nodename(ao.getNodename()).logtype(ao.getLogtype())
				.operatedate(ao.getOperatedate()).operatetime(ao.getOperatetime())
				.remark(ao.getRemark()).build();
	}

	/** 从 OA 查询流转记录转换（注意该 DTO 字段为全大写命名） */
	public static ProcessRecordItem fromOaLog(WorkFlowLogDataDTO dto) {
		return ProcessRecordItem.builder()
				.workcode(dto.getWORKCODE()).lastname(dto.getLASTNAME())
				.nodename(dto.getNODENAME()).logtype(dto.getLOGTYPE())
				.operatedate(dto.getOPERATEDATE()).operatetime(dto.getOPERATETIME())
				.remark(dto.getREMARK()).build();
	}
}
```

`ProcessRecordWriter.java` —— 把 `LeaveApplicationListener.java` 398-437 行（processRecord）与 440-447 行（htmlHandle）**逐行等价**搬迁，仅把入参 `WorkFlowRecordAO process` 替换为 `ProcessRecordItem item`（getter 同名小写），依赖注入 `SmtProcessRecordService`：

```java
package com.tce.smart.platform.service.oacallback;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.tce.smart.platform.core.entity.SmtProcessRecord;
import com.tce.smart.platform.service.SmtProcessRecordService;
import com.tce.smart.tool.enums.NodeStatusEnum;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringEscapeUtils;
import org.springframework.stereotype.Component;

/**
 * smt_process_record 判重写入组件：收敛 LeaveApplicationListener/LeaveApplicationServiceImpl 等 6 处重复（spec §3.2.4）。
 * 逻辑与原 LeaveApplicationListener.processRecord 逐行等价，不做行为变更。
 */
@Slf4j
@Component
public class ProcessRecordWriter {

	private final SmtProcessRecordService smtProcessRecordService;

	public ProcessRecordWriter(SmtProcessRecordService smtProcessRecordService) {
		this.smtProcessRecordService = smtProcessRecordService;
	}

	/** 判重写入一条过程记录；流程干预节点（logtype=i）跳过 */
	public void write(String processId, ProcessRecordItem item) {
		if (item.getLogtype().equals(NodeStatusEnum.INTERVENTION.getCode())) {
			return;
		}
		SmtProcessRecord processRecord = smtProcessRecordService.getOne(Wrappers.<SmtProcessRecord>query().lambda()
				.eq(SmtProcessRecord::getProcessId, processId)
				.eq(SmtProcessRecord::getStaffBadge, item.getWorkcode())
				.ne(SmtProcessRecord::getStatus, NodeStatusEnum.FINISHED.getCode())
				.ne(SmtProcessRecord::getStatus, NodeStatusEnum.NOT_FINISHED.getCode()));
		// 1、判重：不存在则新建
		if (ObjectUtil.isNull(processRecord)) {
			SmtProcessRecord record = new SmtProcessRecord();
			record.setCreatTime(DateUtil.date());
			record.setNodeName(item.getNodename());
			record.setProcessId(processId);
			String dateTime = item.getOperatedate() + " " + item.getOperatetime();
			if (StrUtil.isNotBlank(item.getOperatedate()) && StrUtil.isNotBlank(item.getOperatetime())) {
				record.setRecordDate(DateUtil.parse(dateTime, "yyyy-MM-dd HH:mm:ss"));
			}
			record.setRemark(htmlHandle(item.getRemark()));
			record.setStaffBadge(item.getWorkcode());
			record.setStaffName(item.getLastname());
			record.setStatus(item.getLogtype());
			smtProcessRecordService.save(record);
		} else {
			// 已存在且当前为"等待审批"状态 → 更新为最新节点状态
			if (processRecord.getStatus().equals(NodeStatusEnum.APPROVER.getCode())) {
				SmtProcessRecord record = new SmtProcessRecord();
				record.setId(processRecord.getId());
				String dateTime = item.getOperatedate() + " " + item.getOperatetime();
				if (StrUtil.isNotBlank(item.getOperatedate()) && StrUtil.isNotBlank(item.getOperatetime())) {
					record.setRecordDate(DateUtil.parse(dateTime, "yyyy-MM-dd HH:mm:ss"));
				}
				record.setStatus(item.getLogtype());
				record.setRemark(htmlHandle(item.getRemark()));
				smtProcessRecordService.updateById(record);
			}
		}
	}

	/** 去除 HTML 标签并反转义（等价原 htmlHandle） */
	private String htmlHandle(String html) {
		if (StrUtil.isBlank(html)) {
			return "";
		}
		String txtcontent = html.replaceAll("</?[^>]+>", "");
		txtcontent = txtcontent.replaceAll("<a>\\s*|\t|\r|\n</a>", "");
		return StringEscapeUtils.unescapeHtml(txtcontent).trim();
	}
}
```

`OaFlowRecordSupport.java`：

```java
package com.tce.smart.platform.service.oacallback;

import com.tce.smart.common.core.util.CollectionUtils;
import com.tce.smart.platform.core.ao.WorkFlowRecordAO;
import com.tce.smart.tool.enums.NodeStatusEnum;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 标准"回退判断 + 过程记录循环"复用组件：等价监听器各分支的通用循环。
 * 特殊分支（外宿补贴撤销按最后节点判断、物品放行按 status 字段判断）不使用本组件，保留各自原逻辑。
 */
@Component
public class OaFlowRecordSupport {

	private final ProcessRecordWriter processRecordWriter;

	public OaFlowRecordSupport(ProcessRecordWriter processRecordWriter) {
		this.processRecordWriter = processRecordWriter;
	}

	/**
	 * 循环写入过程记录并检测回退。
	 * @return true=未回退（审批通过路径）；false=存在退回节点
	 */
	public boolean processAndDetectReturn(String processId, List<WorkFlowRecordAO> flowRecords) {
		boolean flag = true;
		if (CollectionUtils.isNotEmpty(flowRecords)) {
			for (WorkFlowRecordAO flowRecord : flowRecords) {
				if (flag) {
					flag = !NodeStatusEnum.RETURN.getCode().equals(flowRecord.getLogtype());
				}
				processRecordWriter.write(processId, ProcessRecordItem.fromCallback(flowRecord));
			}
		}
		return flag;
	}
}
```

- [ ] **Step 4: 运行确认通过**

Run: `mvn -pl smart-platform/smart-platform-biz -am test -Dtest=ProcessRecordWriterTest -DfailIfNoTests=false`
Expected: 4 个测试 PASS。

- [ ] **Step 5: Commit**

```bash
git add smart-module/smart-platform/smart-platform-biz/src/main/java/com/tce/smart/platform/service/oacallback/ \
        smart-module/smart-platform/smart-platform-biz/src/test/java/com/tce/smart/platform/service/oacallback/
git commit -m "feat(platform): add normalized process record writer and flow record support"
```

---

### Task 4: OaWorkflowCallbackHandler 接口 + OaCallbackDispatcher 分发器

**Files:**
- Create: `.../service/oacallback/OaWorkflowCallbackHandler.java`
- Create: `.../service/oacallback/DispatchResult.java`
- Create: `.../service/oacallback/OaCallbackDispatcher.java`
- Test: `.../test/.../service/oacallback/OaCallbackDispatcherTest.java`

**Interfaces:**
- Consumes: Task 1 `RedisMutexLock.acquire/release`、Task 2 `OaCallbackLogService.saveReceived/findLatestUnresolved/updateById`、`WorkFlowAO.getRequestid()/getFlowRecord()`。
- Produces:
  - `OaWorkflowCallbackHandler`：`String name()`；`void handle(String processId, WorkFlowAO ao)`。
  - `OaCallbackDispatcher.dispatch(WorkFlowAO ao)` → `DispatchResult { boolean allSuccess; Long logId; List<String> failedHandlers; }`。
  - 常量：`LOCK_KEY_PREFIX = "oa:callback:lock:"`、`LOCK_TTL_SECONDS = 600`、`LOCK_RETRY_TIMES = 3`、`LOCK_RETRY_SLEEP_MS = 2000`、`HANDLER_COUNT = 12`、`MAX_HANDLER_SECONDS = 30`、`FORWARD_TIMEOUT_MS = 5000`。

- [ ] **Step 1: 写失败测试（核心行为 8 例）**

```java
package com.tce.smart.platform.service.oacallback;

import com.tce.smart.platform.core.ao.WorkFlowAO;
import com.tce.smart.platform.core.entity.OaCallbackLog;
import com.tce.smart.platform.support.RedisMutexLock;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.dao.DuplicateKeyException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/** 分发器单测：隔离、跳过集合、partial 关闭、锁失败、唯一索引冲突 */
public class OaCallbackDispatcherTest {

	private RedisMutexLock lock;
	private OaCallbackLogService logService;
	private List<OaWorkflowCallbackHandler> handlers;
	private OaWorkflowCallbackHandler h1, h2;
	private OaCallbackDispatcher dispatcher;

	@Before
	public void setUp() {
		lock = mock(RedisMutexLock.class);
		logService = mock(OaCallbackLogService.class);
		h1 = mock(OaWorkflowCallbackHandler.class);
		when(h1.name()).thenReturn("h1");
		h2 = mock(OaWorkflowCallbackHandler.class);
		when(h2.name()).thenReturn("h2");
		handlers = Arrays.asList(h1, h2);
		when(lock.acquire(anyString(), anyLong())).thenReturn("token");
		when(logService.saveReceived(anyString(), anyString())).thenReturn(100L);
		when(logService.findLatestUnresolved(anyString())).thenReturn(null);
		dispatcher = new OaCallbackDispatcher(handlers, lock, logService);
	}

	private WorkFlowAO ao() {
		WorkFlowAO ao = new WorkFlowAO();
		ao.setRequestid("28753680");
		return ao;
	}

	@Test
	public void allSuccess_returns200Semantics_andLogStatus1() {
		DispatchResult r = dispatcher.dispatch(ao());
		assertTrue(r.isAllSuccess());
		ArgumentCaptor<OaCallbackLog> c = ArgumentCaptor.forClass(OaCallbackLog.class);
		verify(logService).updateById(c.capture());
		assertEquals(Integer.valueOf(OaCallbackLog.STATUS_SUCCESS), c.getValue().getStatus());
		assertEquals("h1,h2", c.getValue().getSucceededHandlers());
	}

	@Test
	public void oneHandlerThrows_othersStillRun_resultPartialFail() {
		// 隔离：h1 抛异常，h2 照常执行（spec §3.2.2）
		doThrow(new RuntimeException("boom")).when(h1).handle(anyString(), any());
		DispatchResult r = dispatcher.dispatch(ao());
		assertFalse(r.isAllSuccess());
		verify(h2).handle(eq("28753680"), any());
		ArgumentCaptor<OaCallbackLog> c = ArgumentCaptor.forClass(OaCallbackLog.class);
		verify(logService).updateById(c.capture());
		assertEquals(Integer.valueOf(OaCallbackLog.STATUS_PARTIAL_FAIL), c.getValue().getStatus());
		assertEquals("h1", c.getValue().getFailedHandlers());
	}

	@Test
	public void oldPartialExists_succeededHandlerSkipped_andOldResolved() {
		// 存在未解决 partial（h1 已成功）→ 只跑 h2，处理完关闭旧记录（spec §3.2.2 N1）
		OaCallbackLog old = new OaCallbackLog();
		old.setId(50L);
		old.setSucceededHandlers("h1");
		old.setStatus(OaCallbackLog.STATUS_PARTIAL_FAIL);
		old.setResolved(OaCallbackLog.RESOLVED_NO);
		when(logService.findLatestUnresolved("28753680")).thenReturn(old);
		dispatcher.dispatch(ao());
		verify(h1, never()).handle(anyString(), any());
		verify(h2).handle(anyString(), any());
		// 旧 partial 被无条件关闭
		ArgumentCaptor<OaCallbackLog> c = ArgumentCaptor.forClass(OaCallbackLog.class);
		verify(logService, times(2)).updateById(c.capture());
		OaCallbackLog closedOld = c.getAllValues().stream()
				.filter(l -> Long.valueOf(50L).equals(l.getId())).findFirst().orElse(null);
		assertNotNull(closedOld);
		assertEquals(Integer.valueOf(OaCallbackLog.RESOLVED_YES), closedOld.getResolved());
		// 新记录 succeeded 为合并值（跳过的 h1 + 本次成功的 h2）
		OaCallbackLog current = c.getAllValues().stream()
				.filter(l -> Long.valueOf(100L).equals(l.getId())).findFirst().orElse(null);
		assertEquals("h1,h2", current.getSucceededHandlers());
	}

	@Test
	public void lockExhausted_returnsFailure_noHandlerRuns() {
		// 锁重试耗尽 → 不执行任何 handler，结果失败（交给 OA 重试，spec §3.2.2）
		when(lock.acquire(anyString(), anyLong())).thenReturn(null);
		DispatchResult r = dispatcher.dispatch(ao());
		assertFalse(r.isAllSuccess());
		verify(h1, never()).handle(anyString(), any());
		verify(lock, times(3)).acquire(anyString(), anyLong());
	}

	@Test
	public void duplicateKeyOnPartialWrite_fallbackResolvedSnapshot() {
		// 唯一索引冲突（TTL 过期极端窗口）→ 落为 resolved=1 失败快照 + ERROR（spec §3.2.2）
		doThrow(new RuntimeException("boom")).when(h1).handle(anyString(), any());
		when(logService.updateById(argThat(l -> l != null
				&& Integer.valueOf(OaCallbackLog.STATUS_PARTIAL_FAIL).equals(l.getStatus())
				&& Integer.valueOf(OaCallbackLog.RESOLVED_NO).equals(l.getResolved()))))
				.thenThrow(new DuplicateKeyException("ux_oa_cb_unresolved"));
		DispatchResult r = dispatcher.dispatch(ao());
		assertFalse(r.isAllSuccess());
		// 二次回写为 resolved=1 快照
		verify(logService, atLeast(2)).updateById(any(OaCallbackLog.class));
	}

	@Test
	public void lockAlwaysReleased_evenWhenHandlerThrows() {
		doThrow(new RuntimeException("boom")).when(h1).handle(anyString(), any());
		dispatcher.dispatch(ao());
		verify(lock).release(eq("oa:callback:lock:28753680"), eq("token"));
	}

	@Test
	public void logSaveFails_processingContinues() {
		// 落库失败不阻断处理（spec §3.3）
		when(logService.saveReceived(anyString(), anyString())).thenReturn(null);
		DispatchResult r = dispatcher.dispatch(ao());
		assertTrue(r.isAllSuccess());
		verify(h1).handle(anyString(), any());
	}

	@Test
	public void ttlGreaterThanDerivedUpperBound() {
		// 硬校验：TTL 必须大于 handler 数 × 单 handler 最坏耗时（spec 终审 High）
		assertTrue(OaCallbackDispatcher.LOCK_TTL_SECONDS >
				OaCallbackDispatcher.HANDLER_COUNT * OaCallbackDispatcher.MAX_HANDLER_SECONDS);
	}
}
```

- [ ] **Step 2: 运行确认失败**

Run: `mvn -pl smart-platform/smart-platform-biz -am test -Dtest=OaCallbackDispatcherTest -DfailIfNoTests=false`
Expected: 编译失败。

- [ ] **Step 3: 实现接口与分发器**

`OaWorkflowCallbackHandler.java`：

```java
package com.tce.smart.platform.service.oacallback;

import com.tce.smart.platform.core.ao.WorkFlowAO;

/**
 * OA 工作流回调业务处理器：每个业务一个实现，内部自行按 processId 查表决定是否处理（spec §3.2.1）。
 * 实现约束：handle 内所有外部调用（HTTP/Feign）必须有显式超时，否则违反锁 TTL 上界推导前提（spec 终审 High）。
 */
public interface OaWorkflowCallbackHandler {

	/** handler 唯一名，写入 oa_callback_log 的 succeeded/failed_handlers */
	String name();

	/** 处理一次 OA 回调；未命中本业务时应快速返回 */
	void handle(String processId, WorkFlowAO ao);
}
```

`DispatchResult.java`：

```java
package com.tce.smart.platform.service.oacallback;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/** 一次回调分发的聚合结果 */
@Data
@Builder
public class DispatchResult {
	/** 全部 handler 成功（含跳过） */
	private boolean allSuccess;
	/** 本次 oa_callback_log 记录 id（落库失败为 null） */
	private Long logId;
	/** 失败 handler 名列表 */
	private List<String> failedHandlers;
}
```

`OaCallbackDispatcher.java`：

```java
package com.tce.smart.platform.service.oacallback;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONUtil;
import com.tce.smart.platform.core.ao.WorkFlowAO;
import com.tce.smart.platform.core.entity.OaCallbackLog;
import com.tce.smart.platform.support.RedisMutexLock;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * OA 回调分发器：request_id 级互斥锁内完成"查跳过集合 → 执行全部 handler → 落库/回写/关闭旧 partial"（spec §3.2.2）。
 * 单个 handler 失败不阻断其他 handler；存在失败时由 controller 返回 HTTP 500 交 OA 重试。
 */
@Slf4j
@Component
public class OaCallbackDispatcher {

	public static final String LOCK_KEY_PREFIX = "oa:callback:lock:";
	/** 锁 TTL：必须大于分发全程耗时上界（HANDLER_COUNT × MAX_HANDLER_SECONDS），见硬校验单测 */
	public static final long LOCK_TTL_SECONDS = 600;
	public static final int LOCK_RETRY_TIMES = 3;
	public static final long LOCK_RETRY_SLEEP_MS = 2000;
	public static final int HANDLER_COUNT = 12;
	/** 单 handler 最坏耗时上界（秒）：各外部调用均有显式超时的推导值 */
	public static final int MAX_HANDLER_SECONDS = 30;
	/** 大岭山转发超时（毫秒），修 D6 */
	public static final int FORWARD_TIMEOUT_MS = 5000;
	/** 由于OA系统回调地址只能配置一个，所有回调消息同步转发大岭山一份（原监听器 92 行迁移） */
	private static final String FORWARD_URL = "http://smartapp.szyuto.com:8080/platform/oa/workflow/over";

	private final List<OaWorkflowCallbackHandler> handlers;
	private final RedisMutexLock mutexLock;
	private final OaCallbackLogService logService;

	public OaCallbackDispatcher(List<OaWorkflowCallbackHandler> handlers,
			RedisMutexLock mutexLock, OaCallbackLogService logService) {
		this.handlers = handlers;
		this.mutexLock = mutexLock;
		this.logService = logService;
	}

	/** 处理一次 OA 回调（自然回调入口） */
	public DispatchResult dispatch(WorkFlowAO ao) {
		String requestId = ao.getRequestid();
		String payload = JSONUtil.toJsonStr(ao);
		log.info("收到OA审批消息：{}", payload);
		// 转发大岭山：加超时、失败仅告警，不影响本地处理与响应码（修 D6，锁外执行不占锁时长）
		forwardToDls(payload);
		// 入口先落库（独立事务，失败不阻断，spec §3.3）
		Long logId = logService.saveReceived(requestId, payload);
		// request_id 级互斥：串行化同单的自然回调 / OA 重推 / 重放（spec §3.2.2 四审 High-a/b）
		String lockKey = LOCK_KEY_PREFIX + requestId;
		String token = acquireWithRetry(lockKey);
		if (token == null) {
			log.error("OA回调获取request_id锁失败，返回500交OA重试：requestId={}", requestId);
			writeLockFailure(logId);
			return DispatchResult.builder().allSuccess(false).logId(logId)
					.failedHandlers(new ArrayList<>()).build();
		}
		long start = System.currentTimeMillis();
		try {
			OaCallbackLog oldPartial = logService.findLatestUnresolved(requestId);
			Set<String> skip = parseHandlerNames(oldPartial == null ? null : oldPartial.getSucceededHandlers());
			Set<String> succeeded = new LinkedHashSet<>(skip);
			List<String> failed = new ArrayList<>();
			String lastError = null;
			for (OaWorkflowCallbackHandler handler : handlers) {
				if (skip.contains(handler.name())) {
					continue;
				}
				try {
					handler.handle(requestId, ao);
					succeeded.add(handler.name());
				} catch (Exception e) {
					// 隔离：单业务失败不影响其他业务（spec §3.2.2）
					log.error("OA回调处理失败：requestId={}, handler={}", requestId, handler.name(), e);
					failed.add(handler.name());
					lastError = handler.name() + ": " + StrUtil.maxLength(String.valueOf(e), 500);
				}
			}
			// 关闭命中的旧 partial（无条件，spec §3.2.2 三审缺口 1）
			closeOldPartial(oldPartial);
			// 回写本次结果
			writeResult(logId, succeeded, failed, lastError, System.currentTimeMillis() - start);
			return DispatchResult.builder().allSuccess(failed.isEmpty()).logId(logId)
					.failedHandlers(failed).build();
		} finally {
			mutexLock.release(lockKey, token);
		}
	}

	private String acquireWithRetry(String lockKey) {
		for (int i = 0; i < LOCK_RETRY_TIMES; i++) {
			String token = mutexLock.acquire(lockKey, LOCK_TTL_SECONDS);
			if (token != null) {
				return token;
			}
			try {
				Thread.sleep(LOCK_RETRY_SLEEP_MS);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				return null;
			}
		}
		return null;
	}

	private void forwardToDls(String payload) {
		try {
			HttpUtil.createPost(FORWARD_URL).body(payload).timeout(FORWARD_TIMEOUT_MS).execute();
		} catch (Exception e) {
			// 转发失败不影响本地处理（spec §3.2.4），但不再静默吞掉
			log.warn("OA回调转发大岭山失败：{}", e.getMessage());
		}
	}

	private Set<String> parseHandlerNames(String joined) {
		Set<String> names = new LinkedHashSet<>();
		if (StrUtil.isNotBlank(joined)) {
			names.addAll(Arrays.asList(joined.split(",")));
		}
		return names;
	}

	private void closeOldPartial(OaCallbackLog oldPartial) {
		if (oldPartial == null) {
			return;
		}
		try {
			OaCallbackLog update = new OaCallbackLog();
			update.setId(oldPartial.getId());
			update.setResolved(OaCallbackLog.RESOLVED_YES);
			logService.updateById(update);
		} catch (Exception e) {
			log.error("关闭历史partial回调日志失败：logId={}", oldPartial.getId(), e);
		}
	}

	private void writeLockFailure(Long logId) {
		if (logId == null) {
			return;
		}
		try {
			OaCallbackLog update = new OaCallbackLog();
			update.setId(logId);
			update.setLastError("acquire request_id lock timeout");
			logService.updateById(update);
		} catch (Exception e) {
			log.error("回写锁失败信息失败：logId={}", logId, e);
		}
	}

	private void writeResult(Long logId, Set<String> succeeded, List<String> failed, String lastError, long costMs) {
		if (logId == null) {
			return;
		}
		OaCallbackLog update = new OaCallbackLog();
		update.setId(logId);
		update.setSucceededHandlers(String.join(",", succeeded));
		update.setCostMs(costMs);
		if (failed.isEmpty()) {
			update.setStatus(OaCallbackLog.STATUS_SUCCESS);
			update.setResolved(OaCallbackLog.RESOLVED_YES);
		} else {
			update.setStatus(OaCallbackLog.STATUS_PARTIAL_FAIL);
			update.setResolved(OaCallbackLog.RESOLVED_NO);
			update.setFailedHandlers(String.join(",", failed));
			update.setLastError(lastError);
		}
		try {
			logService.updateById(update);
		} catch (Exception e) {
			// 函数唯一索引冲突（TTL 过期极端窗口）：宁可失败暴露，落为 resolved=1 失败快照（spec §3.2.2）
			log.error("回写回调结果冲突（疑似唯一索引拦截第二条未解决partial），落为已解决失败快照：logId={}", logId, e);
			update.setResolved(OaCallbackLog.RESOLVED_YES);
			try {
				logService.updateById(update);
			} catch (Exception e2) {
				log.error("回写失败快照仍失败：logId={}", logId, e2);
			}
		}
	}
}
```

- [ ] **Step 4: 运行确认通过**

Run: `mvn -pl smart-platform/smart-platform-biz -am test -Dtest=OaCallbackDispatcherTest -DfailIfNoTests=false`
Expected: 8 个测试 PASS。

- [ ] **Step 5: Commit**

```bash
git add smart-module/smart-platform/smart-platform-biz/src/main/java/com/tce/smart/platform/service/oacallback/ \
        smart-module/smart-platform/smart-platform-biz/src/test/java/com/tce/smart/platform/service/oacallback/
git commit -m "feat(platform): add oa callback dispatcher with request-level mutex and audit"
```

---

### Task 5～16: 12 个业务 Handler 搬迁（每业务一个 commit）

> 统一规则：**逐行等价搬迁**，业务代码不改写；把 `LeaveApplicationListener` 对应行段的分支体移入新 handler 的 `handle` 方法；原分支的标准循环（`flag` 判断 + `processRecord` 调用）替换为 `OaFlowRecordSupport.processAndDetectReturn`（Task 3 已验证等价）；特殊分支（Task 11 外宿补贴撤销、Task 16 物品放行）保留各自原始判断逻辑，仅将 `processRecord(...)` 调用替换为 `processRecordWriter.write(processId, ProcessRecordItem.fromCallback(...))`。每个 handler 完成后：编译通过 + `OaCallbackDispatcherTest` 仍 PASS + commit。此阶段 `LeaveApplicationListener` 原文件保持不动（Task 17 统一切换删除），新 handler 与旧监听器短暂并存但**尚无调用方**，不产生双跑。

每个 Task 的步骤模板（以 Task 5 为例，Task 6-16 相同节奏）：

- [ ] Step 1: 创建 handler 类（骨架见各 Task）
- [ ] Step 2: 从 `LeaveApplicationListener.java` 指定行段剪贴分支体到 `handle`，按上述统一规则替换记录循环
- [ ] Step 3: `mvn -pl smart-platform/smart-platform-biz -am compile` 编译通过
- [ ] Step 4: `mvn -pl smart-platform/smart-platform-biz -am test -Dtest=OaCallbackDispatcherTest,ProcessRecordWriterTest -DfailIfNoTests=false` PASS
- [ ] Step 5: Commit（message 见各 Task）

**Task 5: 离职**（源 101-121 行）

```java
package com.tce.smart.platform.service.oacallback.handler;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.tce.smart.platform.core.ao.WorkFlowAO;
import com.tce.smart.platform.core.entity.SmtLeaveApplication;
import com.tce.smart.platform.core.service.SmtLeaveApplicationService;
import com.tce.smart.platform.service.ILeaveApplicationService;
import com.tce.smart.platform.service.oacallback.OaFlowRecordSupport;
import com.tce.smart.platform.service.oacallback.OaWorkflowCallbackHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/** 离职申请 OA 回调处理（原 LeaveApplicationListener 101-121 行等价搬迁） */
@Slf4j
@Component
public class LeaveApplicationCallbackHandler implements OaWorkflowCallbackHandler {

	@Autowired
	private SmtLeaveApplicationService smtLeaveApplicationService;
	@Autowired
	private ILeaveApplicationService leaveApplicationService;
	@Autowired
	private OaFlowRecordSupport flowRecordSupport;

	@Override
	public String name() {
		return "leaveApplication";
	}

	@Override
	public void handle(String processId, WorkFlowAO ao) {
		SmtLeaveApplication leaveApplication = smtLeaveApplicationService.getOne(Wrappers.<SmtLeaveApplication>query()
				.lambda().eq(SmtLeaveApplication::getProcessId, processId));
		if (ObjectUtil.isNull(leaveApplication)) {
			return;
		}
		boolean flag = flowRecordSupport.processAndDetectReturn(processId, ao.getFlowRecord());
		log.info("流程编号【{}】审批完成", processId);
		if (flag) {
			leaveApplicationService.endLeaveApplication(processId);
		} else {
			leaveApplicationService.failLeaveApplication(processId);
		}
	}
}
```

Commit: `refactor(platform): extract leave application oa callback handler`

**Task 6: 请假**（源 123-142 行）— 类 `AskLeaveCallbackHandler`，name `askLeave`，注入 `SmtAskLeaveApplicationService`；命中后 `flag ? approvalNotice(badge, APP_PUSH_7301, id) : approvalNotice(badge, APP_PUSH_7302, id)`（原文照搬）。
Commit: `refactor(platform): extract ask-leave oa callback handler`

**Task 7: 加班**（源 143-162 行）— 类 `OvertimeCallbackHandler`，name `overtime`，注入 `SmtOvertimeApplicationService` + `SmtAskLeaveApplicationService`；通知模板 `APP_PUSH_9301/9302`。
Commit: `refactor(platform): extract overtime oa callback handler`

**Task 8: 补卡**（源 163-182 行）— 类 `ReplaceCardCallbackHandler`，name `replaceCard`，注入 `SmtReplaceApplicationService` + `SmtAskLeaveApplicationService`；模板 `APP_PUSH_10301/10303`。
Commit: `refactor(platform): extract replace-card oa callback handler`

**Task 9: 调休**（源 183-203 行）— 类 `BreakoffCallbackHandler`，name `breakoff`，注入 `SmtBreakoffApplicationService` + `SmtAskLeaveApplicationService`；模板 `APP_PUSH_8301/8302`。
Commit: `refactor(platform): extract breakoff oa callback handler`

**Task 10: 外宿**（源 205-232 行）— 类 `OutDormitoryCallbackHandler`，name `outDormitory`，注入 `SmtOutDormitoryStaffService`；保留按 `allowanceType.equals("外宿补贴")` 选模板（6301/6305/6302/6306）的原分支。
Commit: `refactor(platform): extract out-dormitory oa callback handler`

**Task 11: 外宿补贴撤销**（源 233-264 行）—— **特殊分支**：类 `CallowanceCancelCallbackHandler`，name `callowanceCancel`，注入 `SmtCallowanceCancelRecordService` + `ProcessRecordWriter`（不用 Support）；原逻辑"取最后一个节点判断回退 + 全量写记录"逐行照搬（含原 239/243 行两条 log），仅把 `processRecord(processId, flowRecord)` 替换为 `processRecordWriter.write(processId, ProcessRecordItem.fromCallback(flowRecord))`。
Commit: `refactor(platform): extract callowance-cancel oa callback handler`

**Task 12: 保密区预约**（源 266-288 行）— 类 `SecurityAreaOrderCallbackHandler`，name `securityAreaOrder`，注入 `SmtSecurityAreaOrderService`；照搬原逻辑（含原 282-287 行"statusEnum 局部变量未生效、恒 update 为 PASSED"的既有问题——**等价搬迁不修**，在类注释标注 `// 注意：原逻辑无论回退与否均置 PASSED，历史行为保留，待独立需求修复`）。
Commit: `refactor(platform): extract security-area-order oa callback handler`

**Task 13: 保密区权限申请（门禁）**（源 290-314 行）— 类 `SecurityAuthApplyCallbackHandler`，name `securityAuthApply`，注入 `SmtSecurityAuthApplyService`；PR1 保持原逻辑（getByProcessId → 设 oaStatus → updateStatus），**PR2 Task 23 改接 claim**。保留原 293 行 `log.info("保密区权限申请收到OA推送【{}】",processId)`。
Commit: `refactor(platform): extract security-auth-apply oa callback handler`

**Task 14: 入厂申请**（源 316-340 行）— 类 `AdmittanceApplyCallbackHandler`，name `admittanceApply`，注入 `SmtAdmittanceApplyService`；照搬含 `VisitorStatusEnum.Status_2.equals(admittanceApply.getStatus())` 前置判断。
Commit: `refactor(platform): extract admittance-apply oa callback handler`

**Task 15: HF 访客**（源 342-366 行）— 类 `HfVisitorCallbackHandler`，name `hfVisitor`，注入 `SmtVisitorService`。
Commit: `refactor(platform): extract hf-visitor oa callback handler`

**Task 16: 物品放行**（源 368-394 行）—— **特殊分支**：类 `ArticlesReleaseCallbackHandler`，name `articlesRelease`，注入 `SmtArticlesReleaseService` + `SmtCallowanceCancelRecordService` + `ProcessRecordWriter`；回退判断用原文 `"1".equals(workFlowAO.getStatus())`，记录循环无 flag 联动（照原文全量写）。
Commit: `refactor(platform): extract articles-release oa callback handler`

---

### Task 17: Controller 切换到分发器 + 删除旧监听器 + HTTP 500 显式返回

**Files:**
- Modify: `smart-module/smart-platform/smart-platform-biz/src/main/java/com/tce/smart/platform/controller/OAWorkflowController.java`
- Modify: `smart-module/smart-platform/smart-platform-biz/src/main/java/com/tce/smart/platform/service/impl/SmtProcessRecordServiceImpl.java`（删除 `saveProcessRecord` 的事件发布与 `@Transactional`，方法保留但改为直接抛 `UnsupportedOperationException`？——否：接口 `SmtProcessRecordService.saveProcessRecord` 仅 controller 调用，直接从接口与实现中删除该方法及 `SmartEventPublisher` 依赖）
- Delete: `smart-module/smart-platform/smart-platform-biz/src/main/java/com/tce/smart/platform/service/impl/LeaveApplicationListener.java`
- Test: `.../test/.../controller/OAWorkflowControllerTest.java`

**Interfaces:**
- Consumes: Task 4 `OaCallbackDispatcher.dispatch(WorkFlowAO)` → `DispatchResult`。
- Produces: `POST /oa/workflow/over` 返回 `ResponseEntity<Result>`：全成功 HTTP 200 + `success()`；存在失败 HTTP 500（spec §3.2.2 四审 Medium：不得依赖 `Result.fail`/全局异常处理器，两者均为 HTTP 200）。

- [ ] **Step 1: 写 MockMvc 失败测试**

```java
package com.tce.smart.platform.controller;

import com.tce.smart.platform.service.IOAWorkflowService;
import com.tce.smart.platform.service.oacallback.DispatchResult;
import com.tce.smart.platform.service.oacallback.OaCallbackDispatcher;
import org.junit.Before;
import org.junit.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.http.MediaType.APPLICATION_JSON;

/** OA 回调入口：断言真实 HTTP status（spec §4，防被全局异常处理器吞成 200） */
public class OAWorkflowControllerTest {

	private OaCallbackDispatcher dispatcher;
	private MockMvc mockMvc;

	@Before
	public void setUp() {
		dispatcher = mock(OaCallbackDispatcher.class);
		OAWorkflowController controller = new OAWorkflowController(dispatcher, mock(IOAWorkflowService.class));
		mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
	}

	@Test
	public void allSuccess_http200() throws Exception {
		when(dispatcher.dispatch(any())).thenReturn(
				DispatchResult.builder().allSuccess(true).failedHandlers(Collections.emptyList()).build());
		mockMvc.perform(post("/oa/workflow/over").contentType(APPLICATION_JSON)
				.content("{\"requestid\":\"28753680\"}"))
				.andExpect(status().isOk());
	}

	@Test
	public void partialFail_http500() throws Exception {
		when(dispatcher.dispatch(any())).thenReturn(
				DispatchResult.builder().allSuccess(false)
						.failedHandlers(Collections.singletonList("askLeave")).build());
		mockMvc.perform(post("/oa/workflow/over").contentType(APPLICATION_JSON)
				.content("{\"requestid\":\"28753680\"}"))
				.andExpect(status().isInternalServerError());
	}
}
```

- [ ] **Step 2: 运行确认失败**

Run: `mvn -pl smart-platform/smart-platform-biz -am test -Dtest=OAWorkflowControllerTest -DfailIfNoTests=false`
Expected: 编译失败（controller 构造器不匹配）。

- [ ] **Step 3: 改造 controller、删监听器与事件发布**

`OAWorkflowController.java` 全量替换为：

```java
package com.tce.smart.platform.controller;

import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.core.wrapper.BaseController;
import com.tce.smart.platform.core.ao.WorkFlowAO;
import com.tce.smart.platform.service.IOAWorkflowService;
import com.tce.smart.platform.service.oacallback.DispatchResult;
import com.tce.smart.platform.service.oacallback.OaCallbackDispatcher;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * OA 服务管理 controller：接收 OA 审批回调并分发给各业务 handler。
 */
@RestController
@RequestMapping("/oa/workflow")
public class OAWorkflowController extends BaseController {

	private final OaCallbackDispatcher dispatcher;
	private final IOAWorkflowService iOAWorkflowService;

	public OAWorkflowController(OaCallbackDispatcher dispatcher, IOAWorkflowService iOAWorkflowService) {
		this.dispatcher = dispatcher;
		this.iOAWorkflowService = iOAWorkflowService;
	}

	/**
	 * 接收 OA 审核回调。
	 * 注意：存在处理失败时必须返回真实 HTTP 500（Result.fail/全局异常处理器均为 HTTP 200，
	 * 不能触发 OA 重试，spec §3.2.2 四审 Medium）。
	 */
	@PostMapping("/over")
	public ResponseEntity<Result> listen(@RequestBody WorkFlowAO workFlowAO) {
		DispatchResult result = dispatcher.dispatch(workFlowAO);
		if (result.isAllSuccess()) {
			return ResponseEntity.ok(success());
		}
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
				.body(new Result<>(false, "部分业务处理失败：" + String.join(",", result.getFailedHandlers())));
	}

	/** 根据审批编号查询 OA 审批记录（原样保留） */
	@GetMapping("/query")
	public Result query(@RequestParam("requestId") String requestId) {
		return success(iOAWorkflowService.query(requestId));
	}
}
```

> 若 `Result` 无 `(boolean, String)` 构造器，改用项目内既有失败构造方式（查看 `com.tce.smart.common.core.model.Result` 后用其 fail 工厂，但 HTTP 状态码仍由 ResponseEntity 控制）。

同步删除：`LeaveApplicationListener.java` 整个文件；`SmtProcessRecordService.saveProcessRecord` 接口方法与 `SmtProcessRecordServiceImpl` 中的实现及 `SmartEventPublisher` import/字段。全仓 grep `saveProcessRecord` 确认无其他调用方后删除。

- [ ] **Step 4: 运行确认通过 + 全模块回归**

Run: `mvn -pl smart-platform/smart-platform-biz -am test -DfailIfNoTests=false`
Expected: 新增 2 个测试 PASS，既有测试无回归。

- [ ] **Step 5: Commit**

```bash
git add -A smart-module/smart-platform/
git commit -m "refactor(platform): route oa callbacks through dispatcher, drop monolithic listener

- listen endpoint returns explicit HTTP 500 on partial handler failure
- remove synchronous SmartEvent publication and whole-request transaction
- delete LeaveApplicationListener (all 12 branches migrated to handlers)"
```

---

### Task 18: 重放接口 /oa/workflow/replay/{logId}

**Files:**
- Modify: `.../controller/OAWorkflowController.java`
- Create: `.../service/oacallback/OaCallbackReplayService.java`（接口+实现同文件包下 impl）
- Test: `.../test/.../service/oacallback/OaCallbackReplayServiceTest.java`

**Interfaces:**
- Consumes: `RedisMutexLock`、`OaCallbackLogService`、`List<OaWorkflowCallbackHandler>`、`OaCallbackDispatcher.LOCK_KEY_PREFIX/LOCK_TTL_SECONDS`。
- Produces: `OaCallbackReplayService.replay(Long logId)` → `Result`（成功/“正在处理，请稍后”/“已解决或状态不符”/“记录不存在”）；controller 端点 `POST /oa/workflow/replay/{logId}`，带 `@Inner` 注解（与项目内其他 `@Inner` 端点一致，仅 `FROM_IN` 内部调用）。

- [ ] **Step 1: 写失败测试**

```java
package com.tce.smart.platform.service.oacallback;

import cn.hutool.json.JSONUtil;
import com.tce.smart.platform.core.ao.WorkFlowAO;
import com.tce.smart.platform.core.entity.OaCallbackLog;
import com.tce.smart.platform.support.RedisMutexLock;
import com.tce.smart.platform.service.oacallback.impl.OaCallbackReplayServiceImpl;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import java.util.Arrays;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/** 重放服务单测（spec §3.3 N2 + 三审缺口 3） */
public class OaCallbackReplayServiceTest {

	private RedisMutexLock lock;
	private OaCallbackLogService logService;
	private OaWorkflowCallbackHandler h1, h2;
	private OaCallbackReplayServiceImpl service;

	@Before
	public void setUp() {
		lock = mock(RedisMutexLock.class);
		logService = mock(OaCallbackLogService.class);
		h1 = mock(OaWorkflowCallbackHandler.class);
		when(h1.name()).thenReturn("h1");
		h2 = mock(OaWorkflowCallbackHandler.class);
		when(h2.name()).thenReturn("h2");
		service = new OaCallbackReplayServiceImpl(Arrays.asList(h1, h2), lock, logService);
		when(lock.acquire(anyString(), anyLong())).thenReturn("token");
	}

	private OaCallbackLog partialLog() {
		OaCallbackLog log = new OaCallbackLog();
		log.setId(100L);
		log.setRequestId("28753680");
		WorkFlowAO ao = new WorkFlowAO();
		ao.setRequestid("28753680");
		log.setPayload(JSONUtil.toJsonStr(ao));
		log.setStatus(OaCallbackLog.STATUS_PARTIAL_FAIL);
		log.setResolved(OaCallbackLog.RESOLVED_NO);
		log.setSucceededHandlers("h1");
		log.setFailedHandlers("h2");
		log.setRetryCount(0);
		return log;
	}

	@Test
	public void replay_onlyFailedHandlerRuns_successUpdatesOriginal() {
		when(logService.getById(100L)).thenReturn(partialLog());
		// CAS 回写成功
		when(logService.update(any(), any())).thenReturn(true);
		boolean ok = service.replay(100L).isSuccess();
		assertTrue(ok);
		verify(h1, never()).handle(anyString(), any());
		verify(h2).handle(eq("28753680"), any());
	}

	@Test
	public void replay_lockHeld_rejected() {
		when(logService.getById(100L)).thenReturn(partialLog());
		when(lock.acquire(anyString(), anyLong())).thenReturn(null);
		assertFalse(service.replay(100L).isSuccess());
		verify(h2, never()).handle(anyString(), any());
	}

	@Test
	public void replay_alreadyResolved_rejected() {
		OaCallbackLog resolved = partialLog();
		resolved.setResolved(OaCallbackLog.RESOLVED_YES);
		when(logService.getById(100L)).thenReturn(resolved);
		assertFalse(service.replay(100L).isSuccess());
		verify(lock, never()).acquire(anyString(), anyLong());
	}

	@Test
	public void replay_notFound_rejected() {
		when(logService.getById(100L)).thenReturn(null);
		assertFalse(service.replay(100L).isSuccess());
	}
}
```

- [ ] **Step 2: 运行确认失败**

Run: `mvn -pl smart-platform/smart-platform-biz -am test -Dtest=OaCallbackReplayServiceTest -DfailIfNoTests=false`
Expected: 编译失败。

- [ ] **Step 3: 实现重放服务与端点**

```java
package com.tce.smart.platform.service.oacallback;

import com.tce.smart.common.core.model.Result;

/** OA 回调重放服务（spec §3.3）：按 logId 只重跑失败 handler，回写原记录 */
public interface OaCallbackReplayService {
	Result replay(Long logId);
}
```

```java
package com.tce.smart.platform.service.oacallback.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.platform.core.ao.WorkFlowAO;
import com.tce.smart.platform.core.entity.OaCallbackLog;
import com.tce.smart.platform.service.oacallback.OaCallbackDispatcher;
import com.tce.smart.platform.service.oacallback.OaCallbackLogService;
import com.tce.smart.platform.service.oacallback.OaCallbackReplayService;
import com.tce.smart.platform.service.oacallback.OaWorkflowCallbackHandler;
import com.tce.smart.platform.support.RedisMutexLock;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * 重放实现：与自然回调共用同一把 request_id 锁（spec §3.3 v5 升级），
 * 锁内校验 status=2 and resolved=0，只重跑失败 handler，CAS 回写原记录。
 */
@Slf4j
@Service
public class OaCallbackReplayServiceImpl implements OaCallbackReplayService {

	private final List<OaWorkflowCallbackHandler> handlers;
	private final RedisMutexLock mutexLock;
	private final OaCallbackLogService logService;

	public OaCallbackReplayServiceImpl(List<OaWorkflowCallbackHandler> handlers,
			RedisMutexLock mutexLock, OaCallbackLogService logService) {
		this.handlers = handlers;
		this.mutexLock = mutexLock;
		this.logService = logService;
	}

	@Override
	public Result replay(Long logId) {
		OaCallbackLog logEntity = logService.getById(logId);
		if (logEntity == null) {
			return new Result<>(false, "记录不存在");
		}
		if (!Integer.valueOf(OaCallbackLog.STATUS_PARTIAL_FAIL).equals(logEntity.getStatus())
				|| !Integer.valueOf(OaCallbackLog.RESOLVED_NO).equals(logEntity.getResolved())) {
			return new Result<>(false, "已解决或状态不符，无需重放");
		}
		String lockKey = OaCallbackDispatcher.LOCK_KEY_PREFIX + logEntity.getRequestId();
		// 重放拿不到锁不重试，直接拒绝（spec §3.3）
		String token = mutexLock.acquire(lockKey, OaCallbackDispatcher.LOCK_TTL_SECONDS);
		if (token == null) {
			return new Result<>(false, "正在处理，请稍后重试");
		}
		try {
			// 锁内二次校验，防止拿锁前状态已被自然回调改变
			logEntity = logService.getById(logId);
			if (logEntity == null
					|| !Integer.valueOf(OaCallbackLog.STATUS_PARTIAL_FAIL).equals(logEntity.getStatus())
					|| !Integer.valueOf(OaCallbackLog.RESOLVED_NO).equals(logEntity.getResolved())) {
				return new Result<>(false, "已解决或状态不符，无需重放");
			}
			WorkFlowAO ao = JSONUtil.toBean(logEntity.getPayload(), WorkFlowAO.class);
			Set<String> succeeded = new LinkedHashSet<>();
			if (StrUtil.isNotBlank(logEntity.getSucceededHandlers())) {
				succeeded.addAll(Arrays.asList(logEntity.getSucceededHandlers().split(",")));
			}
			List<String> failed = new ArrayList<>();
			String lastError = null;
			for (OaWorkflowCallbackHandler handler : handlers) {
				if (succeeded.contains(handler.name())) {
					continue;
				}
				try {
					handler.handle(logEntity.getRequestId(), ao);
					succeeded.add(handler.name());
				} catch (Exception e) {
					log.error("重放处理失败：logId={}, handler={}", logId, handler.name(), e);
					failed.add(handler.name());
					lastError = handler.name() + ": " + StrUtil.maxLength(String.valueOf(e), 500);
				}
			}
			// CAS 回写原记录（where status=2 and resolved=0），不产生新 log
			boolean allOk = failed.isEmpty();
			boolean updated = logService.update(null, Wrappers.<OaCallbackLog>update().lambda()
					.eq(OaCallbackLog::getId, logId)
					.eq(OaCallbackLog::getStatus, OaCallbackLog.STATUS_PARTIAL_FAIL)
					.eq(OaCallbackLog::getResolved, OaCallbackLog.RESOLVED_NO)
					.set(OaCallbackLog::getRetryCount, logEntity.getRetryCount() + 1)
					.set(OaCallbackLog::getSucceededHandlers, String.join(",", succeeded))
					.set(OaCallbackLog::getFailedHandlers, allOk ? null : String.join(",", failed))
					.set(OaCallbackLog::getLastError, lastError)
					.set(OaCallbackLog::getStatus, allOk ? OaCallbackLog.STATUS_SUCCESS : OaCallbackLog.STATUS_PARTIAL_FAIL)
					.set(OaCallbackLog::getResolved, allOk ? OaCallbackLog.RESOLVED_YES : OaCallbackLog.RESOLVED_NO));
			if (!updated) {
				return new Result<>(false, "回写冲突，请检查记录状态");
			}
			return allOk ? new Result<>(true, "重放成功")
					: new Result<>(false, "重放后仍有失败：" + String.join(",", failed));
		} finally {
			mutexLock.release(lockKey, token);
		}
	}
}
```

Controller 追加端点（`OAWorkflowController`）：

```java
	/** 按日志 id 重放失败的回调处理（仅内部调用，spec §3.3） */
	@com.tce.smart.common.security.annotation.Inner
	@PostMapping("/replay/{logId}")
	public Result replay(@PathVariable("logId") Long logId) {
		return replayService.replay(logId);
	}
```

（`@Inner` 注解的准确包名以项目内其他 `@Inner` 端点 import 为准，构造器注入 `OaCallbackReplayService replayService`；若 `Result` 无 `(boolean, String)` 构造器，与 Task 17 同样处理。）

- [ ] **Step 4: 运行确认通过**

Run: `mvn -pl smart-platform/smart-platform-biz -am test -Dtest=OaCallbackReplayServiceTest,OAWorkflowControllerTest -DfailIfNoTests=false`
Expected: PASS。

- [ ] **Step 5: Commit**

```bash
git add -A smart-module/smart-platform/
git commit -m "feat(platform): add inner replay endpoint for failed oa callback handlers"
```

---

### Task 19: PR1 收尾 —— 修 D7 残留 + 全量回归 + 提 PR

- [ ] Step 1: 全仓 grep 确认无残留：`grep -rn "收到OA审批消息：{}\" +" smart-module/`（Task 4 已用正确占位符）、`grep -rn "LeaveApplicationListener\|saveProcessRecord" smart-module/ --include=*.java` 应无非测试引用。
- [ ] Step 2: `mvn -pl smart-platform/smart-platform-biz -am test -DfailIfNoTests=false` 全量 PASS；`mvn clean package -DskipTests` 全模块编译通过（在 smart-module/ 下）。
- [ ] Step 3: 推分支 `feat/oa-callback-handler-isolation`，创建 PR1（title: `refactor(platform): isolate oa workflow callback handlers with audit log and replay`；body 按全局 PR 规范：Summary/Changes/Testing/Risks，Risks 注明"行为等价搬迁 + 新增审计与 500 语义，OA 侧感知变化 = 部分失败时收到 500"）。

---

# Phase 2（PR2，分支 `feat/security-auth-oa-reconciliation`，基于 PR1 合并后的 main）

### Task 20: OaFinalStatusResolver 终态解析

**Files:**
- Create: `.../service/oacallback/OaFinalStatusResolver.java`
- Test: `.../test/.../service/oacallback/OaFinalStatusResolverTest.java`

**Interfaces:**
- Consumes: `WorkFlowLogDTO.success()/getResultdata()`、`WorkFlowLogDataDTO.getCURRENTNODETYPE()/getOPERATEDATE()/getOPERATETIME()`、`OaFinalStatusEnum.CAUSE_3/CAUSE_0`（既有，入厂/HF 访客同款语义）。
- Produces: `Integer resolve(WorkFlowLogDTO dto)` → `ApproveListStateEnum.AGREE.getCode()`(1)/`REFUSE.getCode()`(2)/`null`（仍在审批/无法判定）。
- **实施前置任务（spec §3.1.2）**：编码前先用生产样本核实 `CURRENTNODETYPE` 是流程级还是记录级——执行 `curl "<OA logUrl>?requestid=28753387&TokenID=<token>"`（参数照 `OAWorkflowServiceImpl.query`，用当天已归档正常单 28753387），把结论回写 spec §3.1.2；无论结论如何，按 `OPERATEDATE+OPERATETIME` 排序取最新记录的实现两种语义下均正确。

- [ ] **Step 1: 写失败测试**（用例：归档→1；退回→2；审批中(其他值)→null；空 resultdata→null；`success()=false`→null；**乱序**记录（最新在中间）按时间排序后取对；重复时间戳用原顺序稳定兜底；退回后再提交（最新记录归档）→1）

```java
package com.tce.smart.platform.service.oacallback;

import com.tce.smart.platform.core.dto.WorkFlowLogDTO;
import com.tce.smart.platform.core.dto.WorkFlowLogDataDTO;
import com.tce.smart.tool.constant.WorkFlowLogConstants;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.*;

/** OA 终态解析单测（spec §3.1.2：排序、乱序、退回后重提、异常输入） */
public class OaFinalStatusResolverTest {

	private final OaFinalStatusResolver resolver = new OaFinalStatusResolver();

	private WorkFlowLogDataDTO rec(String nodeType, String date, String time) {
		WorkFlowLogDataDTO d = new WorkFlowLogDataDTO();
		d.setCURRENTNODETYPE(nodeType);
		d.setOPERATEDATE(date);
		d.setOPERATETIME(time);
		return d;
	}

	private WorkFlowLogDTO dto(WorkFlowLogDataDTO... records) {
		WorkFlowLogDTO dto = new WorkFlowLogDTO();
		dto.setType(WorkFlowLogConstants.SUCCESS);
		dto.setResultdata(Arrays.asList(records));
		return dto;
	}

	@Test
	public void archived_returnsAgree() {
		assertEquals(Integer.valueOf(1), resolver.resolve(dto(
				rec("1", "2026-07-02", "09:46:09"), rec("3", "2026-07-02", "09:57:36"))));
	}

	@Test
	public void returned_returnsRefuse() {
		assertEquals(Integer.valueOf(2), resolver.resolve(dto(
				rec("1", "2026-07-02", "09:46:09"), rec("0", "2026-07-02", "10:00:00"))));
	}

	@Test
	public void inProgress_returnsNull() {
		assertNull(resolver.resolve(dto(rec("1", "2026-07-02", "09:46:09"))));
	}

	@Test
	public void unordered_latestByTimeWins() {
		// 乱序：最新记录（归档）排在中间，仍应命中
		assertEquals(Integer.valueOf(1), resolver.resolve(dto(
				rec("1", "2026-07-02", "09:46:09"),
				rec("3", "2026-07-02", "09:57:36"),
				rec("1", "2026-07-02", "09:50:00"))));
	}

	@Test
	public void returnedThenResubmitted_latestArchiveWins() {
		// 退回后再提交并归档 → 以最新记录为准
		assertEquals(Integer.valueOf(1), resolver.resolve(dto(
				rec("0", "2026-07-01", "10:00:00"), rec("3", "2026-07-02", "09:57:36"))));
	}

	@Test
	public void emptyData_returnsNull() {
		WorkFlowLogDTO dto = new WorkFlowLogDTO();
		dto.setType(WorkFlowLogConstants.SUCCESS);
		dto.setResultdata(Collections.emptyList());
		assertNull(resolver.resolve(dto));
	}

	@Test
	public void queryFailed_returnsNull() {
		WorkFlowLogDTO dto = new WorkFlowLogDTO();
		dto.setType(999);
		assertNull(resolver.resolve(dto));
		assertNull(resolver.resolve(null));
	}
}
```

- [ ] **Step 2: 确认失败** → **Step 3: 实现**

```java
package com.tce.smart.platform.service.oacallback;

import cn.hutool.core.util.StrUtil;
import com.tce.smart.platform.core.dto.WorkFlowLogDTO;
import com.tce.smart.platform.core.dto.WorkFlowLogDataDTO;
import com.tce.smart.tool.enums.ApproveListStateEnum;
import com.tce.smart.tool.enums.OaFinalStatusEnum;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/**
 * OA 流程终态解析：按 OPERATEDATE+OPERATETIME 排序取最新记录的 CURRENTNODETYPE 判定（spec §3.1.2）。
 * 3=归档→通过；0=退回→拒绝；其余（审批中/空/查询失败）→ null 表示尚无终态。
 */
@Component
public class OaFinalStatusResolver {

	public Integer resolve(WorkFlowLogDTO dto) {
		if (Objects.isNull(dto) || !dto.success()) {
			return null;
		}
		List<WorkFlowLogDataDTO> data = dto.getResultdata();
		if (data == null || data.isEmpty()) {
			return null;
		}
		// 按操作时间升序排序（时间字符串可比较），时间相同保持原顺序（稳定排序），取最新一条
		WorkFlowLogDataDTO latest = data.stream()
				.sorted(Comparator.comparing(this::operateDateTime))
				.reduce((a, b) -> b).orElse(null);
		if (latest == null) {
			return null;
		}
		if (OaFinalStatusEnum.CAUSE_3.getCode().toString().equals(latest.getCURRENTNODETYPE())) {
			return ApproveListStateEnum.AGREE.getCode();
		}
		if (OaFinalStatusEnum.CAUSE_0.getCode().toString().equals(latest.getCURRENTNODETYPE())) {
			return ApproveListStateEnum.REFUSE.getCode();
		}
		return null;
	}

	/** 拼接可字典序比较的时间串；缺失时间的记录排最前（视为最旧） */
	private String operateDateTime(WorkFlowLogDataDTO d) {
		String date = StrUtil.nullToEmpty(d.getOPERATEDATE());
		String time = StrUtil.nullToEmpty(d.getOPERATETIME());
		return date + " " + time;
	}
}
```

- [ ] **Step 4: PASS 确认** → **Step 5: Commit** `feat(platform): add oa final status resolver with operate-time ordering`

---

### Task 21: claim 抢占 + 明细级原子抢占 + 下发触发修正

**Files:**
- Modify: `.../service/securityzone/SmtSecurityAuthApplyService.java`（接口 +2 方法）
- Modify: `.../service/securityzone/impl/SmtSecurityAuthApplyServiceImpl.java`
- Modify: `.../service/securityzone/SmtSecurityTaskDetailsService.java` + `impl/SmtSecurityTaskDetailsServiceImpl.java`
- Test: `.../test/.../service/securityzone/SmtSecurityAuthApplyClaimTest.java`

**Interfaces:**
- Produces:
  - `boolean claimOaFinalStatus(Long applyId, Integer finalOaStatus)`：CAS `update smt_security_auth_apply set oa_status=:final where id=:id and oa_status=0`，返回是否抢到（spec §3.1.1）。
  - `boolean triggerDownDevice(SmtSecurityAuthApply apply)`：调 `smtSecurityTaskDetailsService.downDevice(id, applyBadge)`，**成功才**置 `device_status=4`；异常记带堆栈 ERROR 并返回 false（修 D4，spec §3.1.3）。
  - `SmtSecurityTaskDetailsService.claimDetail(Long detailId)`：CAS `update smt_security_task_details set status=3 where id=:id and status=0`，`down()` 内先 claim 再下发（spec §3.1.1 明细级抢占）。
- 同步修改：原 `updateStatus(SmtSecurityAuthApply)` 重写为"若 oaStatus=AGREE 则 triggerDownDevice"，异常日志带堆栈（`log.error("保密区申请权限下发失败：applyId={}", id, e)`）。

- [ ] **Step 1: 写失败测试**（用例：claim 首次成功/二次失败（mock mapper update 返回 1/0）；triggerDownDevice 下发抛异常 → device_status 不置 4、返回 false、日志带异常对象；down() 明细 claim 失败 → 跳过该人不调 updatePersonCard）
- [ ] **Step 2: 确认失败** → **Step 3: 实现**

`SmtSecurityAuthApplyServiceImpl` 新增/修改：

```java
	@Override
	public boolean claimOaFinalStatus(Long applyId, Integer finalOaStatus) {
		// CAS 抢占终态：回调 handler 与对账任务共用，只有抢到 PENDING 的一方可触发下发（spec §3.1.1）
		return this.update(Wrappers.<SmtSecurityAuthApply>lambdaUpdate()
				.eq(SmtSecurityAuthApply::getId, applyId)
				.eq(SmtSecurityAuthApply::getOaStatus, ApproveListStateEnum.PENDING.getCode())
				.set(SmtSecurityAuthApply::getOaStatus, finalOaStatus));
	}

	@Override
	public boolean triggerDownDevice(SmtSecurityAuthApply authApply) {
		try {
			smtSecurityTaskDetailsService.downDevice(authApply.getId(), authApply.getApplyBadge());
			// 全部明细触发成功才推进主表"已触发下发"状态（修 D4，spec §3.1.3）
			this.update(Wrappers.<SmtSecurityAuthApply>lambdaUpdate()
					.eq(SmtSecurityAuthApply::getId, authApply.getId())
					.eq(SmtSecurityAuthApply::getDeviceStatus, DeviceDownStatusEnum.WAIT.getCode())
					.set(SmtSecurityAuthApply::getDeviceStatus, DeviceDownStatusEnum.ALRAEDY.getCode()));
			return true;
		} catch (Exception e) {
			// 带堆栈，保持主表 device_status 现值，由对账任务场景 2 重试（spec §3.1.3）
			log.error("保密区申请权限下发失败：applyId={}", authApply.getId(), e);
			return false;
		}
	}

	@Override
	public void updateStatus(SmtSecurityAuthApply authApply) {
		if (ApproveListStateEnum.AGREE.getCode().equals(authApply.getOaStatus())) {
			this.triggerDownDevice(authApply);
		}
		this.updateById(authApply);
	}
```

`SmtSecurityTaskDetailsServiceImpl.down()` 开头插入明细级抢占（原"下发后 setStatus(IN_WORK)"改为"先抢占再下发"）：

```java
	private void down(SmtSecurityTaskDetails detail, String badge) {
		// 明细级原子抢占：status WAIT→IN_WORK，抢不到说明并发方已处理，直接跳过（spec §3.1.1）
		boolean claimed = this.update(Wrappers.<SmtSecurityTaskDetails>lambdaUpdate()
				.eq(SmtSecurityTaskDetails::getId, detail.getId())
				.eq(SmtSecurityTaskDetails::getStatus, DeviceDownStatusEnum.WAIT.getCode())
				.set(SmtSecurityTaskDetails::getStatus, DeviceDownStatusEnum.IN_WORK.getCode()));
		if (!claimed) {
			return;
		}
		// ……以下为原 down() 逻辑，原"detail.setStatus(IN_WORK)"一行删除（已由抢占完成），
		// 失败置 FAIL + remark 的逻辑原样保留
```

- [ ] **Step 4: PASS + 全量回归** → **Step 5: Commit** `feat(platform): add CAS claim for oa final status and atomic detail dispatch`

---

### Task 22: 保密门禁 handler 接 claim（PR1 遗留的唯一行为变更）

**Files:**
- Modify: `.../service/oacallback/handler/SecurityAuthApplyCallbackHandler.java`
- Test: `.../test/.../service/oacallback/handler/SecurityAuthApplyCallbackHandlerTest.java`

**Interfaces:**
- Consumes: Task 21 `claimOaFinalStatus/triggerDownDevice`、Task 3 `OaFlowRecordSupport`。

- [ ] **Step 1: 写失败测试**（用例：claim 成功且 flag=true → triggerDownDevice 被调；claim 失败（对账已处理）→ 不触发下发；flag=false → claim REFUSE 且不下发）
- [ ] **Step 2: 确认失败** → **Step 3: 实现**（`handle` 重写为：命中 → `processAndDetectReturn` → `Integer finalStatus = flag ? AGREE : REFUSE` → `if (claimOaFinalStatus(id, finalStatus) && flag) triggerDownDevice(apply)`；保留"收到OA推送"日志）
- [ ] **Step 4: PASS** → **Step 5: Commit** `feat(platform): route security-auth callback through CAS claim flow`

---

### Task 23: updateOaStatusTask 对账任务（platform 侧）

**Files:**
- Modify: `.../service/securityzone/SmtSecurityAuthApplyService.java`（+`void updateOaStatusTask()`）
- Modify: `.../service/securityzone/impl/SmtSecurityAuthApplyServiceImpl.java`
- Test: `.../test/.../service/securityzone/SecurityAuthOaReconcileTest.java`

**Interfaces:**
- Consumes: `IOAWorkflowService.query(processId)`、Task 20 `OaFinalStatusResolver.resolve`、Task 21 `claimOaFinalStatus/triggerDownDevice`、Task 3 `ProcessRecordWriter` + `ProcessRecordItem.fromOaLog`、`StringRedisTemplate`（游标）。
- Produces: `updateOaStatusTask()`：
  - 场景 1：`oa_status=0 && process_id not null && create_time ∈ [now-90d, now-5min]`，`id > cursor order by id asc limit 200`（Redis 游标 key `oa:security:auth:cursor`，批空且 cursor>0 时归零重扫，镜像入厂模式）；逐单 query→resolve→claim→（AGREE：补 process_record + triggerDownDevice；REFUSE：claim 即完成）；query 异常记 warn 跳过（下轮天然重扫）。
  - 场景 2：`oa_status=1 && device_status=0`（同窗口）→ 直接 `triggerDownDevice`。
  - 每轮结构化计数日志：`log.info("保密门禁OA对账完成：扫描={}, 通过={}, 退回={}, 审批中={}, 查询失败={}, 触发失败={}", ...)`；发现 `create_time < now-24h` 仍 PENDING 的单：`log.warn("保密门禁申请超24小时未收到OA终态：processId={}", ...)`（spec §5.3）。
  - 常量：`RECONCILE_WINDOW_DAYS=90`、`RECONCILE_MIN_AGE_MINUTES=5`、`RECONCILE_BATCH_SIZE=200`、`PENDING_ALARM_HOURS=24`。

- [ ] **Step 1: 写失败测试**（用例：PENDING 单 OA 已归档 → claim + triggerDownDevice + 补 process_record；OA 退回 → claim REFUSE 不下发；OA 审批中 → 不动；query 抛异常 → 跳过不中断本轮其余单；场景 2 单 → 直接 triggerDownDevice；claim 输给并发回调（返回 false）→ 不下发；游标推进与批空归零）
- [ ] **Step 2: 确认失败** → **Step 3: 实现**（结构照 `SmtAdmittanceApplyServiceImpl.updateOaStatusTask` 1819-1859 行的游标/分页骨架简化：单游标即可，无需入厂的三段式 pending 列表）
- [ ] **Step 4: PASS + 全量回归** → **Step 5: Commit** `feat(platform): add oa status reconciliation task for security auth apply`

---

### Task 24: inner 端点 + Feign + schedule 接线

**Files:**
- Modify: `.../controller/securityzone/SmtSecurityAuthApplyController.java`（+`@Inner @GetMapping("/oa/status/task")`，调 `updateOaStatusTask()`，注解风格照同文件 `/msg` 端点）
- Modify: `smart-module/smart-platform/smart-platform-api/src/main/java/com/tce/smart/platform/api/feign/securityzone/RemoteSecurityAuthService.java`（+方法）
- Modify: `smart-module/smart-tool/src/main/java/com/tce/smart/tool/enums/TimerTaskEnum.java`（+`SECURITY_AUTH_UPDATE_OA("timer_security_auth_update_oa","保密门禁OA状态对账")`，键值风格照 `ADMITTANCE_UPDATE_OA`）
- Modify: `smart-module/smart-schedule/src/main/java/com/tce/smart/schedule/config/TaskJob.java`（+`private Boolean securityAuthUpdateOa;`）
- Modify: `smart-module/smart-schedule/src/main/java/com/tce/smart/schedule/task/PlatformTimerTask.java`

**Interfaces:**
- Produces: Feign `@GetMapping("/security/auth/apply/oa/status/task") Result updateOaStatusTask(@RequestHeader(SecurityConstants.FROM) String from);`；schedule 方法（照 `EHRViewTimerTask.admittanceUpdateOaTask` 166-183 行的 acquire/release 模式）：

```java
	/** 保密门禁申请OA审批状态对账 每2分钟执行一次（spec §3.1.6，Nacos 开关默认关） */
	@Scheduled(fixedDelay = 1000 * 60 * 2)
	public void securityAuthUpdateOaTask() {
		if (taskJob.getSecurityAuthUpdateOa() == null || !taskJob.getSecurityAuthUpdateOa()) {
			return;
		}
		String lockToken = switchService.acquire(TimerTaskEnum.SECURITY_AUTH_UPDATE_OA, 5, TimeUnit.MINUTES);
		if (lockToken == null) {
			return;
		}
		try {
			remoteSecurityAuthService.updateOaStatusTask(SecurityConstants.FROM_IN);
		} finally {
			switchService.release(TimerTaskEnum.SECURITY_AUTH_UPDATE_OA, lockToken);
		}
	}
```

- [ ] Step 1: 各文件按上述内容修改（无独立单测——纯接线，逻辑已在 Task 23 覆盖；schedule 模块若已有 task 测试惯例则补一个开关判断测试）
- [ ] Step 2: `mvn -pl smart-platform/smart-platform-api,smart-platform/smart-platform-biz,smart-schedule,smart-tool -am package -DskipTests` 编译通过（在 smart-module/ 下；模块参数按实际 pom 结构调整）
- [ ] Step 3: Commit `feat(schedule): wire security auth oa reconciliation task with nacos switch`

---

### Task 25: 手动下发 /down/{id} 重写

**Files:**
- Modify: `.../service/securityzone/impl/SmtSecurityAuthApplyServiceImpl.java`（重写 `downDevice(Long applyId)`）
- Test: `.../test/.../service/securityzone/SecurityAuthManualDownTest.java`

**Interfaces:**
- Consumes: `IOAWorkflowService.query`、`OaFinalStatusResolver`、`claimOaFinalStatus`、`triggerDownDevice`、`ProcessRecordWriter`。
- Produces: `Boolean downDevice(Long applyId)`，异常路径抛 `SmartException`（带明确中文提示，由既有全局异常处理器转为业务失败响应）。

- [ ] **Step 1: 写失败测试**（用例按 spec §3.4 全分支：记录不存在→异常"申请单不存在"；processId 空→异常；oa_status=0 且 OA 归档→claim+补记录+下发返回 true；oa_status=0 且 OA 退回→claim REFUSE+异常"OA已退回"；oa_status=0 且审批中→异常"OA审批未完成"；oa_status=0 且 query 抛异常→异常"OA状态查询失败"；oa_status=1→直接 triggerDownDevice；oa_status=2→异常"已被OA退回"）
- [ ] **Step 2: 确认失败** → **Step 3: 实现**

```java
	@Override
	public Boolean downDevice(Long applyId) {
		SmtSecurityAuthApply authApply = this.getById(applyId);
		// 输入与状态边界校验（spec §3.4，修 D5：原实现记录不存在直接 NPE 且未审批可下发）
		if (Objects.isNull(authApply)) {
			throw new SmartException("申请单不存在");
		}
		if (StrUtil.isBlank(authApply.getProcessId())) {
			throw new SmartException("申请单缺少OA流程编号，无法下发");
		}
		Integer oaStatus = authApply.getOaStatus();
		if (ApproveListStateEnum.REFUSE.getCode().equals(oaStatus)) {
			throw new SmartException("该申请已被OA退回，禁止下发");
		}
		if (ApproveListStateEnum.PENDING.getCode().equals(oaStatus)) {
			// 待审批：实时查 OA 判终态，与回调/对账共用同一套 claim 流程（spec §3.4）
			WorkFlowLogDTO logDTO;
			try {
				logDTO = ioaWorkflowService.query(authApply.getProcessId());
			} catch (Exception e) {
				log.error("手动下发查询OA状态失败：applyId={}", applyId, e);
				throw new SmartException("OA状态查询失败，请稍后重试");
			}
			Integer finalStatus = oaFinalStatusResolver.resolve(logDTO);
			if (Objects.isNull(finalStatus)) {
				throw new SmartException("OA审批未完成，禁止下发");
			}
			if (!claimOaFinalStatus(applyId, finalStatus)) {
				throw new SmartException("状态已被其他任务更新，请刷新后重试");
			}
			if (ApproveListStateEnum.REFUSE.getCode().equals(finalStatus)) {
				throw new SmartException("该申请已被OA退回，禁止下发");
			}
			// 补写过程记录，详情页本地留痕（spec §3.1.3）
			if (logDTO.getResultdata() != null) {
				logDTO.getResultdata().forEach(d ->
						processRecordWriter.write(authApply.getProcessId(), ProcessRecordItem.fromOaLog(d)));
			}
			authApply.setOaStatus(finalStatus);
		}
		// 此时 oa_status=1：触发下发（明细级抢占保证幂等，可安全重试）
		return this.triggerDownDevice(authApply);
	}
```

（`ioaWorkflowService`/`oaFinalStatusResolver`/`processRecordWriter` 为新增 `@Autowired` 字段。）

- [ ] **Step 4: PASS + 全量回归** → **Step 5: Commit** `fix(platform): harden manual security-auth dispatch with oa status verification`

---

### Task 26: 运维 Runbook + PR2 收尾

**Files:**
- Create: `docs/superpowers/runbooks/oa-callback-runbook.md`

- [ ] Step 1: 编写 runbook，内容必须包含：
  1. **巡检 SQL**（照 spec 附录 B 四条，`oa_callback_log` 条件为 `status=2 and resolved=0`）。
  2. **重放操作**：`curl -X POST "http://<gateway>/platform/oa/workflow/replay/{logId}" -H "from: Y"`（`from` 头的值以 `SecurityConstants.FROM_IN` 实际值为准），附"先查 `select id,request_id,failed_handlers,last_error from oa_callback_log where status=2 and resolved=0` 定位 logId"。
  3. **Nacos 开关**：`taskJob.securityAuthUpdateOa=true` 灰度开启对账；关闭即回滚到纯回调模式。
  4. **上线 SOP**（照 spec §5.1 顺序）：建表（`oa_callback_log.sql`）→ 发 platform → 发 schedule（开关关）→ 测试环境验证 → 生产灰度 → 观察 28753680 自动补齐（`select oa_status, device_status from smt_security_auth_apply where process_id='28753680'` 预期变为 `1/4`）；28760183 先在 OA 侧确认已归档。
  5. **对账任务日志关键字**：`保密门禁OA对账完成`、`保密门禁申请超24小时未收到OA终态`。
- [ ] Step 2: `mvn clean package -DskipTests`（smart-module/ 全模块）+ `mvn -pl smart-platform/smart-platform-biz -am test` 全量 PASS。
- [ ] Step 3: Commit `docs(runbooks): add oa callback reconciliation runbook` → 推分支，创建 PR2（title: `feat(platform): oa status reconciliation and hardened dispatch for security auth apply`，body Risks 注明"手动下发行为变化：未审批单从可下发变为拒绝，属安全修复预期行为"）。

---

## Self-Review 结论（计划作者自查）

- **Spec 覆盖**：§3.1 对账（Task 20/21/23/24）、§3.2 Handler 化+失败语义+锁（Task 1/3/4/5-17）、§3.3 落库+重放（Task 2/18）、§3.4 手动下发（Task 25）、§4 测试（各 Task Step 1）、§5 上线/巡检（Task 19/26）、终审 High TTL 硬校验（Task 4 `ttlGreaterThanDerivedUpperBound` 用例）。超 TTL 模拟集成测试合并进 Task 4 的唯一索引冲突用例（`duplicateKeyOnPartialWrite_fallbackResolvedSnapshot`）——锁过期的可观测后果即该冲突路径。
- **已知留白（刻意）**：Task 5-16 的 handler 主体来自源文件行段搬迁而非计划内复制粘贴——449 行监听器全文复制进计划的转写错误风险高于"精确行段+骨架+统一替换规则"；执行者以 git 历史中的源文件为准。
- **类型一致性**：`claimOaFinalStatus(Long, Integer)`、`triggerDownDevice(SmtSecurityAuthApply)`、`resolve(WorkFlowLogDTO)→Integer`、`DispatchResult` 在 Task 17/18/21/22/23/25 间引用一致。
- **执行注意**：Task 20 的 OA 样本核实是编码前置硬性动作；`@Inner` 注解包名、`Result` 失败构造器两处以项目实际代码为准（计划已标注核对方法）。
