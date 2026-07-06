# 保密门禁端点访问控制 + OA 回调日志留存治理 实现计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 给 `SmtSecurityAuthApplyController` 的 `/msg`、`/down/{id}` 端点补访问控制；落地 `smt_oa_callback_log` 90 天留存清理任务、回调日志脱敏与 runbook 留存/访问控制文档。

**Architecture:** ① `/msg` 加 `@Inner`（对齐同文件 `/oa/status/task`，调用方 Feign 已传 `FROM_IN`）；`/down/{id}` 加 `@PreAuthorize("@pms.hasPermission('platform_security_auth_down')")` + smart-ui 按钮权限。② platform 侧 `OaCallbackLogService.cleanExpiredLogs()` 删 90 天前整行，经 `@Inner` 端点 + 新 Feign 客户端由 smart-schedule 每日触发（`TaskJob` Nacos 开关默认关 + `TimerTaskEnum` 分布式锁惯例）；`OaCallbackDispatcher` 不再打全量 payload。

**Tech Stack:** Java 8、Spring Boot/Cloud（Greenwich 代系）、MyBatis-Plus 3.4.1、JUnit 4 + Mockito、Vue 2 + Element UI（smart-ui）。

**规格文档:** `docs/superpowers/specs/2026-07-05-security-auth-endpoint-acl-and-callback-log-retention-design.md`

## Global Constraints

- 注释一律中文（项目规约）；commit message 英文 Conventional Commits。
- 权限码固定为 `platform_security_auth_down`（后端注解、前端按钮、runbook 三处必须一致）。
- 留存常量 `RETENTION_DAYS = 90`、WARN 采样 `WARN_SAMPLE_SIZE = 10`，不得散落魔法数字。
- 所有 Maven 命令在 `smart-module/` 目录下执行；smart-ui 命令在 `smart-ui/` 目录下执行（pnpm）。
- 不改 `smt_oa_callback_log` 表结构，不写 sys_menu INSERT SQL（权限码走菜单管理 UI 配置，写入 runbook）。

---

### Task 1: 控制器访问控制注解 + 注解断言测试

**Files:**
- Modify: `smart-module/smart-platform/smart-platform-biz/src/main/java/com/tce/smart/platform/controller/securityzone/SmtSecurityAuthApplyController.java`
- Test(新建): `smart-module/smart-platform/smart-platform-biz/src/test/java/com/tce/smart/platform/controller/securityzone/SmtSecurityAuthApplyControllerAclTest.java`

**Interfaces:**
- Consumes: `com.tce.smart.common.security.annotation.Inner`、`org.springframework.security.access.prepost.PreAuthorize`（common-security 依赖已有）。
- Produces: 权限码字符串 `platform_security_auth_down`（Task 2 前端、Task 7 runbook 依赖同一权限码）。

- [ ] **Step 1: 写失败测试（反射断言注解存在且权限码正确）**

```java
package com.tce.smart.platform.controller.securityzone;

import com.tce.smart.common.security.annotation.Inner;
import org.junit.Test;
import org.springframework.security.access.prepost.PreAuthorize;

import java.lang.reflect.Method;

import static org.junit.Assert.*;

/**
 * 端点访问控制注解断言（PR #118/#119 评审安全后续项）：
 * /msg 仅供 smart-schedule 内部调用须 @Inner；
 * /down/{id} 是管理端手动下发须 @pms 权限码。
 */
public class SmtSecurityAuthApplyControllerAclTest {

	/** /msg 必须标 @Inner：唯一调用方是定时任务 Feign（FROM_IN），不面向前端 */
	@Test
	public void sendMessage_hasInnerAnnotation() throws Exception {
		Method method = SmtSecurityAuthApplyController.class.getMethod("sendMessage");
		assertNotNull("/msg 缺少 @Inner 注解", method.getAnnotation(Inner.class));
	}

	/** /down/{id} 必须标 @PreAuthorize 且权限码为 platform_security_auth_down */
	@Test
	public void downDevice_hasPreAuthorizeWithPermissionCode() throws Exception {
		Method method = SmtSecurityAuthApplyController.class.getMethod("downDevice", String.class);
		PreAuthorize preAuthorize = method.getAnnotation(PreAuthorize.class);
		assertNotNull("/down/{id} 缺少 @PreAuthorize 注解", preAuthorize);
		assertTrue("权限码必须是 platform_security_auth_down",
				preAuthorize.value().contains("platform_security_auth_down"));
	}
}
```

