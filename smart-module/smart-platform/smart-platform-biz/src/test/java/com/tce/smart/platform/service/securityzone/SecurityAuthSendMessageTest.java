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
