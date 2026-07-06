# 保密权限微信推送改造 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 保密权限下发通知的微信推送：正文压缩到 20 字内且信息可读、失败重试封顶 3 次不再无限骚扰、WeChatMsgUtil 模板名参数化（旧 17 个调用点零改动）。

**Architecture:** `smart-tool` 的 `WeChatMsgUtil` 新增 `sendTemplateMsg` 重载并把「显示名」与「路由工号」解耦，旧 `sendMsg` 原签名委托之；`smart-platform-biz` 的 `SmtSecurityAuthApplyServiceImpl.sendMessage()` 重写为带重试上限的状态机（isMsg 0→1 成功 / 0→…→2 失败放弃），静态调用通过 protected seam 方法隔离以便单测；DB 变更走 `database/manual/` Oracle PL/SQL 匿名块脚本。

**Tech Stack:** Java 8、Spring Boot 2.1、MyBatis-Plus、JUnit 4 + Mockito 2.x（**无 mockStatic**，静态依赖用 protected seam + 子类覆写）、Oracle（manual SQL）。

**Spec:** `docs/superpowers/specs/2026-07-06-security-auth-wechat-msg-redesign.md`

## Global Constraints

- 注释一律中文（含 JavaDoc、SQL、测试注释）；新代码注释密度对齐仓库现状。
- Java 8 语法；Mockito 2.23/2.28 无 `mockStatic`，禁止引 PowerMock。
- 其余 17 个 `WeChatMsgUtil.sendMsg` 调用点行为必须逐字节不变。
- `smt_security_auth_apply` / `smt_msg_template` 均为 `tech_platform` 模式的表；manual 脚本必须是 PL/SQL 匿名块、幂等（查 `USER_TAB_COLUMNS` 再 DDL）、文件头注明目标模式、整段执行、末尾无 `/`。
- 分支 `feat/security-auth-wechat-msg`，提交走 Conventional Commits（英文）。
- 微信 thing 字段上限 20 字：模板内容「保密权限下发完成 成功{成功数量}/共{总数量}」。
- 常量：`MAX_MSG_RETRY = 3`；`isMsg` 语义 0=未发送、1=已发送、2=失败放弃（新常量 `MSG_SEND_ABANDONED`）。

---

### Task 0: 建功能分支

**Files:** 无代码改动。

- [ ] **Step 1: 从当前 HEAD 建分支**

```bash
git checkout -b feat/security-auth-wechat-msg
```

预期：`Switched to a new branch 'feat/security-auth-wechat-msg'`（当前 worktree 已隔离，spec/plan 两个 docs 提交随分支带走）。

---

### Task 1: WeChatMsgUtil 参数化（smart-tool）

**Files:**
- Modify: `smart-module/smart-tool/src/main/java/com/tce/smart/tool/util/WeChatMsgUtil.java`
- Test: `smart-module/smart-tool/src/test/java/com/tce/smart/tool/util/WeChatMsgUtilTest.java`（新建）

**Interfaces:**
- Produces（Task 2 依赖）:
  - `public static final String DEFAULT_TEMPLATE_NAME = "访客出入园提醒"`（原私有 `TEMPLATE_NAME` 改名提级，值不变）
  - `public static Boolean sendTemplateMsg(String templateName, String displayName, String body, String loginName, String openId, String url)`
  - 旧 `public static Boolean sendMsg(String loginName, String remark, String openId, String url)` 签名与行为不变（内部委托）。
- 包内可见（仅供同包单测）: `static JSONObject buildMessageData(String displayName, String body)`、`static JSONObject buildRequestParameter(String templateName, String loginName, String openId, String url, JSONObject dataObj)`

- [ ] **Step 1: 写失败的单测**

新建 `WeChatMsgUtilTest.java`（与工具类同包，直测包内可见的构建方法，不发真实 HTTP）：

```java
package com.tce.smart.tool.util;

import cn.hutool.json.JSONObject;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * WeChatMsgUtil 参数化单测：只测消息体/请求参数构建（纯函数），不发真实 HTTP。
 * 背景：模板名原先写死「访客出入园提醒」，本次把模板名与「显示名/路由工号」解耦，
 * 旧 sendMsg 签名必须保持行为逐字节不变（全系统 17 个调用点依赖）。
 */
public class WeChatMsgUtilTest {

	/** displayName 应落到 thing18，body 应落到 thing14，time4 非空（发送时刻） */
	@Test
	public void buildMessageData_displayNameAndBodyLandOnCorrectFields() {
		JSONObject data = WeChatMsgUtil.buildMessageData("张三", "保密权限下发完成 成功3/共5");

		assertEquals("张三", data.getJSONObject("thing18").getStr("value"));
		assertEquals("保密权限下发完成 成功3/共5", data.getJSONObject("thing14").getStr("value"));
		assertFalse(data.getJSONObject("time4").getStr("value").isEmpty());
	}

	/** displayName 为空时沿用「系统通知」兜底（旧行为） */
	@Test
	public void buildMessageData_emptyDisplayNameFallsBackToSystemNotice() {
		JSONObject data = WeChatMsgUtil.buildMessageData(null, "正文");

		assertEquals("系统通知", data.getJSONObject("thing18").getStr("value"));
	}

	/** templateName 参数应落到请求的 templateName 字段；loginName/openId 有值才写入（旧行为） */
	@Test
	public void buildRequestParameter_templateNameIsParameterized() {
		JSONObject data = WeChatMsgUtil.buildMessageData("张三", "正文");
		JSONObject param = WeChatMsgUtil.buildRequestParameter(
				"某新模板", "8056297", null, "http://example.com", data);

		assertEquals("某新模板", param.getStr("templateName"));
		assertEquals("8056297", param.getStr("loginName"));
		assertFalse("openId 为空不应写入", param.containsKey("openId"));
		assertEquals("http://example.com", param.getStr("url"));
	}

	/** 默认模板名常量必须保持「访客出入园提醒」——17 个旧调用点靠它维持现状 */
	@Test
	public void defaultTemplateNameUnchanged() {
		assertEquals("访客出入园提醒", WeChatMsgUtil.DEFAULT_TEMPLATE_NAME);
	}
}
```