- [ ] **Step 2: 运行测试确认失败**

Run: `cd smart-module && mvn -pl smart-platform/smart-platform-biz -am test -Dtest=SmtSecurityAuthApplyControllerAclTest -DfailIfNoTests=false -q`
Expected: FAIL（两个断言都因注解缺失失败）

- [ ] **Step 3: 给控制器加注解**

`SmtSecurityAuthApplyController.java`——`downDevice` 方法（约 96 行）改为：

```java
	/**
	 * 手动下发（管理端操作，需菜单按钮权限码，PR #118/#119 评审安全后续项）
	 *
	 * @param id
	 * @return
	 */
	@PreAuthorize("@pms.hasPermission('platform_security_auth_down')")
	@GetMapping("/down/{id}")
	@ApiOperation("手动下发")
	public Result downDevice(@PathVariable("id") String id) {
		return success(smtSecurityAuthApplyService.downDevice(Long.parseLong(id)));
	}
```

`sendMessage` 方法（约 107 行）改为：

```java
	/**
	 * 提示信息推送（仅供 smart-schedule 定时任务 Feign 调用，对齐 /oa/status/task 的 @Inner）
	 *
	 * @param
	 * @return
	 */
	@Inner
	@GetMapping("/msg")
	@ApiOperation("下发提示信息推送")
	public void sendMessage() {
		smtSecurityAuthApplyService.sendMessage();
	}
```

import 区新增（`Inner` 已 import）：

```java
import org.springframework.security.access.prepost.PreAuthorize;
```

- [ ] **Step 4: 运行测试确认通过**

Run: `cd smart-module && mvn -pl smart-platform/smart-platform-biz -am test -Dtest=SmtSecurityAuthApplyControllerAclTest -DfailIfNoTests=false -q`
Expected: PASS（2 tests）

- [ ] **Step 5: Commit**

```bash
git add smart-module/smart-platform/smart-platform-biz/src/main/java/com/tce/smart/platform/controller/securityzone/SmtSecurityAuthApplyController.java smart-module/smart-platform/smart-platform-biz/src/test/java/com/tce/smart/platform/controller/securityzone/SmtSecurityAuthApplyControllerAclTest.java
git commit -m "fix(platform): add @Inner and @pms permission to security auth apply endpoints"
```

---

### Task 2: smart-ui 手动下发按钮权限控制

**Files:**
- Modify: `smart-ui/src/views/platform/security_area/xc_guard_apply/index.vue`（按钮约 67 行、script 约 76-91 行）

**Interfaces:**
- Consumes: Vuex getter `permissions`（`src/store/getters.js` 已有：`permissions: state => state.user.permissions`）；权限码 `platform_security_auth_down`（Task 1 定义）。
- Produces: 无（终端 UI 改动）。

- [ ] **Step 1: 按钮加权限显隐 + 引入 permissions getter**

模板（约 64-68 行）改为——保留原 `:disabled` OA 状态判断，外加权限 `v-if`：

```html
          <template slot-scope="scope" slot="menu">
            <el-button type="text" icon="el-icon-view" @click="handleDetail(scope.row,scope.$index)" >详情</el-button>
            <!--oaStatus oa状态 已通过 才可以手动下发；按钮权限码需在菜单管理配置并绑定角色（见 runbook） -->
            <el-button type="text" v-if="permissions['platform_security_auth_down']" @click="handleSend(scope.row, scope.$index)" :disabled="scope.row.oaStatus!==1">手动下发</el-button>
          </template>
```

script 区（约 76-91 行）加 `mapGetters`（沿用 `admin/role/index.vue` 惯例）：

