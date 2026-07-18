package com.tce.smart.platform.service.securityzone;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.tce.smart.platform.core.entity.securityzone.SmtSecurityAuthApply;
import com.tce.smart.platform.core.entity.securityzone.SmtSecurityTaskDetails;
import com.tce.smart.platform.core.mapper.SmtSecurityAuthApplyMapper;
import com.tce.smart.platform.core.mapper.SmtSecurityTaskDetailsMapper;
import com.tce.smart.platform.service.securityzone.impl.SmtSecurityAuthApplyServiceImpl;
import com.tce.smart.platform.service.securityzone.impl.SmtSecurityTaskDetailsServiceImpl;
import com.tce.smart.tool.enums.ApproveListStateEnum;
import com.tce.smart.tool.enums.DeviceDownStatusEnum;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Task 21 单测：claim 抢占 + 明细级原子抢占 + 下发触发修正（spec §3.1.1 / §3.1.3）。
 * 覆盖 claimOaFinalStatus / triggerDownDevice（含内存状态同步回归）、down() 明细级 CAS 抢占。
 * 注：旧 updateStatus(SmtSecurityAuthApply) 已随 Task 25 删除，相关用例已改为直接测 triggerDownDevice。
 */
@SuppressWarnings("unchecked")
public class SmtSecurityAuthApplyClaimTest {

	/**
	 * 手动预热 MyBatis-Plus lambda 缓存：纯单测无 Spring 容器扫描 mapper，
	 * LambdaUpdateWrapper 需要 TableInfoHelper 缓存过的实体元数据才能解析字段
	 * （与 OaCallbackReplayServiceTest / SmtAdmittanceApplyServiceImplTest 同做法）。
	 */
	@BeforeClass
	public static void initMybatisPlusLambdaCache() {
		TableInfoHelper.initTableInfo(new MapperBuilderAssistant(new MybatisConfiguration(), ""), SmtSecurityAuthApply.class);
		TableInfoHelper.initTableInfo(new MapperBuilderAssistant(new MybatisConfiguration(), ""), SmtSecurityTaskDetails.class);
	}

	private SmtSecurityAuthApplyMapper applyMapper;
	private SmtSecurityAuthApplyServiceImpl applyService;
	private SmtSecurityTaskDetailsService taskDetailsService;

	@Before
	public void setUp() throws Exception {
		applyMapper = mock(SmtSecurityAuthApplyMapper.class);
		applyService = new SmtSecurityAuthApplyServiceImpl();
		setField(applyService, "baseMapper", applyMapper);

		taskDetailsService = mock(SmtSecurityTaskDetailsService.class);
		setField(applyService, "smtSecurityTaskDetailsService", taskDetailsService);
	}