- [ ] **Step 2: 跑测试确认失败**

```bash
mvn -pl smart-module/smart-tool -am test -Dtest=WeChatMsgUtilTest -DfailIfNoTests=false -q
```

预期：编译失败（`buildMessageData(String,String)` 方法签名不存在、`DEFAULT_TEMPLATE_NAME` 不存在）。

- [ ] **Step 3: 改造 WeChatMsgUtil**

对 `WeChatMsgUtil.java` 做如下修改（保持其余方法不动）：

```java
	/**
	 * 微信消息推送相关常量
	 */
	private static final String WECHAT_API_URL = "https://xchr.szyuto.com:8888/commonData/getData.html";
	private static final String API_KEY = "insertTemplateMsg";
	/**
	 * 默认公众号模板名：历史上全系统所有业务推送都借用「访客出入园提醒」模板壳。
	 * 旧 sendMsg 签名继续用它以保持 17 个存量调用点行为不变；
	 * 新业务请走 sendTemplateMsg 显式传模板名（可用模板清单需运维确认）。
	 */
	public static final String DEFAULT_TEMPLATE_NAME = "访客出入园提醒";
	private static final String DEFAULT_SYSTEM_NOTICE = "系统通知";
	private static final String ENCODING_UTF8 = "utf-8";
```

```java
	/**
	 * 发送微信消息（旧签名，行为不变：displayName 取 loginName、走默认模板）。
	 * 注意 loginName 双重职责的历史包袱：既是中转服务查 openId 的路由键，
	 * 又直接显示在模板 thing18 字段——需要姓名展示的场景请改用 sendTemplateMsg。
	 *
	 * @param loginName 登录名（工号，同时作为 thing18 展示值）
	 * @param remark 消息备注（正文，thing 字段超 20 字会被微信截断）
	 * @param openId 微信OpenId
	 * @param url 跳转链接
	 * @return 发送结果
	 */
	public static Boolean sendMsg(String loginName, String remark, String openId, String url) {
		return sendTemplateMsg(DEFAULT_TEMPLATE_NAME, loginName, remark, loginName, openId, url);
	}

	/**
	 * 发送微信模板消息（模板名参数化，显示名与路由工号解耦）。
	 *
	 * @param templateName 公众号模板名（中转服务按名称匹配模板）
	 * @param displayName 展示名（落 thing18 字段，空则显示「系统通知」）
	 * @param body 正文（落 thing14 字段，超 20 字会被微信截断，调用方自行控制长度）
	 * @param loginName 路由工号（中转服务据此查 openId，可空）
	 * @param openId 微信OpenId（与 loginName 二选一，可空）
	 * @param url 跳转链接
	 * @return 发送结果（失败/异常一律返回 false，不抛出——17 个存量调用点依赖此契约）
	 */
	public static Boolean sendTemplateMsg(String templateName, String displayName,
			String body, String loginName, String openId, String url) {
		try {
			log.info("【微信推送开始】templateName: {}, loginName: {}", templateName, loginName);

			// 参数校验
			validateParameters(body);

			// 构建消息数据
			JSONObject dataObj = buildMessageData(displayName, body);

			// 构建请求参数
			JSONObject parameter = buildRequestParameter(templateName, loginName, openId, url, dataObj);

			// 发送HTTP请求
			String result = sendHttpRequest(parameter);

			// 解析响应结果
			return parseResponse(result);

		} catch (Exception e) {
			log.error("微信消息发送异常: loginName={}, error={}", loginName, e.getMessage(), e);
			return Boolean.FALSE;
		}
	}
```

```java
	/**
	 * 构建消息数据（包内可见以便单测直接验证字段落位）
	 */
	static JSONObject buildMessageData(String displayName, String body) {
		// 构建展示名字段（历史字段名 thing18 语义是「访客姓名」，借用为通用展示名）
		JSONObject userNameObj = JSONUtil.createObj();
		if (StrUtil.isNotEmpty(displayName)) {
			userNameObj.put("value", displayName);
		} else {
			userNameObj.put("value", DEFAULT_SYSTEM_NOTICE);
		}

		// 构建时间字段
		JSONObject timeObj = JSONUtil.createObj();
		timeObj.put("value", DateUtils.convert(LocalDateTime.now()));

		// 构建正文字段
		JSONObject remarkObj = JSONUtil.createObj();
		remarkObj.put("value", body);

		// 组装数据对象
		JSONObject dataObj = JSONUtil.createObj();
		dataObj.put(FIELD_THING18, userNameObj);
		dataObj.put(FIELD_TIME4, timeObj);
		dataObj.put(FIELD_THING14, remarkObj);

		return dataObj;
	}

	/**
	 * 构建请求参数（包内可见以便单测直接验证模板名参数化）
	 */
	static JSONObject buildRequestParameter(String templateName, String loginName,
			String openId, String url, JSONObject dataObj) {
		JSONObject parameter = JSONUtil.createObj();

		if (StrUtil.isNotEmpty(loginName)) {
			parameter.put("loginName", loginName);
		}
		if (StrUtil.isNotEmpty(openId)) {
			parameter.put("openId", openId);
		}

		parameter.put("url", url);
		parameter.put("templateName", templateName);
		parameter.put("data", dataObj);

		return parameter;
	}
```

同时删除原私有常量 `TEMPLATE_NAME`（已被 `DEFAULT_TEMPLATE_NAME` 取代）、原 `sendMsg` 方法体内的旧流程（改为一行委托）、原两个私有构建方法（被上面的包内可见版本取代）。`validateParameters`、`sendHttpRequest`、`encodeParameter`、`parseResponse` 保持不动。

- [ ] **Step 4: 跑测试确认通过**

```bash
mvn -pl smart-module/smart-tool -am test -Dtest=WeChatMsgUtilTest -DfailIfNoTests=false -q
```