```js
import { xcGuardApplyApi } from "./_service"
import { mapGetters } from 'vuex'

export default {
  mixins: [tce.mixins.list],
  data() {
    return {
      depIds: [],
      times: [],
      tableData: [],
      listOption: listOption(),
    };
  },
  computed: {
    // 按钮权限：platform_security_auth_down 由菜单管理配置、登录时随用户权限下发
    ...mapGetters(['permissions'])
  },
  created() {
    this.refresh()
  },
```

- [ ] **Step 2: lint 校验**

Run: `cd smart-ui && pnpm lint`
Expected: 通过（或仅存量告警，无本文件新增错误）

- [ ] **Step 3: Commit**

```bash
git add smart-ui/src/views/platform/security_area/xc_guard_apply/index.vue
git commit -m "fix(ui): gate manual security auth dispatch button by permission code"
```

---

### Task 3: OaCallbackLogService.cleanExpiredLogs() + 单测

**Files:**
- Modify: `smart-module/smart-platform/smart-platform-biz/src/main/java/com/tce/smart/platform/service/oacallback/OaCallbackLogService.java`
- Modify: `smart-module/smart-platform/smart-platform-biz/src/main/java/com/tce/smart/platform/service/oacallback/impl/OaCallbackLogServiceImpl.java`
- Modify(追加测试): `smart-module/smart-platform/smart-platform-biz/src/test/java/com/tce/smart/platform/service/oacallback/OaCallbackLogServiceImplTest.java`

**Interfaces:**
- Consumes: `OaCallbackLog` 实体常量（`STATUS_PARTIAL_FAIL`、`RESOLVED_NO`）。
- Produces: `int cleanExpiredLogs()`（返回删除行数；Task 4 controller 调用）；常量 `OaCallbackLogService.RETENTION_DAYS = 90`。

- [ ] **Step 1: 接口加方法与常量**

`OaCallbackLogService.java` 改为：

```java
package com.tce.smart.platform.service.oacallback;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tce.smart.platform.core.entity.OaCallbackLog;

/** OA 回调日志服务 */
public interface OaCallbackLogService extends IService<OaCallbackLog> {

	/**
	 * payload 留存天数：90 天整行删除。
	 * 与保密门禁对账回溯窗口（90 天）对齐；payload 含姓名/工号 PII，到期必须物理删除（spec 2026-07-05 §2）。
	 */
	int RETENTION_DAYS = 90;

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

	/**
	 * 清理 receive_time 早于 RETENTION_DAYS 天前的全部日志（整行删，含 payload）。
	 * 被删行中若含未解决 partial（status=2 and resolved=0）记 WARN——90 天无人重放视为放弃重放。
	 * @return 实际删除行数
	 */
	int cleanExpiredLogs();
}
```

- [ ] **Step 2: 写失败测试（追加到既有测试类）**

`OaCallbackLogServiceImplTest.java` 在类内追加以下测试；同时把 `setUp` 中的匿名子类扩展出可注入的桩（完整替换 `setUp` 与新增字段如下）：

```java
	private OaCallbackLogServiceImpl service;
	private OaCallbackLogMapper mapper;
	/** cleanExpiredLogs 桩数据：list() 返回的未解决 partial */
	private java.util.List<OaCallbackLog> stubUnresolvedExpired = new java.util.ArrayList<>();
	/** cleanExpiredLogs 桩数据：count() 返回的过期总行数 */
	private int stubExpiredCount = 0;
	/** 记录 remove 是否被调用 */
	private boolean removeCalled = false;

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

			// cleanExpiredLogs 依赖的 MP 查询方法全部覆写为桩，隔离数据库
			@Override
			public java.util.List<OaCallbackLog> list(com.baomidou.mybatisplus.core.conditions.Wrapper<OaCallbackLog> wrapper) {
				return stubUnresolvedExpired;
			}

			@Override
			public int count(com.baomidou.mybatisplus.core.conditions.Wrapper<OaCallbackLog> wrapper) {
				return stubExpiredCount;
			}

			@Override
			public boolean remove(com.baomidou.mybatisplus.core.conditions.Wrapper<OaCallbackLog> wrapper) {
				removeCalled = true;
				return true;
			}
		};
	}
```