	/**
	 * 从 LambdaUpdateWrapper#getSqlSet() 里为指定列解析出其绑定的占位符名，
	 * 再从 paramNameValuePairs 里按该占位符名精确取值（参考 OaCallbackReplayServiceTest#boundParamFor，
	 * 避免多列共用 contains() 断言互相掩护）。
	 */
	private String boundParamFor(String sqlSet, String column) {
		Pattern pattern = Pattern.compile(Pattern.quote(column) + "=#\\{ew\\.paramNameValuePairs\\.(MPGENVAL\\d+)\\}");
		Matcher matcher = pattern.matcher(sqlSet);
		assertTrue("sqlSet 中未找到列 " + column + " 对应的绑定占位符：" + sqlSet, matcher.find());
		return matcher.group(1);
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

	// ========== claimOaFinalStatus ==========

	@Test
	public void claimOaFinalStatus_firstCallSucceeds_returnsTrue() {
		when(applyMapper.update(any(), any())).thenReturn(1);

		boolean claimed = applyService.claimOaFinalStatus(1001L, ApproveListStateEnum.AGREE.getCode());

		assertTrue(claimed);
		ArgumentCaptor<LambdaUpdateWrapper<SmtSecurityAuthApply>> captor = ArgumentCaptor.forClass(LambdaUpdateWrapper.class);
		verify(applyMapper).update(isNull(), captor.capture());
		LambdaUpdateWrapper<SmtSecurityAuthApply> wrapper = captor.getValue();
		String sqlSet = wrapper.getSqlSet();
		Map<String, Object> params = wrapper.getParamNameValuePairs();
		// CAS 条件必须包含 oa_status=0（PENDING），否则重复回调会重复触发下发
		String sqlSegment = wrapper.getSqlSegment().toLowerCase(java.util.Locale.ROOT);
		assertTrue("CAS 条件应限定 oa_status", sqlSegment.contains("oa_status"));
		assertEquals("set 的 oa_status 绑定值应为传入的终态", ApproveListStateEnum.AGREE.getCode(),
				params.get(boundParamFor(sqlSet, "oa_status")));
	}

	@Test
	public void claimOaFinalStatus_alreadyClaimed_returnsFalse() {
		// 已被抢：底层 update 影响行数为 0
		when(applyMapper.update(any(), any())).thenReturn(0);

		boolean claimed = applyService.claimOaFinalStatus(1001L, ApproveListStateEnum.AGREE.getCode());

		assertFalse(claimed);
	}

	// ========== triggerDownDevice ==========

	@Test
	public void triggerDownDevice_downloadThrows_returnsFalseAndDoesNotTouchMainTable() {
		SmtSecurityAuthApply apply = new SmtSecurityAuthApply();
		apply.setId(2001L);
		apply.setApplyBadge("badge-1");
		doThrow(new RuntimeException("device offline")).when(taskDetailsService).downDevice(2001L, "badge-1");

		boolean result = applyService.triggerDownDevice(apply);

		assertFalse(result);
		// 下发异常后不应触碰主表 device_status，交由对账任务重试
		verify(applyMapper, never()).update(any(), any());
	}

	@Test
	public void triggerDownDevice_success_updatesMainTableWithDeviceStatusCas() {
		SmtSecurityAuthApply apply = new SmtSecurityAuthApply();
		apply.setId(2002L);
		apply.setApplyBadge("badge-2");
		when(taskDetailsService.downDevice(2002L, "badge-2")).thenReturn(Boolean.TRUE);
		when(applyMapper.update(any(), any())).thenReturn(1);

		boolean result = applyService.triggerDownDevice(apply);

		assertTrue(result);
		ArgumentCaptor<LambdaUpdateWrapper<SmtSecurityAuthApply>> captor = ArgumentCaptor.forClass(LambdaUpdateWrapper.class);
		verify(applyMapper).update(isNull(), captor.capture());
		LambdaUpdateWrapper<SmtSecurityAuthApply> wrapper = captor.getValue();
		String sqlSegment = wrapper.getSqlSegment().toLowerCase(java.util.Locale.ROOT);
		String sqlSet = wrapper.getSqlSet();
		Map<String, Object> params = wrapper.getParamNameValuePairs();
		// CAS 条件必须限定 device_status=0（WAIT），且 set 的目标值为 ALRAEDY(4)
		assertTrue("CAS 条件应限定 device_status", sqlSegment.contains("device_status"));
		assertEquals("set 的 device_status 绑定值应为 ALRAEDY", DeviceDownStatusEnum.ALRAEDY.getCode(),
				params.get(boundParamFor(sqlSet, "device_status")));
	}

	// ========== triggerDownDevice 内存状态同步回归用例 ==========
	// 注：旧 updateStatus(SmtSecurityAuthApply) 已随 Task 25 删除（Task 22 起生产侧
	// 无调用方，callback handler 已改走 claim 流程），以下用例改为直接调用
	// triggerDownDevice 验证同等的 Critical 回归点：内存字段与 CAS 结果的一致性。

	/**
	 * Critical 回归用例：不 stub triggerDownDevice 内部逻辑，走真实方法路径，
	 * 只 mock 最底层依赖（downDevice 成功、applyMapper.update CAS 返回 true），
	 * 断言调用后传入实体的 deviceStatus 已被同步为 ALRAEDY(4)，
	 * 而不是调用前的旧值 WAIT(0)——否则后续任何基于该实体的 updateById 都会用
	 * 内存里过期的 0 把 CAS 刚写入 DB 的 4 覆盖回去（Task 21 评审确认的 Critical）。
	 * 注意：主表 device_status（本用例断言的 4=ALRAEDY）与明细表
	 * SmtSecurityTaskDetails.status（1=SUCCESS）是两套码表，勿混淆。
	 */
	@Test
	public void triggerDownDevice_success_syncsInMemoryDeviceStatusToAlready() {
		SmtSecurityAuthApply apply = new SmtSecurityAuthApply();
		apply.setId(3003L);
		apply.setApplyBadge("badge-3");
		apply.setOaStatus(ApproveListStateEnum.AGREE.getCode());
		apply.setDeviceStatus(DeviceDownStatusEnum.WAIT.getCode());

		// 底层设备下发成功
		when(taskDetailsService.downDevice(3003L, "badge-3")).thenReturn(Boolean.TRUE);
		// 主表 CAS update（device_status 0->4）返回命中
		when(applyMapper.update(any(), any())).thenReturn(1);

		boolean result = applyService.triggerDownDevice(apply);

		assertTrue(result);
		assertEquals("triggerDownDevice 成功后应把内存字段同步为 ALRAEDY，防止后续误用旧值覆盖 CAS 结果",
				DeviceDownStatusEnum.ALRAEDY.getCode(), apply.getDeviceStatus());
	}

	/**
	 * 反向用例：downDevice 抛异常时不得误推进内存状态，调用后实体
	 * deviceStatus 应保持调用前的原值（这里是 WAIT），交由对账任务重试。
	 */
	@Test
	public void triggerDownDevice_downloadThrows_deviceStatusStaysWait() {
		SmtSecurityAuthApply apply = new SmtSecurityAuthApply();
		apply.setId(3004L);
		apply.setApplyBadge("badge-4");
		apply.setOaStatus(ApproveListStateEnum.AGREE.getCode());
		apply.setDeviceStatus(DeviceDownStatusEnum.WAIT.getCode());

		doThrow(new RuntimeException("device offline")).when(taskDetailsService).downDevice(3004L, "badge-4");

		boolean result = applyService.triggerDownDevice(apply);

		assertFalse(result);
		assertEquals("下发异常时不应误推进 deviceStatus，应保持原值由对账任务重试",
				DeviceDownStatusEnum.WAIT.getCode(), apply.getDeviceStatus());
		// 异常路径不应触碰主表 CAS
		verify(applyMapper, never()).update(any(), any());
	}

	// ========== down() 明细级 CAS 抢占（SmtSecurityTaskDetailsServiceImpl）==========

	@Test
	public void down_claimFails_skipsPersonCardUpdate() throws Exception {
		SmtSecurityTaskDetailsMapper detailsMapper = mock(SmtSecurityTaskDetailsMapper.class);
		com.tce.smart.platform.service.SmtStaffService staffService = mock(com.tce.smart.platform.service.SmtStaffService.class);
		SmtSecurityTaskDetailsServiceImpl detailsService = new SmtSecurityTaskDetailsServiceImpl();
		setField(detailsService, "baseMapper", detailsMapper);
		setField(detailsService, "smtStaffService", staffService);
		// 明细级 CAS 抢占失败：并发方已处理该明细
		when(detailsMapper.update(any(), any())).thenReturn(0);

		SmtSecurityTaskDetails detail = SmtSecurityTaskDetails.builder()
				.id(4001L).staffId(9001L).authId(1).status(DeviceDownStatusEnum.WAIT.getCode()).build();

		Method down = SmtSecurityTaskDetailsServiceImpl.class.getDeclaredMethod("down", SmtSecurityTaskDetails.class, String.class);
		down.setAccessible(true);
		down.invoke(detailsService, detail, "badge-1");

		// 抢占失败应直接返回，既不查员工信息也不触发下发
		verify(staffService, never()).getById(anyLong());
		verify(staffService, never()).updatePersonCard(any(), any(), any(), any(), any(), any());
	}
}