预期：`Tests run: 4, Failures: 0, Errors: 0`。

- [ ] **Step 5: 确认无其他编译破坏（smart-tool 被众多模块依赖）**

```bash
mvn -pl smart-module/smart-tool -am install -DskipTests -q
grep -rn "TEMPLATE_NAME" smart-module --include="*.java" | grep -v DEFAULT_TEMPLATE_NAME
```

预期：install 成功；grep 无结果（旧常量名无外部引用残留）。

- [ ] **Step 6: Commit**

```bash
git add smart-module/smart-tool/src/main/java/com/tce/smart/tool/util/WeChatMsgUtil.java \
        smart-module/smart-tool/src/test/java/com/tce/smart/tool/util/WeChatMsgUtilTest.java
git commit -m "feat(tool): parameterize wechat template name and decouple display name from routing login"
```

---

### Task 2: 实体加 msgRetryCount 字段（smart-platform-core）

**Files:**
- Modify: `smart-module/smart-platform/smart-platform-core/src/main/java/com/tce/smart/platform/core/entity/securityzone/SmtSecurityAuthApply.java`

**Interfaces:**
- Produces（Task 3 依赖）: `SmtSecurityAuthApply#getMsgRetryCount()` / `setMsgRetryCount(Integer)`（Lombok `@Data` 生成），映射列 `msg_retry_count`。

- [ ] **Step 1: 加字段**

在 `isMsg` 字段之后追加：

```java
	/**
	 * 微信推送失败次数；达到上限（SmtSecurityAuthApplyServiceImpl.MAX_MSG_RETRY）后
	 * is_msg 置 2（失败放弃）不再重试。列由 manual 脚本
	 * 2026-07-06-security-msg-retry.sql 添加，DEFAULT 0。
	 */
	private Integer msgRetryCount;
```

并把 `isMsg` 的注释从「是否发送短信」修正为实际语义：

```java
	/**
	 * 微信推送状态：0-未发送，1-已发送，2-连续失败达上限已放弃（不再入扫）
	 */
	private Integer isMsg;
```

- [ ] **Step 2: 编译验证**

```bash
mvn -pl smart-module/smart-platform/smart-platform-core -am install -DskipTests -q
```

预期：BUILD SUCCESS。

- [ ] **Step 3: Commit**

```bash
git add smart-module/smart-platform/smart-platform-core/src/main/java/com/tce/smart/platform/core/entity/securityzone/SmtSecurityAuthApply.java
git commit -m "feat(platform): add msg_retry_count field to security auth apply entity"
```

---

### Task 3: sendMessage() 重试治理与正文重写（smart-platform-biz）

**Files:**
- Modify: `smart-module/smart-platform/smart-platform-biz/src/main/java/com/tce/smart/platform/service/securityzone/impl/SmtSecurityAuthApplyServiceImpl.java:457-494`（`sendMessage()` 整体替换）
- Test: `smart-module/smart-platform/smart-platform-biz/src/test/java/com/tce/smart/platform/service/securityzone/SecurityAuthSendMessageTest.java`（新建）

**Interfaces:**
- Consumes: Task 1 的 `WeChatMsgUtil.sendTemplateMsg(templateName, displayName, body, loginName, openId, url)` 与 `WeChatMsgUtil.DEFAULT_TEMPLATE_NAME`；Task 2 的 `msgRetryCount`。
- Produces: `protected Boolean pushWeChatMsg(String displayName, String body, String badge)`（测试 seam，子类可覆写）；常量 `MAX_MSG_RETRY = 3`、`MSG_SEND_ABANDONED = 2`。

**测试策略说明**（Mockito 2.x 无 mockStatic）：静态调用 `WeChatMsgUtil.sendTemplateMsg` 收敛进 protected 实例方法 `pushWeChatMsg`，单测用子类覆写该方法记录入参、返回脚本化结果——测行为（状态机、正文、姓名落位）不测实现。仿照同目录 `SmtSecurityAuthApplyClaimTest` 的既有做法：`@BeforeClass` 预热 MyBatis-Plus lambda 缓存、反射 `setField` 注入 mock。

- [ ] **Step 1: 写失败的单测**

新建 `SecurityAuthSendMessageTest.java`：