追加三个测试方法：

```java
	@Test
	public void cleanExpiredLogs_deletesAndReturnsCount() {
		stubExpiredCount = 5;
		int deleted = service.cleanExpiredLogs();
		assertEquals(5, deleted);
		assertTrue("有过期数据必须执行删除", removeCalled);
	}

	@Test
	public void cleanExpiredLogs_noExpired_skipsDelete() {
		stubExpiredCount = 0;
		int deleted = service.cleanExpiredLogs();
		assertEquals(0, deleted);
		assertFalse("无过期数据不应执行删除", removeCalled);
	}

	@Test
	public void cleanExpiredLogs_withUnresolvedPartial_stillDeletes() {
		// 90 天未重放的 partial 一并删除（留存承诺优先，WARN 日志兜底可见性）
		stubExpiredCount = 3;
		OaCallbackLog partial = new OaCallbackLog();
		partial.setRequestId("28753680");
		stubUnresolvedExpired.add(partial);
		int deleted = service.cleanExpiredLogs();
		assertEquals(3, deleted);
		assertTrue(removeCalled);
	}
```

- [ ] **Step 3: 运行测试确认失败**

Run: `cd smart-module && mvn -pl smart-platform/smart-platform-biz -am test -Dtest=OaCallbackLogServiceImplTest -DfailIfNoTests=false -q`
Expected: 编译 FAIL（`cleanExpiredLogs` 未定义）

- [ ] **Step 4: 实现 cleanExpiredLogs**

`OaCallbackLogServiceImpl.java`——import 区补充：

```java
import java.util.stream.Collectors;
```

类内追加：

```java
	/** WARN 日志采样的 request_id 上限，防单条日志过长 */
	private static final int WARN_SAMPLE_SIZE = 10;

	@Override
	public int cleanExpiredLogs() {
		LocalDateTime cutoff = LocalDateTime.now().minusDays(RETENTION_DAYS);
		// 先统计将被删除的未解决 partial：90 天无人重放视为放弃，删除即丧失重放能力，必须 WARN 留痕
		List<OaCallbackLog> expiredUnresolved = this.list(Wrappers.<OaCallbackLog>query().lambda()
				.eq(OaCallbackLog::getStatus, OaCallbackLog.STATUS_PARTIAL_FAIL)
				.eq(OaCallbackLog::getResolved, OaCallbackLog.RESOLVED_NO)
				.lt(OaCallbackLog::getReceiveTime, cutoff));
		if (!expiredUnresolved.isEmpty()) {
			List<String> sampleRequestIds = expiredUnresolved.stream()
					.map(OaCallbackLog::getRequestId)
					.limit(WARN_SAMPLE_SIZE)
					.collect(Collectors.toList());
			log.warn("OA回调日志清理将删除未解决partial：count={}, requestIds(最多{}个)={}",
					expiredUnresolved.size(), WARN_SAMPLE_SIZE, sampleRequestIds);
		}
		// 90 天整行删除（payload 含 PII，到期物理删除，spec 2026-07-05 §2）
		int total = this.count(Wrappers.<OaCallbackLog>query().lambda()
				.lt(OaCallbackLog::getReceiveTime, cutoff));
		if (total > 0) {
			this.remove(Wrappers.<OaCallbackLog>query().lambda()
					.lt(OaCallbackLog::getReceiveTime, cutoff));
		}
		log.info("OA回调日志过期清理完成：deleted={}, cutoff={}", total, cutoff);
		return total;
	}
```

- [ ] **Step 5: 运行测试确认通过**

Run: `cd smart-module && mvn -pl smart-platform/smart-platform-biz -am test -Dtest=OaCallbackLogServiceImplTest -DfailIfNoTests=false -q`
Expected: PASS（5 tests：既有 2 + 新增 3）

- [ ] **Step 6: Commit**

