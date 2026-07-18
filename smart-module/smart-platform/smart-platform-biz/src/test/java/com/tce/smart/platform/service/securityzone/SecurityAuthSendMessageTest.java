package com.tce.smart.platform.service.securityzone;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
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
import org.mockito.ArgumentCaptor;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * sendMessage() 重试治理单测。
 * 覆盖：成功置 isMsg=1、失败计数、达上限置 isMsg=2 放弃、员工缺失按失败计、单条异常不中断整轮。
 * 静态调用 WeChatMsgUtil 通过覆写 protected pushWeChatMsg 隔离（Mockito 2.x 无 mockStatic）。
 *
 * 状态推进已改为条件化更新（CAS：is_msg 0→1、计数带旧值条件），不再走 updateById 全字段回写，
 * 因此断言从「验证内存实体字段」改为「用 ArgumentCaptor 捕获 LambdaUpdateWrapper 核对 set/条件」，
 * 手法与同包 SmtSecurityAuthApplyClaimTest 一致。
 */
@SuppressWarnings("unchecked")
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

	/**
	 * 从 LambdaUpdateWrapper#getSqlSet() 里为指定列解析出其绑定的占位符名，
	 * 再从 paramNameValuePairs 里按该占位符名精确取值（参考同包 SmtSecurityAuthApplyClaimTest#boundParamFor，
	 * 避免多列共用 contains() 断言互相掩护）。
	 */
	private String boundParamFor(String sqlSet, String column) {
		Pattern pattern = Pattern.compile(Pattern.quote(column) + "=#\\{ew\\.paramNameValuePairs\\.(MPGENVAL\\d+)\\}");
		Matcher matcher = pattern.matcher(sqlSet);
		assertTrue("sqlSet 中未找到列 " + column + " 对应的绑定占位符：" + sqlSet, matcher.find());
		return matcher.group(1);
	}

	/** 捕获本轮唯一一次 baseMapper.update(null, wrapper) 的 LambdaUpdateWrapper */
	private LambdaUpdateWrapper<SmtSecurityAuthApply> captureUpdateWrapper() {
		ArgumentCaptor<LambdaUpdateWrapper<SmtSecurityAuthApply>> captor = ArgumentCaptor.forClass(LambdaUpdateWrapper.class);
		verify(applyMapper).update(isNull(), captor.capture());
		return captor.getValue();
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
		// 无下发中明细（IN_WORK=0）；明细口径：成功 3、失败 2 → 成功3/共5
		when(taskDetailsService.getCount(1L, DeviceDownStatusEnum.IN_WORK.getCode())).thenReturn(0);
		when(taskDetailsService.getCount(1L, DeviceDownStatusEnum.SUCCESS.getCode())).thenReturn(3);
		when(taskDetailsService.getCount(1L, DeviceDownStatusEnum.FAIL.getCode())).thenReturn(2);
		SmtStaff staff = new SmtStaff();
		staff.setName("张三");
		when(staffService.getSimpleSttaffByBadge("8056297")).thenReturn(staff);
		SmtMsgTemplate template = new SmtMsgTemplate();
		template.setTempContent("保密权限下发完成 成功{成功数量}/共{总数量}");
		when(msgTemplateService.selectByTempCode(any())).thenReturn(template);
		// 条件化更新命中（影响 1 行）
		when(applyMapper.update(isNull(), any())).thenReturn(1);
		return apply;
	}

	/** 成功：正文按明细口径渲染（成功3/共5）、thing18 用姓名、CAS 把 is_msg 0→1 */
	@Test
	public void sendMessage_success_rendersBodyAndMarksSent() {
		stubHappyPathApply();
		service.pushResult = Boolean.TRUE;

		service.sendMessage();

		assertEquals(Collections.singletonList("保密权限下发完成 成功3/共5"), service.pushedBodies);
		assertEquals(Collections.singletonList("张三"), service.pushedDisplayNames);

		LambdaUpdateWrapper<SmtSecurityAuthApply> wrapper = captureUpdateWrapper();
		String sqlSet = wrapper.getSqlSet();
		String sqlSegment = wrapper.getSqlSegment().toLowerCase(java.util.Locale.ROOT);
		Map<String, Object> params = wrapper.getParamNameValuePairs();
		// set is_msg=1，且 CAS 条件必须限定 is_msg（防旧快照回退已发送态）
		assertEquals("成功应把 is_msg 置 1", Integer.valueOf(1),
				params.get(boundParamFor(sqlSet, "is_msg")));
		assertTrue("CAS 条件应限定 is_msg", sqlSegment.contains("is_msg"));
	}

	/** 首次失败：CAS set msg_retry_count=1，不置终态、不改 is_msg */
	@Test
	public void sendMessage_firstFailure_incrementsRetryAndStaysUnsent() {
		stubHappyPathApply();
		service.pushResult = Boolean.FALSE;

		service.sendMessage();

		LambdaUpdateWrapper<SmtSecurityAuthApply> wrapper = captureUpdateWrapper();
		String sqlSet = wrapper.getSqlSet();
		Map<String, Object> params = wrapper.getParamNameValuePairs();
		assertEquals("首败应把 msg_retry_count 置 1", Integer.valueOf(1),
				params.get(boundParamFor(sqlSet, "msg_retry_count")));
		// 未达上限不应在 set 中出现 is_msg
		assertFalse("首败不应改 is_msg", sqlSet.contains("is_msg="));
	}

	/** 第 3 次失败达上限：同一条 CAS 同时 set msg_retry_count=3 与 is_msg=2（失败放弃） */
	@Test
	public void sendMessage_thirdFailure_abandonsWithTerminalState() {
		SmtSecurityAuthApply apply = stubHappyPathApply();
		apply.setMsgRetryCount(2);
		service.pushResult = Boolean.FALSE;

		service.sendMessage();

		LambdaUpdateWrapper<SmtSecurityAuthApply> wrapper = captureUpdateWrapper();
		String sqlSet = wrapper.getSqlSet();
		Map<String, Object> params = wrapper.getParamNameValuePairs();
		assertEquals("达上限计数应为 3", Integer.valueOf(3),
				params.get(boundParamFor(sqlSet, "msg_retry_count")));
		assertEquals("达上限应把 is_msg 置终态 2", Integer.valueOf(2),
				params.get(boundParamFor(sqlSet, "is_msg")));
	}

	/** 历史数据 msgRetryCount 为 null（加列前的存量行）：计数条件走 isNull 分支，仍 set 为 1 */
	@Test
	public void sendMessage_nullRetryCount_treatedAsZero() {
		SmtSecurityAuthApply apply = stubHappyPathApply();
		apply.setMsgRetryCount(null);
		service.pushResult = Boolean.FALSE;

		service.sendMessage();

		LambdaUpdateWrapper<SmtSecurityAuthApply> wrapper = captureUpdateWrapper();
		String sqlSet = wrapper.getSqlSet();
		String sqlSegment = wrapper.getSqlSegment().toUpperCase(java.util.Locale.ROOT);
		Map<String, Object> params = wrapper.getParamNameValuePairs();
		assertEquals("null 起算 +1 应为 1", Integer.valueOf(1),
				params.get(boundParamFor(sqlSet, "msg_retry_count")));
		// 旧值为 null 时条件应走 IS NULL 分支，而非等值比较
		assertTrue("null 计数条件应包含 IS NULL", sqlSegment.contains("IS NULL"));
	}

	/** 员工查不到：按一次失败计数（CAS set msg_retry_count=1）、不推送（修复原 NPE 卡死整轮的 bug） */
	@Test
	public void sendMessage_staffMissing_countsAsFailureWithoutPush() {
		stubHappyPathApply();
		when(staffService.getSimpleSttaffByBadge("8056297")).thenReturn(null);

		service.sendMessage();

		assertTrue("员工缺失不应推送", service.pushedBodies.isEmpty());
		LambdaUpdateWrapper<SmtSecurityAuthApply> wrapper = captureUpdateWrapper();
		String sqlSet = wrapper.getSqlSet();
		Map<String, Object> params = wrapper.getParamNameValuePairs();
		assertEquals(Integer.valueOf(1), params.get(boundParamFor(sqlSet, "msg_retry_count")));
	}

	/** 模板缺失：按一次失败计数（CAS set msg_retry_count=1）、不推送（快速失败，留告警日志人工排查） */
	@Test
	public void sendMessage_templateMissing_countsAsFailureWithoutPush() {
		stubHappyPathApply();
		when(msgTemplateService.selectByTempCode(any())).thenReturn(null);

		service.sendMessage();

		assertTrue(service.pushedBodies.isEmpty());
		LambdaUpdateWrapper<SmtSecurityAuthApply> wrapper = captureUpdateWrapper();
		String sqlSet = wrapper.getSqlSet();
		Map<String, Object> params = wrapper.getParamNameValuePairs();
		assertEquals(Integer.valueOf(1), params.get(boundParamFor(sqlSet, "msg_retry_count")));
	}

	/** 还有下发中明细（IN_WORK>0）：跳过本单不推送也不计失败（结果未定型），不触发任何 update */
	@Test
	public void sendMessage_inWorkDetailsRemain_skipsWithoutCounting() {
		stubHappyPathApply();
		when(taskDetailsService.getCount(1L, DeviceDownStatusEnum.IN_WORK.getCode())).thenReturn(1);

		service.sendMessage();

		assertTrue(service.pushedBodies.isEmpty());
		verify(applyMapper, never()).update(any(), any());
	}

	/** 单条异常不中断整轮：第一单 syncTaskStatus 抛异常，第二单仍正常推送并 CAS 置 is_msg=1 */
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
		ok.setIsMsg(0);
		ok.setMsgRetryCount(0);
		when(applyMapper.selectList(any())).thenReturn(java.util.Arrays.asList(broken, ok));
		doThrow(new RuntimeException("sync boom")).when(taskDetailsService).syncTaskStatus(1L);
		when(taskDetailsService.getCount(2L, DeviceDownStatusEnum.IN_WORK.getCode())).thenReturn(0);
		// 明细口径：成功 5、失败 0 → 成功5/共5（数值与旧断言一致，桩改为直接给 SUCCESS 数）
		when(taskDetailsService.getCount(2L, DeviceDownStatusEnum.SUCCESS.getCode())).thenReturn(5);
		when(taskDetailsService.getCount(2L, DeviceDownStatusEnum.FAIL.getCode())).thenReturn(0);
		SmtStaff staff = new SmtStaff();
		staff.setName("李四");
		when(staffService.getSimpleSttaffByBadge("8056297")).thenReturn(staff);
		SmtMsgTemplate template = new SmtMsgTemplate();
		template.setTempContent("保密权限下发完成 成功{成功数量}/共{总数量}");
		when(msgTemplateService.selectByTempCode(any())).thenReturn(template);
		when(applyMapper.update(isNull(), any())).thenReturn(1);
		service.pushResult = Boolean.TRUE;

		service.sendMessage();

		assertEquals("第二单应正常推送", Collections.singletonList("保密权限下发完成 成功5/共5"), service.pushedBodies);
		// 第二单成功应 CAS 把 is_msg 置 1
		LambdaUpdateWrapper<SmtSecurityAuthApply> wrapper = captureUpdateWrapper();
		String sqlSet = wrapper.getSqlSet();
		Map<String, Object> params = wrapper.getParamNameValuePairs();
		assertEquals("第二单成功应把 is_msg 置 1", Integer.valueOf(1),
				params.get(boundParamFor(sqlSet, "is_msg")));
	}
}
