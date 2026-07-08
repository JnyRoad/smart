package com.tce.smart.platform.service.impl;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.tce.smart.platform.core.entity.SmtVisitor;
import com.tce.smart.platform.core.mapper.SmtVisitorMapper;
import com.tce.smart.platform.service.ApproveListService;
import com.tce.smart.platform.service.SmtVisitorProcessRecordService;
import com.tce.smart.platform.service.SmtVisitorService;
import com.tce.smart.tool.enums.VisitorStatusEnum;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Locale;

/**
 * 访客（smt_visitor）超时清理：真过期才清理 + CAS 抢占。
 * 与入厂申请侧 SmtAdmittanceApplyServiceImplTest 的 visitorOverTime 测试同构：
 * 历史上查询叠加 overtime-offset-hour（生产配 24 小时）提前量，会把仍在 OA 审批中的
 * 短期访客单提前一天终态化，之后回调与拉取对账（都只认待审核状态）永远无法落审批结果。
 */
@SuppressWarnings({"rawtypes", "unchecked"})
public class VisitorTaskServiceImplTest {

	@BeforeClass
	public static void initMybatisPlusLambdaCache() {
		TableInfoHelper.initTableInfo(new MapperBuilderAssistant(new MybatisConfiguration(), ""), SmtVisitor.class);
	}

	/**
	 * 搭建 visitorOverTime 的最小依赖：查询走 visitorService，写库走 baseMapper，
	 * 副作用走审批待办与流程记录两个服务。
	 */
	private VisitorTaskHarness setUpHarness() throws Exception {
		VisitorTaskHarness harness = new VisitorTaskHarness();
		harness.mapper = Mockito.mock(SmtVisitorMapper.class);
		harness.visitorService = Mockito.mock(SmtVisitorService.class);
		harness.approveListService = Mockito.mock(ApproveListService.class);
		harness.processRecordService = Mockito.mock(SmtVisitorProcessRecordService.class);
		harness.service = new VisitorTaskServiceImpl();
		setField(harness.service, "baseMapper", harness.mapper);
		setField(harness.service, "visitorService", harness.visitorService);
		setField(harness.service, "approveListService", harness.approveListService);
		setField(harness.service, "smtVisitorProcessRecordService", harness.processRecordService);
		return harness;
	}

	private static final class VisitorTaskHarness {
		VisitorTaskServiceImpl service;
		SmtVisitorMapper mapper;
		SmtVisitorService visitorService;
		ApproveListService approveListService;
		SmtVisitorProcessRecordService processRecordService;
	}

	private static final Integer PARK_ID = 1001;

	private SmtVisitor expiredVisitor(Long id, Integer status) {
		SmtVisitor visitor = new SmtVisitor();
		visitor.setId(id);
		visitor.setStatus(status);
		visitor.setParkId(PARK_ID);
		//预约结束时间一小时前：真过期
		visitor.setEndTime(new Date(System.currentTimeMillis() - 3600_000L));
		return visitor;
	}

	/**
	 * 两条查询的 end_time 上限都必须是当前时刻，不允许叠加提前量误杀在审单。
	 */
	@Test
	public void visitorOverTimeOnlyTargetsRowsAlreadyPastEndTime() throws Exception {
		VisitorTaskHarness harness = setUpHarness();
		Mockito.when(harness.visitorService.list(Mockito.any())).thenReturn(Collections.emptyList());

		harness.service.visitorOverTime(PARK_ID);

		ArgumentCaptor<LambdaQueryWrapper> queryCaptor = ArgumentCaptor.forClass(LambdaQueryWrapper.class);
		Mockito.verify(harness.visitorService, Mockito.times(2)).list(queryCaptor.capture());
		for (Object captured : queryCaptor.getAllValues()) {
			LambdaQueryWrapper wrapper = (LambdaQueryWrapper) captured;
			wrapper.getSqlSegment();
			Collection<?> queryParams = wrapper.getParamNameValuePairs().values();
			Date endTimeUpperBound = queryParams.stream()
					.filter(Date.class::isInstance)
					.map(Date.class::cast)
					.findFirst()
					.orElseThrow(() -> new AssertionError("查询应包含 end_time 时间上限参数"));
			Assert.assertFalse("end_time 上限不得晚于当前时刻（禁止提前量误杀在审单），实际：" + endTimeUpperBound,
					endTimeUpperBound.after(new Date(System.currentTimeMillis() + 1000L)));
		}
	}