```bash
git add smart-module/smart-platform/smart-platform-biz/src/main/java/com/tce/smart/platform/service/oacallback/OaCallbackLogService.java smart-module/smart-platform/smart-platform-biz/src/main/java/com/tce/smart/platform/service/oacallback/impl/OaCallbackLogServiceImpl.java smart-module/smart-platform/smart-platform-biz/src/test/java/com/tce/smart/platform/service/oacallback/OaCallbackLogServiceImplTest.java
git commit -m "feat(platform): add 90-day retention cleanup for oa callback log"
```

---

### Task 4: 清理端点（@Inner）+ Feign 客户端

**Files:**
- Modify: `smart-module/smart-platform/smart-platform-biz/src/main/java/com/tce/smart/platform/controller/OAWorkflowController.java`
- Create: `smart-module/smart-platform/smart-platform-api/src/main/java/com/tce/smart/platform/api/feign/RemoteOaCallbackLogService.java`
- Modify(测试): `smart-module/smart-platform/smart-platform-biz/src/test/java/com/tce/smart/platform/controller/OAWorkflowControllerTest.java`

**Interfaces:**
- Consumes: Task 3 的 `OaCallbackLogService.cleanExpiredLogs()`。
- Produces: `GET /oa/workflow/callback/log/clean`（@Inner）；Feign `RemoteOaCallbackLogService.cleanTask(String from)`（Task 5 调度用）。

- [ ] **Step 1: 写失败测试（注解断言 + 端点行为）**

`OAWorkflowControllerTest.java`——`setUp` 需给控制器构造函数补第 4 个 mock，并追加测试。完整替换 `setUp` 与新增内容：

```java
	private OaCallbackDispatcher dispatcher;
	private com.tce.smart.platform.service.oacallback.OaCallbackLogService logService;
	private MockMvc mockMvc;

	@Before
	public void setUp() {
		dispatcher = mock(OaCallbackDispatcher.class);
		logService = mock(com.tce.smart.platform.service.oacallback.OaCallbackLogService.class);
		OAWorkflowController controller = new OAWorkflowController(dispatcher, mock(IOAWorkflowService.class),
				mock(OaCallbackReplayService.class), logService);
		mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
	}
```

追加测试方法（import 补 `com.tce.smart.common.security.annotation.Inner`、`java.lang.reflect.Method`、`static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get`、`static org.junit.Assert.assertNotNull`）：

```java
	/** 清理端点必须 @Inner：仅供 smart-schedule 定时任务调用 */
	@Test
	public void cleanExpiredLogs_hasInnerAnnotation() throws Exception {
		Method method = OAWorkflowController.class.getMethod("cleanExpiredLogs");
		assertNotNull("/callback/log/clean 缺少 @Inner 注解", method.getAnnotation(Inner.class));
	}

	/** 清理端点调用 service 并返回 200 */
	@Test
	public void cleanExpiredLogs_invokesServiceAndReturns200() throws Exception {
		when(logService.cleanExpiredLogs()).thenReturn(3);
		mockMvc.perform(get("/oa/workflow/callback/log/clean"))
				.andExpect(status().isOk());
		verify(logService).cleanExpiredLogs();
	}
```

- [ ] **Step 2: 运行测试确认失败**

Run: `cd smart-module && mvn -pl smart-platform/smart-platform-biz -am test -Dtest=OAWorkflowControllerTest -DfailIfNoTests=false -q`
Expected: 编译 FAIL（构造函数签名不匹配 / `cleanExpiredLogs` 不存在）

- [ ] **Step 3: 控制器加端点**

`OAWorkflowController.java`——import 补 `com.tce.smart.platform.service.oacallback.OaCallbackLogService;`；字段与构造函数改为：

```java
	private final OaCallbackDispatcher dispatcher;
	private final IOAWorkflowService iOAWorkflowService;
	private final OaCallbackReplayService replayService;
	private final OaCallbackLogService logService;

	public OAWorkflowController(OaCallbackDispatcher dispatcher, IOAWorkflowService iOAWorkflowService,
			OaCallbackReplayService replayService, OaCallbackLogService logService) {
		this.dispatcher = dispatcher;
		this.iOAWorkflowService = iOAWorkflowService;
		this.replayService = replayService;
		this.logService = logService;
	}
```