```java
package com.tce.smart.platform.service.securityzone;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.tce.smart.platform.core.entity.SmtMsgTemplate;
import com.tce.smart.platform.core.entity.SmtStaff;
import com.tce.smart.platform.core.entity.securityzone.SmtSecurityAuthApply;
import com.tce.smart.platform.core.mapper.SmtSecurityAuthApplyMapper;
import com.tce.smart.platform.core.service.SmtMsgTemplateService;
import com.tce.smart.platform.service.SmtStaffService;
import com.tce.smart.platform.service.securityzone.impl.SmtSecurityAuthApplyServiceImpl;
import com.tce.smart.tool.enums.DeviceDownStatusEnum;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * sendMessage() 重试治理单测（spec docs/superpowers/specs/2026-07-06-security-auth-wechat-msg-redesign.md §3）。
 * 覆盖：成功置 isMsg=1、失败计数、达上限置 isMsg=2 放弃、员工缺失按失败计、单条异常不中断整轮。
 * 静态调用 WeChatMsgUtil 通过覆写 protected pushWeChatMsg 隔离（Mockito 2.x 无 mockStatic）。
 */
public class SecurityAuthSendMessageTest {

	/** 手动预热 MyBatis-Plus lambda 缓存（同 SmtSecurityAuthApplyClaimTest 做法） */
	@BeforeClass
	public static void initMybatisPlusLambdaCache() {
		TableInfoHelper.initTableInfo(new MapperBuilderAssistant(new MybatisConfiguration(), ""), SmtSecurityAuthApply.class);
	}

	/** 可测子类：覆写推送 seam，记录入参并按脚本返回结果 */
	private static class TestableService extends SmtSecurityAuthApplyServiceImpl {
		final List<String> pushedDisplayNames = new ArrayList<>();
		final List<String> pushedBodies = new ArrayList<>();
		Boolean pushResult = Boolean.TRUE;

		@Override
		protected Boolean pushWeChatMsg(String displayName, String body, String badge) {
			pushedDisplayNames.add(displayName);
			pushedBodies.add(body);
			return pushResult;
		}
	}

	private SmtSecurityAuthApplyMapper applyMapper;
	private SmtSecurityTaskDetailsService taskDetailsService;
	private SmtStaffService staffService;
	private SmtMsgTemplateService msgTemplateService;
	private TestableService service;

	@Before
	public void setUp() throws Exception {
		applyMapper = mock(SmtSecurityAuthApplyMapper.class);
		taskDetailsService = mock(SmtSecurityTaskDetailsService.class);
		staffService = mock(SmtStaffService.class);
		msgTemplateService = mock(SmtMsgTemplateService.class);

		service = new TestableService();
		setField(service, "baseMapper", applyMapper);
		setField(service, "smtSecurityTaskDetailsService", taskDetailsService);
		setField(service, "smtStaffService", staffService);
		setField(service, "smtMsgTemplateService", msgTemplateService);
	}

	private void setField(Object target, String name, Object value) throws Exception {
		Class<?> type = target.getClass();
		while (type != null) {
			try {
				Field field = type.getDeclaredField(name);
				field.setAccessible(true);
				field.set(target, value);
				return;
			} catch (NoSuchFieldException ignored) {
				type = type.getSuperclass();
			}
		}
		throw new NoSuchFieldException(name + " on " + target.getClass());
	}

	/** 构造一条「已下发、未推送」的申请单并布好默认桩：员工存在、模板存在、无下发中明细 */
	private SmtSecurityAuthApply stubHappyPathApply() {
		SmtSecurityAuthApply apply = new SmtSecurityAuthApply();
		apply.setId(1L);
		apply.setApplyBadge("8056297");
		apply.setTotalNum(5);
		apply.setIsMsg(0);
		apply.setMsgRetryCount(0);
		when(applyMapper.selectList(any())).thenReturn(Collections.singletonList(apply));
		// 无下发中明细（IN_WORK=0），失败明细 2 条 → 成功 3/共 5
		when(taskDetailsService.getCount(1L, DeviceDownStatusEnum.IN_WORK.getCode())).thenReturn(0);
		when(taskDetailsService.getCount(1L, DeviceDownStatusEnum.FAIL.getCode())).thenReturn(2);
		SmtStaff staff = new SmtStaff();
		staff.setName("张三");
		when(staffService.getSimpleSttaffByBadge("8056297")).thenReturn(staff);
		SmtMsgTemplate template = new SmtMsgTemplate();
		template.setTempContent("保密权限下发完成 成功{成功数量}/共{总数量}");
		when(msgTemplateService.selectByTempCode(any())).thenReturn(template);
		when(applyMapper.updateById(any())).thenReturn(1);
		return apply;
	}

	/** 成功：正文按模板渲染成功数（总-失败）、thing18 用姓名、isMsg 置 1、失败计数不动 */
	@Test
	public void sendMessage_success_rendersBodyAndMarksSent() {
		SmtSecurityAuthApply apply = stubHappyPathApply();
		service.pushResult = Boolean.TRUE;

		service.sendMessage();

		assertEquals(Collections.singletonList("保密权限下发完成 成功3/共5"), service.pushedBodies);
		assertEquals(Collections.singletonList("张三"), service.pushedDisplayNames);
		assertEquals("成功后 isMsg 应置 1", Integer.valueOf(1), apply.getIsMsg());
		assertEquals("成功不应增加失败计数", Integer.valueOf(0), apply.getMsgRetryCount());
		verify(applyMapper).updateById(apply);
	}

	/** 首次失败：isMsg 保持 0（下一轮重扫）、失败计数 +1 */
	@Test
	public void sendMessage_firstFailure_incrementsRetryAndStaysUnsent() {
		SmtSecurityAuthApply apply = stubHappyPathApply();
		service.pushResult = Boolean.FALSE;

		service.sendMessage();

		assertEquals("失败未达上限 isMsg 应保持 0", Integer.valueOf(0), apply.getIsMsg());
		assertEquals(Integer.valueOf(1), apply.getMsgRetryCount());
		verify(applyMapper).updateById(apply);
	}

	/** 第 3 次失败达上限：isMsg 置 2（失败放弃），不再入扫 */
	@Test
	public void sendMessage_thirdFailure_abandonsWithTerminalState() {
		SmtSecurityAuthApply apply = stubHappyPathApply();
		apply.setMsgRetryCount(2);
		service.pushResult = Boolean.FALSE;

		service.sendMessage();

		assertEquals("达上限应置终态 2", Integer.valueOf(2), apply.getIsMsg());
		assertEquals(Integer.valueOf(3), apply.getMsgRetryCount());
	}

	/** 历史数据 msgRetryCount 为 null（加列前的存量行）：按 0 起算，不 NPE */
	@Test
	public void sendMessage_nullRetryCount_treatedAsZero() {
		SmtSecurityAuthApply apply = stubHappyPathApply();
		apply.setMsgRetryCount(null);
		service.pushResult = Boolean.FALSE;

		service.sendMessage();

		assertEquals(Integer.valueOf(1), apply.getMsgRetryCount());
		assertEquals(Integer.valueOf(0), apply.getIsMsg());
	}

	/** 员工查不到：按一次失败计数、不推送（修复原 NPE 卡死整轮的 bug） */
	@Test
	public void sendMessage_staffMissing_countsAsFailureWithoutPush() {
		SmtSecurityAuthApply apply = stubHappyPathApply();
		when(staffService.getSimpleSttaffByBadge("8056297")).thenReturn(null);

		service.sendMessage();

		assertTrue("员工缺失不应推送", service.pushedBodies.isEmpty());
		assertEquals(Integer.valueOf(1), apply.getMsgRetryCount());
		verify(applyMapper).updateById(apply);
	}

	/** 模板缺失：按一次失败计数、不推送（快速失败，留告警日志人工排查） */
	@Test
	public void sendMessage_templateMissing_countsAsFailureWithoutPush() {
		SmtSecurityAuthApply apply = stubHappyPathApply();
		when(msgTemplateService.selectByTempCode(any())).thenReturn(null);

		service.sendMessage();

		assertTrue(service.pushedBodies.isEmpty());
		assertEquals(Integer.valueOf(1), apply.getMsgRetryCount());
	}

	/** 还有下发中明细（IN_WORK>0）：跳过本单不推送也不计失败（结果未定型） */
	@Test
	public void sendMessage_inWorkDetailsRemain_skipsWithoutCounting() {
		SmtSecurityAuthApply apply = stubHappyPathApply();
		when(taskDetailsService.getCount(1L, DeviceDownStatusEnum.IN_WORK.getCode())).thenReturn(1);

		service.sendMessage();

		assertTrue(service.pushedBodies.isEmpty());
		assertEquals(Integer.valueOf(0), apply.getMsgRetryCount());
		verify(applyMapper, never()).updateById(any(SmtSecurityAuthApply.class));
	}

	/** 单条异常不中断整轮：第一单 syncTaskStatus 抛异常，第二单仍正常推送 */
	@Test
	public void sendMessage_oneApplyThrows_othersStillProcessed() {
		SmtSecurityAuthApply broken = new SmtSecurityAuthApply();
		broken.setId(1L);
		broken.setApplyBadge("bad");
		broken.setTotalNum(1);
		SmtSecurityAuthApply ok = new SmtSecurityAuthApply();
		ok.setId(2L);
		ok.setApplyBadge("8056297");
		ok.setTotalNum(5);
		ok.setMsgRetryCount(0);
		when(applyMapper.selectList(any())).thenReturn(java.util.Arrays.asList(broken, ok));
		doThrow(new RuntimeException("sync boom")).when(taskDetailsService).syncTaskStatus(1L);
		when(taskDetailsService.getCount(2L, DeviceDownStatusEnum.IN_WORK.getCode())).thenReturn(0);
		when(taskDetailsService.getCount(2L, DeviceDownStatusEnum.FAIL.getCode())).thenReturn(0);
		SmtStaff staff = new SmtStaff();
		staff.setName("李四");
		when(staffService.getSimpleSttaffByBadge("8056297")).thenReturn(staff);
		SmtMsgTemplate template = new SmtMsgTemplate();
		template.setTempContent("保密权限下发完成 成功{成功数量}/共{总数量}");
		when(msgTemplateService.selectByTempCode(any())).thenReturn(template);
		when(applyMapper.updateById(any())).thenReturn(1);
		service.pushResult = Boolean.TRUE;

		service.sendMessage();

		assertEquals("第二单应正常推送", Collections.singletonList("保密权限下发完成 成功5/共5"), service.pushedBodies);
		assertEquals(Integer.valueOf(1), ok.getIsMsg());
	}
}
```