	/**
	 * 待审核行的超时终态化必须走 CAS（仅当行仍为待审核(2)时置预约超时(6)），
	 * 不允许无条件 updateById——与回调/对账 claim 并发时会把已落的审批结果改回超时。
	 */
	@Test
	public void updateNoPassClaimsPendingRowWithCasInsteadOfUnconditionalUpdate() throws Exception {
		VisitorTaskHarness harness = setUpHarness();
		//第一次查询（已通过 status=0）返回空，第二次查询（待审核 status=2）返回过期行
		Mockito.when(harness.visitorService.list(Mockito.any()))
				.thenReturn(Collections.emptyList(),
						Collections.singletonList(expiredVisitor(4101L, VisitorStatusEnum.Status_2.getCode())));
		Mockito.when(harness.mapper.update(Mockito.isNull(), Mockito.any())).thenReturn(1);
		Mockito.when(harness.approveListService.list(Mockito.any())).thenReturn(Collections.emptyList());
		Mockito.when(harness.processRecordService.list(Mockito.any())).thenReturn(Collections.emptyList());

		harness.service.visitorOverTime(PARK_ID);

		ArgumentCaptor<LambdaUpdateWrapper> updateCaptor = ArgumentCaptor.forClass(LambdaUpdateWrapper.class);
		Mockito.verify(harness.mapper).update(Mockito.isNull(), updateCaptor.capture());
		LambdaUpdateWrapper casWrapper = updateCaptor.getValue();
		Assert.assertTrue("CAS 条件必须限定行仍为待审核",
				casWrapper.getSqlSegment().toLowerCase(Locale.ROOT).contains("status"));
		Assert.assertTrue(updateWrapperHasParam(casWrapper, VisitorStatusEnum.Status_2.getCode()));
		Assert.assertTrue(updateWrapperHasParam(casWrapper, VisitorStatusEnum.CAUSE_6.getCode()));
		Mockito.verify(harness.mapper, Mockito.never()).updateById(Mockito.any());
	}

	/**
	 * CAS 未命中（行已被回调/对账先行落终态）时整单跳过，不再更新审批待办与流程记录。
	 */
	@Test
	public void updateNoPassSkipsSideEffectsWhenPendingRowAlreadyClaimed() throws Exception {
		VisitorTaskHarness harness = setUpHarness();
		Mockito.when(harness.visitorService.list(Mockito.any()))
				.thenReturn(Collections.emptyList(),
						Collections.singletonList(expiredVisitor(4102L, VisitorStatusEnum.Status_2.getCode())));
		Mockito.when(harness.mapper.update(Mockito.isNull(), Mockito.any())).thenReturn(0);

		harness.service.visitorOverTime(PARK_ID);

		Mockito.verify(harness.approveListService, Mockito.never()).list(Mockito.any());
		Mockito.verify(harness.processRecordService, Mockito.never()).list(Mockito.any());
		Mockito.verify(harness.mapper, Mockito.never()).updateById(Mockito.any());
	}

	/**
	 * 已通过行的"超时未到"同样必须走 CAS（仅当行仍为已通过(0)时置超时未到(4)）。
	 */
	@Test
	public void removeVisitorClaimsApprovedRowWithCasInsteadOfUnconditionalUpdate() throws Exception {
		VisitorTaskHarness harness = setUpHarness();
		//第一次查询（已通过 status=0）返回过期行，第二次查询（待审核 status=2）返回空
		Mockito.when(harness.visitorService.list(Mockito.any()))
				.thenReturn(Collections.singletonList(expiredVisitor(4103L, VisitorStatusEnum.Status_0.getCode())),
						Collections.emptyList());
		Mockito.when(harness.mapper.update(Mockito.isNull(), Mockito.any())).thenReturn(1);
		Mockito.when(harness.approveListService.list(Mockito.any())).thenReturn(Collections.emptyList());

		harness.service.visitorOverTime(PARK_ID);

		ArgumentCaptor<LambdaUpdateWrapper> updateCaptor = ArgumentCaptor.forClass(LambdaUpdateWrapper.class);
		Mockito.verify(harness.mapper).update(Mockito.isNull(), updateCaptor.capture());
		LambdaUpdateWrapper casWrapper = updateCaptor.getValue();
		Assert.assertTrue(updateWrapperHasParam(casWrapper, VisitorStatusEnum.Status_0.getCode()));
		Assert.assertTrue(updateWrapperHasParam(casWrapper, VisitorStatusEnum.CAUSE_4.getCode()));
		Mockito.verify(harness.mapper, Mockito.never()).updateById(Mockito.any());
	}

	/**
	 * 已通过行 CAS 未命中（如访客已登记到场）时整单跳过，不再更新审批待办。
	 */
	@Test
	public void removeVisitorSkipsSideEffectsWhenApprovedRowAlreadyClaimed() throws Exception {
		VisitorTaskHarness harness = setUpHarness();
		Mockito.when(harness.visitorService.list(Mockito.any()))
				.thenReturn(Collections.singletonList(expiredVisitor(4104L, VisitorStatusEnum.Status_0.getCode())),
						Collections.emptyList());
		Mockito.when(harness.mapper.update(Mockito.isNull(), Mockito.any())).thenReturn(0);

		harness.service.visitorOverTime(PARK_ID);

		Mockito.verify(harness.approveListService, Mockito.never()).list(Mockito.any());
		Mockito.verify(harness.mapper, Mockito.never()).updateById(Mockito.any());
	}

	private boolean updateWrapperHasParam(LambdaUpdateWrapper updateWrapper, Object expected) {
		//先触发 SQL 段生成，确保条件与 set 的参数都进入 paramNameValuePairs
		updateWrapper.getSqlSegment();
		updateWrapper.getSqlSet();
		Collection<?> params = updateWrapper.getParamNameValuePairs().values();
		return params.stream().anyMatch(value -> String.valueOf(expected).equals(String.valueOf(value)));
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
}