类尾追加端点（GET 跟随项目定时任务端点惯例）：

```java
	/** 过期回调日志清理（90 天整行删，仅供 smart-schedule 定时任务调用，spec 2026-07-05 §3.2） */
	@Inner
	@GetMapping("/callback/log/clean")
	public Result cleanExpiredLogs() {
		return success(logService.cleanExpiredLogs());
	}
```

- [ ] **Step 4: 运行测试确认通过**

Run: `cd smart-module && mvn -pl smart-platform/smart-platform-biz -am test -Dtest=OAWorkflowControllerTest -DfailIfNoTests=false -q`
Expected: PASS（4 tests：既有 2 + 新增 2）

- [ ] **Step 5: 新建 Feign 客户端**

Create `smart-module/smart-platform/smart-platform-api/src/main/java/com/tce/smart/platform/api/feign/RemoteOaCallbackLogService.java`：

```java
package com.tce.smart.platform.api.feign;

import com.tce.smart.common.core.constant.SecurityConstants;
import com.tce.smart.common.core.constant.ServiceNameConstants;
import com.tce.smart.common.core.model.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;

/**
 * OA 回调日志维护（内部调用，smart-schedule 定时任务专用）
 */
@FeignClient(value = ServiceNameConstants.PLATFORM_SERVICE, contextId = "remoteOaCallbackLogService")
public interface RemoteOaCallbackLogService {

	/**
	 * 过期回调日志清理任务（90 天整行删除，spec 2026-07-05 §3.2）
	 * @param from 内部调用标识（SecurityConstants.FROM_IN）
	 * @return 删除行数
	 */
	@GetMapping("/oa/workflow/callback/log/clean")
	Result cleanTask(@RequestHeader(SecurityConstants.FROM) String from);
}
```

注意：若编译报 `contextId` 不支持（Feign 版本过老），去掉 `contextId` 属性——同服务多 Feign 接口在本项目已有先例（`RemoteSecurityAuthService` 等均未用 contextId），跟随既有写法即可。

- [ ] **Step 6: 编译 platform-api 确认通过**

Run: `cd smart-module && mvn -pl smart-platform/smart-platform-api -am package -DskipTests -q`
Expected: BUILD SUCCESS

- [ ] **Step 7: Commit**

```bash
git add smart-module/smart-platform/smart-platform-biz/src/main/java/com/tce/smart/platform/controller/OAWorkflowController.java smart-module/smart-platform/smart-platform-api/src/main/java/com/tce/smart/platform/api/feign/RemoteOaCallbackLogService.java smart-module/smart-platform/smart-platform-biz/src/test/java/com/tce/smart/platform/controller/OAWorkflowControllerTest.java
git commit -m "feat(platform): expose inner endpoint and feign client for callback log cleanup"
```

---

### Task 5: smart-schedule 定时调度接线

**Files:**
- Modify: `smart-module/smart-tool/src/main/java/com/tce/smart/tool/enums/TimerTaskEnum.java`（约 60 行 SECURITY_AUTH_UPDATE_OA 之后）
- Modify: `smart-module/smart-schedule/src/main/java/com/tce/smart/schedule/config/TaskJob.java`（约 30 行 securityAuthUpdateOa 之后）
- Modify: `smart-module/smart-schedule/src/main/resources/bootstrap.yml`（约 43 行 security-auth-update-oa 之后）
- Modify: `smart-module/smart-schedule/src/main/java/com/tce/smart/schedule/task/PlatformTimerTask.java`

**Interfaces:**
- Consumes: Task 4 的 `RemoteOaCallbackLogService.cleanTask(String)`；既有 `ISwitchService.process(TimerTaskEnum)`、`TaskJob`。
- Produces: Nacos 开关键 `task.job.oaCallbackLogClean`（runbook 引用）；Redis 锁键 `timer_oa_callback_log_clean`。

- [ ] **Step 1: TimerTaskEnum 加枚举**

在 `SECURITY_AUTH_UPDATE_OA(...)` 行后追加：