注意：`SmtStaff` 若无公开 `setName`（Lombok `@Data` 应生成），执行时如遇编译问题改用 Builder 或反射赋值，以实际实体为准。

- [ ] **Step 2: 跑测试确认失败**

```bash
mvn -pl smart-module/smart-platform/smart-platform-biz -am test -Dtest=SecurityAuthSendMessageTest -DfailIfNoTests=false -q
```

预期：编译失败（`pushWeChatMsg` 不存在）或多数用例 FAIL（旧逻辑无重试计数）。

- [ ] **Step 3: 重写 sendMessage()**

在 `SmtSecurityAuthApplyServiceImpl` 常量区（`OA_RECONCILE_CURSOR_KEY` 之后）追加：

```java
	/** 微信推送失败重试上限：定时任务 20 分钟一轮即天然重试间隔，3 次后放弃（spec §3） */
	private static final int MAX_MSG_RETRY = 3;

	/** isMsg 终态：连续失败达上限后放弃，不再入扫（0=未发送，1=已发送，2=失败放弃） */
	private static final int MSG_SEND_ABANDONED = 2;
```

用以下实现整体替换 457-494 行的 `sendMessage()`：

```java
	@Override
	public void sendMessage() {
		// 获得已下发且未推送微信的数据（isMsg=2 失败放弃的终态单天然不入扫）
		List<SmtSecurityAuthApply> applyList = this.list(Wrappers.<SmtSecurityAuthApply>query().lambda()
				.eq(SmtSecurityAuthApply::getDeviceStatus, DeviceDownStatusEnum.ALRAEDY.getCode())
				.eq(SmtSecurityAuthApply::getIsMsg, OneOrZeroEnum.ZERO.getCode()));
		if (CollUtil.isEmpty(applyList)) {
			return;
		}
		for (SmtSecurityAuthApply apply : applyList) {
			// 单条 try 包住全流程：任何一单异常不得中断整轮
			// （修复原实现 getSimpleSttaffByBadge 返回 null 时 NPE 卡死其后所有单的 bug）
			try {
				smtSecurityTaskDetailsService.syncTaskStatus(apply.getId());
				Integer initNum = smtSecurityTaskDetailsService.getCount(apply.getId(), DeviceDownStatusEnum.IN_WORK.getCode());
				if (initNum > 0) {
					// 还有下发中的明细，结果未定型：本轮跳过，不推送也不计失败
					continue;
				}
				boolean sent = trySendSecurityMsg(apply);
				if (sent) {
					apply.setIsMsg(OneOrZeroEnum.ONE.getCode());
				} else {
					// 失败计数 +1；历史存量行加列前为 null，按 0 起算
					int retryCount = (apply.getMsgRetryCount() == null ? 0 : apply.getMsgRetryCount()) + 1;
					apply.setMsgRetryCount(retryCount);
					if (retryCount >= MAX_MSG_RETRY) {
						// 达上限置终态放弃，封死无限重发；告警日志留人工排查线索
						apply.setIsMsg(MSG_SEND_ABANDONED);
						log.warn("保密权限微信推送连续失败达上限，放弃重试：processId={}, applyBadge={}, retryCount={}",
								apply.getProcessId(), apply.getApplyBadge(), retryCount);
					}
				}
				this.updateById(apply);
			} catch (Exception e) {
				// 异常单不计失败次数（与「明确发送失败」区分），下一轮重扫自然重试
				log.error("保密权限微信推送处理异常：processId={}, applyBadge={}",
						apply.getProcessId(), apply.getApplyBadge(), e);
			}
		}
	}

	/**
	 * 尝试推送单条保密权限下发结果。
	 * 正文用 smt_msg_template 的 WECHAT_SECURITY_11101 渲染（20 字内，spec §3）：
	 * 「保密权限下发完成 成功{成功数量}/共{总数量}」，thing18 显示申请人姓名。
	 *
	 * @return true=中转服务确认发送成功；false=员工/模板缺失或发送失败（计入失败次数）
	 */
	private boolean trySendSecurityMsg(SmtSecurityAuthApply apply) {
		SmtStaff staff = smtStaffService.getSimpleSttaffByBadge(apply.getApplyBadge());
		if (staff == null) {
			log.warn("保密权限微信推送查不到员工，按一次失败计数：applyBadge={}, processId={}",
					apply.getApplyBadge(), apply.getProcessId());
			return false;
		}
		SmtMsgTemplate template = smtMsgTemplateService.selectByTempCode(SmsTemplateEnum.WECHAT_SECURITY_11101.getCode());
		if (template == null || StrUtil.isEmpty(template.getTempContent())) {
			log.warn("保密权限微信推送模板缺失或内容为空，按一次失败计数：tempCode={}",
					SmsTemplateEnum.WECHAT_SECURITY_11101.getCode());
			return false;
		}
		Integer failNum = smtSecurityTaskDetailsService.getCount(apply.getId(), DeviceDownStatusEnum.FAIL.getCode());
		int totalNum = apply.getTotalNum() == null ? 0 : apply.getTotalNum();
		int successNum = Math.max(0, totalNum - (failNum == null ? 0 : failNum));
		String body = template.getTempContent()
				.replace("{成功数量}", String.valueOf(successNum))
				.replace("{总数量}", String.valueOf(totalNum));
		return Boolean.TRUE.equals(pushWeChatMsg(staff.getName(), body, apply.getApplyBadge()));
	}

	/**
	 * 微信推送 seam：静态调用收敛于此，protected 以便单测子类覆写隔离
	 * （Mockito 2.x 无 mockStatic）。本轮仍走默认模板壳，运维确认可用模板清单后
	 * 换模板只改这里的第一个入参。
	 */
	protected Boolean pushWeChatMsg(String displayName, String body, String badge) {
		return WeChatMsgUtil.sendTemplateMsg(WeChatMsgUtil.DEFAULT_TEMPLATE_NAME, displayName, body, badge, null, null);
	}
```