```java
	OA_CALLBACK_LOG_CLEAN("timer_oa_callback_log_clean","timer_oa_callback_log_clean","OA回调日志过期清理任务"),
```

- [ ] **Step 2: TaskJob 加开关字段**

在 `securityAuthUpdateOa` 字段后追加：

```java
	/** OA回调日志90天过期清理开关（spec 2026-07-05 §3.2，Nacos 默认关） */
	private Boolean oaCallbackLogClean;
```

- [ ] **Step 3: bootstrap.yml 加默认关配置**

在 `security-auth-update-oa: false` 行后追加（对齐缩进与注释风格）：

```yaml
    oa-callback-log-clean: false          # OA回调日志90天过期清理任务（payload 含 PII，见 oa-callback-runbook）
```

- [ ] **Step 4: PlatformTimerTask 加调度方法**

import 补 `com.tce.smart.platform.api.feign.RemoteOaCallbackLogService;`；字段注入区追加：

```java
	@Autowired
	private RemoteOaCallbackLogService remoteOaCallbackLogService;
```

类尾追加调度方法（每日 03:30，错开 00:00 的 autoDeleteTask；清理量日均几十行、秒级完成，用简单 process 锁惯例即可）：

```java
	/**
	 * OA回调日志过期清理 每天03:30执行一次（90 天整行删除，payload 含 PII，spec 2026-07-05 §3.2）
	 */
	@Scheduled(cron = "0 30 3 * * ?")
	public void oaCallbackLogClean() {
		if (taskJob.getOaCallbackLogClean() != null && taskJob.getOaCallbackLogClean()
				&& switchService.process(TimerTaskEnum.OA_CALLBACK_LOG_CLEAN)) {
			try {
				remoteOaCallbackLogService.cleanTask(SecurityConstants.FROM_IN);
			} catch (Exception e) {
				log.error("OA回调日志过期清理任务异常", e);
			}
		}
	}
```

- [ ] **Step 5: 编译 schedule 与 tool 确认通过**

Run: `cd smart-module && mvn -pl smart-schedule -am package -DskipTests -q`
Expected: BUILD SUCCESS

- [ ] **Step 6: Commit**

```bash
git add smart-module/smart-tool/src/main/java/com/tce/smart/tool/enums/TimerTaskEnum.java smart-module/smart-schedule/src/main/java/com/tce/smart/schedule/config/TaskJob.java smart-module/smart-schedule/src/main/resources/bootstrap.yml smart-module/smart-schedule/src/main/java/com/tce/smart/schedule/task/PlatformTimerTask.java
git commit -m "feat(schedule): wire daily oa callback log cleanup task with nacos switch"
```

---

### Task 6: OaCallbackDispatcher 日志脱敏

**Files:**
- Modify: `smart-module/smart-platform/smart-platform-biz/src/main/java/com/tce/smart/platform/service/oacallback/OaCallbackDispatcher.java:55`

**Interfaces:**
- Consumes / Produces: 无（纯日志语句改动）。

- [ ] **Step 1: 改日志语句**

第 55 行：

```java
		log.info("收到OA审批消息：{}", payload);
```

改为（payload 含姓名/工号 PII，全文已落库 `smt_oa_callback_log`，排查走 DB，日志只留定位信息）：

```java
		log.info("收到OA审批消息：requestId={}, payloadLength={}", requestId, payload.length());
```

- [ ] **Step 2: 跑分发器既有测试回归**

Run: `cd smart-module && mvn -pl smart-platform/smart-platform-biz -am test -Dtest=OaCallbackDispatcherTest -DfailIfNoTests=false -q`
Expected: PASS（既有全部用例）

- [ ] **Step 3: Commit**

```bash
git add smart-module/smart-platform/smart-platform-biz/src/main/java/com/tce/smart/platform/service/oacallback/OaCallbackDispatcher.java
git commit -m "fix(platform): stop logging full oa callback payload with pii"
```

---

### Task 7: runbook 留存/访问控制文档

**Files:**
- Modify: `docs/superpowers/runbooks/oa-callback-runbook.md`（在「应急回滚」节之前插入新节；「相关 Redis key 与日志关键字」表追加两行）

**Interfaces:**
- Consumes: 权限码 `platform_security_auth_down`（Task 1）、开关 `task.job.oaCallbackLogClean`（Task 5）、锁键 `timer_oa_callback_log_clean`（Task 5）。

- [ ] **Step 1: 插入「数据留存与访问控制」节**

在 `## 应急回滚` 节之前插入：

```markdown
## 数据留存与访问控制

### payload 留存策略（90 天整行删除）

`smt_oa_callback_log.payload` 明文存储 OA 回调全报文，含姓名、工号等 PII。留存策略：

- **保留 90 天，到期整行物理删除**（含 payload），与保密门禁对账回溯窗口（90 天）对齐。
- 执行方式：smart-schedule 每天 03:30 触发（Nacos 开关 `task.job.oaCallbackLogClean`，默认关，上线后需手动开启），经 Feign 调 platform 的 `@Inner` 端点 `GET /oa/workflow/callback/log/clean`。
- 被删行中若含未解决 partial（status=2 且 resolved=0），任务记 WARN（含数量与最多前 10 个 request_id）——90 天无人重放视为放弃重放，属有意取舍；巡检 SQL（见上文）保证 90 天内可见。
- 首次开启注意：会一次性删除全部存量超期数据，属预期行为。

### payload 访问途径（最小化清单）

| 途径 | 访问控制 | 说明 |
|---|---|---|
| 数据库直查 | DBA / 运维数据库账号 | 唯一的全文查看途径，按数据库权限管理 |
| 重放接口 `POST /oa/workflow/replay/{logId}` | `@Inner`，仅内部调用 | 使用 payload 但不返回 payload 内容 |
| 管理后台 / 前端 | 无 | 该表无任何 UI 暴露 |
| 应用日志 | 已脱敏 | 分发器只打 requestId + 报文长度，不打全量报文 |

### 手动下发按钮权限码上线顺序（platform_security_auth_down）

`/security/auth/apply/down/{id}`（保密区权限手动下发）已加 `@PreAuthorize("@pms.hasPermission('platform_security_auth_down')")`，发版顺序**必须**：

1. 管理后台「权限管理 → 菜单管理」在保密门禁申请页面下新增按钮型菜单，权限标识填 `platform_security_auth_down`；
2. 「角色管理」把该按钮权限授予需要手动下发的管理角色，相关用户重新登录生效；
3. 再发布 smart-platform 与 smart-ui 版本。

顺序反了的后果：所有人调用 `/down/{id}` 一律 403（前端按钮同时被权限码隐藏）。回退手段：给角色补绑权限码即可，无需回滚版本。
```

- [ ] **Step 2: 「相关 Redis key 与日志关键字」表追加两行**

```markdown
| 回调日志清理任务分布式锁 | `timer_oa_callback_log_clean` | 防止多实例并发清理；`TimerTaskEnum.OA_CALLBACK_LOG_CLEAN` |
| 清理完成计数日志 | `OA回调日志过期清理完成：` | 含 deleted 与 cutoff；WARN `OA回调日志清理将删除未解决partial` 需人工关注 |
```

- [ ] **Step 3: Commit**

```bash
git add docs/superpowers/runbooks/oa-callback-runbook.md
git commit -m "docs(runbooks): add oa callback log retention, access control and permission rollout"
```

---

### Task 8: 全量验证

**Files:** 无新改动，纯验证。

- [ ] **Step 1: platform 全模块测试**

Run: `cd smart-module && mvn -pl smart-platform/smart-platform-biz -am test -q`
Expected: BUILD SUCCESS，全部测试通过

- [ ] **Step 2: schedule 编译 + smart-ui lint**

Run: `cd smart-module && mvn -pl smart-schedule -am package -DskipTests -q && cd ../smart-ui && pnpm lint`
Expected: 均通过

- [ ] **Step 3: 确认工作区干净、提交完整**

Run: `git status -sb && git log --oneline origin/main..HEAD`
Expected: 工作区 clean；commit 序列 = 设计文档 + Task 1~7 共 8 个 commit
```