被删除的旧逻辑：`workFlowName` 拼接（`"XCAJ02-许昌裕同保密权限申请表-"...`）、`{申请人}/{OA单标题}/{失败数量}` 占位符替换、循环外层无保护的 `staffName.getName()`。检查 `DateUtils` import 是否仍被本文件其他方法使用（`updateOaStatusTask` 等有用到则保留）。

- [ ] **Step 4: 跑测试确认通过**

```bash
mvn -pl smart-module/smart-platform/smart-platform-biz -am test -Dtest=SecurityAuthSendMessageTest -DfailIfNoTests=false -q
```

预期：`Tests run: 8, Failures: 0, Errors: 0`。

- [ ] **Step 5: 跑同包既有回归测试**

```bash
mvn -pl smart-module/smart-platform/smart-platform-biz -am test -Dtest='Smt*Test,Security*Test' -DfailIfNoTests=false -q
```

预期：全部 PASS（`SmtSecurityAuthApplyClaimTest`、`SecurityAuthManualDownTest`、`SecurityAuthOaReconcileTest` 等不受影响）。

- [ ] **Step 6: Commit**

```bash
git add smart-module/smart-platform/smart-platform-biz/src/main/java/com/tce/smart/platform/service/securityzone/impl/SmtSecurityAuthApplyServiceImpl.java \
        smart-module/smart-platform/smart-platform-biz/src/test/java/com/tce/smart/platform/service/securityzone/SecurityAuthSendMessageTest.java
git commit -m "feat(platform): cap security auth wechat push retries and compress msg body"
```

---

### Task 4: manual SQL 脚本（Oracle PL/SQL 匿名块）

**Files:**
- Create: `smart-module/database/manual/2026-07-06-security-msg-retry.sql`
- Create: `smart-module/database/manual/2026-07-06-security-msg-retry-rollback.sql`
- Create: `smart-module/database/manual/2026-07-06-security-msg-template-content.sql`
- Create: `smart-module/database/manual/2026-07-06-security-msg-template-content-rollback.sql`

**Interfaces:**
- Consumes: Task 2 实体字段 `msgRetryCount` ↔ 列 `MSG_RETRY_COUNT`。
- 约定：全部脚本目标模式 `tech_platform`；风格对齐 `2026-07-01-isc-batch-model.sql`（USER_TAB_COLUMNS 幂等检查、EXECUTE IMMEDIATE、DBMS_OUTPUT、末尾无 `/`）。

- [ ] **Step 1: 写加列脚本**

`2026-07-06-security-msg-retry.sql`：

```sql
-- 手工数据库变更：保密权限微信推送失败重试计数（spec 2026-07-06-security-auth-wechat-msg-redesign §4）。
-- 目标模式：tech_platform（smt_* 业务表统一归属，用错账号列会加到错误模式）。
-- 本脚本可先于代码发布执行：DEFAULT 0，旧代码不感知新列，无兼容风险。
-- Oracle 低版本不支持 ADD COLUMN IF NOT EXISTS，使用 PL/SQL 匿名块做存在性判断。
-- 执行时请整段执行本文件，不要按分号逐句执行。

DECLARE
	V_COUNT NUMBER;
BEGIN
	-- 检查 smt_security_auth_apply 表中 msg_retry_count 列是否存在
	SELECT COUNT(1) INTO V_COUNT
	FROM USER_TAB_COLUMNS
	WHERE TABLE_NAME = 'SMT_SECURITY_AUTH_APPLY'
	  AND COLUMN_NAME = 'MSG_RETRY_COUNT';
	IF V_COUNT = 0 THEN
		EXECUTE IMMEDIATE 'ALTER TABLE SMT_SECURITY_AUTH_APPLY ADD (MSG_RETRY_COUNT NUMBER(4) DEFAULT 0 NOT NULL)';
		DBMS_OUTPUT.PUT_LINE('已添加 SMT_SECURITY_AUTH_APPLY.MSG_RETRY_COUNT 列');
	ELSE
		DBMS_OUTPUT.PUT_LINE('SMT_SECURITY_AUTH_APPLY.MSG_RETRY_COUNT 列已存在，跳过');
	END IF;

	-- 列中文备注
	EXECUTE IMMEDIATE q'[COMMENT ON COLUMN SMT_SECURITY_AUTH_APPLY.MSG_RETRY_COUNT IS '微信推送失败次数，达上限（3）后 is_msg 置 2 失败放弃']';

	DBMS_OUTPUT.PUT_LINE('保密权限微信推送重试计数列初始化完成');
END;
```

- [ ] **Step 2: 写加列回滚脚本**

`2026-07-06-security-msg-retry-rollback.sql`：

```sql
-- 回滚脚本：删除保密权限微信推送失败重试计数列。
-- 目标模式：tech_platform。
-- 注意：须先回滚应用代码（实体仍有 msgRetryCount 字段时删列会导致 updateById 报 ORA-00904）。
-- 执行时请整段执行本文件，不要按分号逐句执行。

DECLARE
	V_COUNT NUMBER;
BEGIN
	SELECT COUNT(1) INTO V_COUNT
	FROM USER_TAB_COLUMNS
	WHERE TABLE_NAME = 'SMT_SECURITY_AUTH_APPLY'
	  AND COLUMN_NAME = 'MSG_RETRY_COUNT';
	IF V_COUNT = 1 THEN
		EXECUTE IMMEDIATE 'ALTER TABLE SMT_SECURITY_AUTH_APPLY DROP COLUMN MSG_RETRY_COUNT';
		DBMS_OUTPUT.PUT_LINE('已删除 SMT_SECURITY_AUTH_APPLY.MSG_RETRY_COUNT 列');
	ELSE
		DBMS_OUTPUT.PUT_LINE('SMT_SECURITY_AUTH_APPLY.MSG_RETRY_COUNT 列不存在，跳过');
	END IF;
END;
```

- [ ] **Step 3: 写模板内容更新脚本**

`2026-07-06-security-msg-template-content.sql`：

```sql
-- 手工数据库变更：保密权限微信推送正文模板压缩到 20 字内（微信 thing 字段截断上限）。
-- 目标模式：tech_platform。
-- ！！执行窗口约束：必须与新代码同窗口生效（新旧代码占位符集合不同：
-- 旧={申请人}{OA单标题}{失败数量}{总数量}，新={成功数量}{总数量}），
-- 建议顺序：停 supplierAuthMsg 任务开关 → 发布应用 → 执行本脚本 → 开开关。
-- 脚本会先打印旧内容，请把输出记录到变更单，回滚时按记录值恢复。
-- 执行时请整段执行本文件，不要按分号逐句执行。

DECLARE
	V_OLD_CONTENT SMT_MSG_TEMPLATE.TEMP_CONTENT%TYPE;
	V_COUNT NUMBER;
BEGIN
	SELECT COUNT(1) INTO V_COUNT
	FROM SMT_MSG_TEMPLATE
	WHERE TEMP_CODE = '11101';
	IF V_COUNT = 0 THEN
		DBMS_OUTPUT.PUT_LINE('未找到 TEMP_CODE=11101（保密区门禁权限申请结果通知）的模板，请人工核实后再执行');
		RETURN;
	END IF;

	-- 先打印旧内容供回滚记录
	SELECT TEMP_CONTENT INTO V_OLD_CONTENT
	FROM SMT_MSG_TEMPLATE
	WHERE TEMP_CODE = '11101';
	DBMS_OUTPUT.PUT_LINE('旧模板内容（回滚依据，请记录到变更单）：' || V_OLD_CONTENT);

	UPDATE SMT_MSG_TEMPLATE
	SET TEMP_CONTENT = '保密权限下发完成 成功{成功数量}/共{总数量}'
	WHERE TEMP_CODE = '11101';
	DBMS_OUTPUT.PUT_LINE('已更新模板内容，影响行数：' || SQL%ROWCOUNT);

	COMMIT;
END;
```

- [ ] **Step 4: 写模板内容回滚脚本**

`2026-07-06-security-msg-template-content-rollback.sql`：

```sql
-- 回滚脚本：恢复保密权限微信推送正文模板为升级前内容。
-- 目标模式：tech_platform。
-- ！！旧内容以升级脚本执行时 DBMS_OUTPUT 打印并记录在变更单里的值为准，
-- 执行前把下方 V_OLD_CONTENT 的占位值替换为变更单记录的原文。
-- 执行时请整段执行本文件，不要按分号逐句执行。

DECLARE
	-- TODO(执行人)：替换为变更单记录的升级前模板原文
	V_OLD_CONTENT SMT_MSG_TEMPLATE.TEMP_CONTENT%TYPE := '<替换为变更单记录的升级前模板原文>';
BEGIN
	IF V_OLD_CONTENT = '<替换为变更单记录的升级前模板原文>' THEN
		DBMS_OUTPUT.PUT_LINE('未替换旧内容占位值，拒绝执行：请先从变更单取回升级前模板原文');
		RETURN;
	END IF;

	UPDATE SMT_MSG_TEMPLATE
	SET TEMP_CONTENT = V_OLD_CONTENT
	WHERE TEMP_CODE = '11101';
	DBMS_OUTPUT.PUT_LINE('已恢复模板内容，影响行数：' || SQL%ROWCOUNT);

	COMMIT;
END;
```

- [ ] **Step 5: Commit**

```bash
git add smart-module/database/manual/2026-07-06-security-msg-retry.sql \
        smart-module/database/manual/2026-07-06-security-msg-retry-rollback.sql \
        smart-module/database/manual/2026-07-06-security-msg-template-content.sql \
        smart-module/database/manual/2026-07-06-security-msg-template-content-rollback.sql
git commit -m "feat(database): add msg retry count column and compressed wechat template content scripts"
```

---

### Task 5: 全量验证 + PR

**Files:** 无新改动，验证与集成。

- [ ] **Step 1: 受影响模块全量编译 + 测试**

```bash
mvn -pl smart-module/smart-tool,smart-module/smart-platform/smart-platform-core,smart-module/smart-platform/smart-platform-biz -am install -q
```

预期：BUILD SUCCESS，所有测试 PASS（此命令不带 skipTests，跑全量单测）。

- [ ] **Step 2: 确认 smart-schedule 等依赖方编译不破**

```bash
mvn -pl smart-module/smart-schedule -am package -DskipTests -q
```

预期：BUILD SUCCESS（`ISCDeviceTaskServiceImpl` 等旧调用点走原 `sendMsg` 签名，不受影响）。

- [ ] **Step 3: 代码评审自查**

按全局规则跑一次代码评审（bug、回归、边界、测试缺口）；确认暂存区无构建产物/日志/环境文件。

- [ ] **Step 4: 推分支并建 PR**

```bash
git push -u origin feat/security-auth-wechat-msg
gh pr create --title "feat(platform): cap security auth wechat push retries and compress msg body" --body "$(cat <<'EOF'
## Summary
Production wechat push for security-zone auth results had two issues: (1) every business flow piggybacks on the "visitor entry/exit" official-account template with mismatched fields, and the message body is truncated by WeChat's 20-char `thing` field limit, making notifications unreadable; (2) `sendMessage()` retries failed pushes every 20 minutes forever with no cap, risking unlimited spam if the relay service (xchr.szyuto.com) changes its response format.

This PR (scope: security auth push only; other 17 call sites unchanged):
- Rewrites the push body to fit within 20 chars: `保密权限下发完成 成功X/共Y`, and shows the applicant's name instead of badge number.
- Caps push retries at 3 (20-min scheduler interval acts as natural backoff); adds `msg_retry_count` column and terminal state `is_msg=2` (abandoned) with warn logs.
- Fixes a latent NPE: a missing staff record used to crash the whole loop and block all subsequent applies.
- Parameterizes the template name in `WeChatMsgUtil` (`sendTemplateMsg`), decoupling display name from routing login; old `sendMsg` signature delegates unchanged.

## Changes
- `smart-tool` `WeChatMsgUtil`: new `sendTemplateMsg`, public `DEFAULT_TEMPLATE_NAME`, package-private builders for tests.
- `smart-platform-core` `SmtSecurityAuthApply`: new `msgRetryCount` field; fixed `isMsg` comment semantics.
- `smart-platform-biz` `SmtSecurityAuthApplyServiceImpl.sendMessage()`: retry state machine + per-apply exception isolation + protected `pushWeChatMsg` seam.
- `database/manual`: Oracle PL/SQL scripts (add column + template content update, with rollbacks), schema `tech_platform`.

## Testing
- `mvn -pl smart-module/smart-tool -am test` (WeChatMsgUtilTest: field mapping, template param, default-name regression)
- `mvn -pl smart-module/smart-platform/smart-platform-biz -am test` (SecurityAuthSendMessageTest: 8 cases covering success/failure/cap/null-staff/null-template/in-work skip/loop isolation; existing security-zone suites green)
- `mvn -pl smart-module/smart-schedule -am package -DskipTests` (downstream callers compile)

## Risks
- Deploy ordering matters for the template content script: old and new code use different placeholder sets. Sequence: run add-column DDL (safe anytime) → stop `supplierAuthMsg` switch → deploy → run template content update → re-enable switch.
- Backlog applies with `is_msg=0` will retry up to 3 more times after deploy, then terminate — one-time convergence, intended.
- Template shell is still "访客出入园提醒" pending ops confirmation of available templates; switching later is a one-arg change at the `pushWeChatMsg` seam.

🤖 Generated with [Claude Code](https://claude.com/claude-code)
EOF
)"
```

- [ ] **Step 5: 收尾提醒（写进 PR 评论或回复用户）**

生产上线检查单（代码合并 ≠ 生效）：
1. `tech_platform` 账号执行 `2026-07-06-security-msg-retry.sql`（可提前）。
2. 停 `supplierAuthMsg` 开关 → 发布 smart-platform / smart-tool 依赖服务 → `tech_platform` 执行 `2026-07-06-security-msg-template-content.sql`（记录旧内容到变更单）→ 开开关。
3. 观察一轮（20 分钟）：确认成功单 isMsg=1、失败单计数递增、达 3 次后出现「放弃重试」warn 日志。

---

## Self-Review 记录

- **Spec 覆盖**：§1 改动范围→Task 1-4；§2 参数化→Task 1；§3 重试治理/正文/NPE 修复→Task 3；§4 数据脚本（含拆分执行窗口）→Task 4；§5 测试与兼容→各 Task 内嵌 + Task 5；部署注意→Task 5 Step 5。无缺口。
- **占位符**：模板回滚脚本中的 `<替换为...>` 是面向执行人的运行时占位（脚本自带防呆拒绝执行），非计划缺口。
- **类型一致性**：`sendTemplateMsg(String,String,String,String,String,String)` 在 Task 1 定义、Task 3 `pushWeChatMsg` 消费，参数顺序一致；`msgRetryCount`（Task 2）↔ `MSG_RETRY_COUNT NUMBER(4)`（Task 4）；`MSG_SEND_ABANDONED=2` 与脚本注释「is_msg 置 2」一致。
