package com.tce.smart.platform.service.securityzone;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.tce.smart.platform.core.entity.securityzone.SmtSecurityAuthApply;
import com.tce.smart.platform.core.entity.securityzone.SmtSecurityTaskDetails;
import com.tce.smart.platform.core.mapper.SmtSecurityAuthApplyMapper;
import com.tce.smart.platform.core.mapper.SmtSecurityTaskDetailsMapper;
import com.tce.smart.platform.core.vo.SecurityDispatchAcceptedVO;
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
import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 202 下发命令受理的领域测试：只持久化最新批次，绝不在请求线程调用 ISC。
 */
@SuppressWarnings("unchecked")
public class SecurityAuthDispatchAcceptanceTest {

	@BeforeClass
	public static void initMybatisPlusLambdaCache() {
		TableInfoHelper.initTableInfo(new MapperBuilderAssistant(new MybatisConfiguration(), ""), SmtSecurityAuthApply.class);
		TableInfoHelper.initTableInfo(new MapperBuilderAssistant(new MybatisConfiguration(), ""), SmtSecurityTaskDetails.class);
	}

	private SmtSecurityAuthApplyMapper applyMapper;
	private SmtSecurityTaskDetailsService taskDetailsService;
	private SmtSecurityAuthApplyServiceImpl applyService;

	@Before
	public void setUp() throws Exception {
		applyMapper = mock(SmtSecurityAuthApplyMapper.class);
		taskDetailsService = mock(SmtSecurityTaskDetailsService.class);
		applyService = spy(new SmtSecurityAuthApplyServiceImpl());
		setField(applyService, "baseMapper", applyMapper);
		setField(applyService, "smtSecurityTaskDetailsService", taskDetailsService);
	}

	@Test
	public void acceptDispatch_rebindsOnlyLatestBatchAndStartsInWork() {
		SmtSecurityAuthApply apply = approvedApply(1001L);
		doReturn(apply).when(applyService).getOne(any());
		when(taskDetailsService.rebindDispatchBatch(anyLong(), anyLong())).thenReturn(1501);
		when(taskDetailsService.countDispatchPeople(anyLong(), anyLong())).thenReturn(1000);
		when(applyMapper.update(any(), any())).thenReturn(1);

		SecurityDispatchAcceptedVO first = applyService.acceptDispatch(1001L);
		SecurityDispatchAcceptedVO second = applyService.acceptDispatch(1001L);

		assertEquals("一个人拥有多个权限明细时，受理人数必须去重", Integer.valueOf(1000), first.getAcceptedCount());
		assertEquals(Integer.valueOf(0), first.getTakeoverCount());
		assertNotNull(first.getBatchId());
		assertNotEquals("连续受理必须换成新批次", first.getBatchId(), second.getBatchId());
		assertEquals("申请单只能保留最新批次", second.getBatchId(), apply.getCurrentDispatchBatchId());
		assertEquals(DeviceDownStatusEnum.IN_WORK.getCode(), apply.getDeviceStatus());
		verify(taskDetailsService).rebindDispatchBatch(1001L, first.getBatchId());
		verify(taskDetailsService).rebindDispatchBatch(1001L, second.getBatchId());
	}

	@Test
	public void acceptDispatch_withoutAnyPendingDetails_doesNotMarkApplySuccessful() {
		SmtSecurityAuthApply apply = approvedApply(1002L);
		doReturn(apply).when(applyService).getOne(any());
		when(taskDetailsService.rebindDispatchBatch(anyLong(), anyLong())).thenReturn(0);

		SecurityDispatchAcceptedVO accepted = applyService.acceptDispatch(1002L);

		assertEquals(Integer.valueOf(0), accepted.getAcceptedCount());
		assertNotNull(accepted.getBatchId());
		assertEquals("空明细不能被误判为全部成功", DeviceDownStatusEnum.WAIT.getCode(), apply.getDeviceStatus());
	}

	@Test
	public void getDispatchProgress_updatesStatusWithCurrentBatchCas() {
		SmtSecurityAuthApply apply = approvedApply(1003L);
		apply.setCurrentDispatchBatchId(9003L);
		apply.setDeviceStatus(DeviceDownStatusEnum.SUCCESS.getCode());
		doReturn(apply).when(applyService).getById(1003L);
		when(taskDetailsService.list(any())).thenReturn(Collections.singletonList(SmtSecurityTaskDetails.builder()
				.status(DeviceDownStatusEnum.WAIT.getCode()).build()));
		when(applyMapper.update(any(), any())).thenReturn(0);

		applyService.getDispatchProgress(1003L, 9003L);

		ArgumentCaptor<LambdaUpdateWrapper<SmtSecurityAuthApply>> captor = ArgumentCaptor.forClass(LambdaUpdateWrapper.class);
		verify(applyMapper).update(any(), captor.capture());
		assertTrue("状态聚合必须以当前批次作 CAS 围栏，不能回写旧实体", captor.getValue().getSqlSegment()
				.toLowerCase(java.util.Locale.ROOT).contains("current_dispatch_batch_id"));
	}

	@Test
	public void rebindDispatchBatch_excludesSuccessfulDetailsAndResetsOtherStatusesToWait() throws Exception {
		SmtSecurityTaskDetailsMapper detailsMapper = mock(SmtSecurityTaskDetailsMapper.class);
		SmtSecurityTaskDetailsServiceImpl detailsService = new SmtSecurityTaskDetailsServiceImpl();
		setField(detailsService, "baseMapper", detailsMapper);
		when(detailsMapper.update(any(), any())).thenReturn(2);

		int acceptedCount = detailsService.rebindDispatchBatch(1001L, 9001L);

		assertEquals(2, acceptedCount);
		ArgumentCaptor<LambdaUpdateWrapper<SmtSecurityTaskDetails>> captor = ArgumentCaptor.forClass(LambdaUpdateWrapper.class);
		verify(detailsMapper).update(any(), captor.capture());
		String sql = captor.getValue().getSqlSegment().toLowerCase(java.util.Locale.ROOT);
		assertTrue("重绑必须按申请单限制", sql.contains("apply_id"));
		assertTrue("已成功人员不能进入新批次", sql.contains("status"));
		assertEquals(DeviceDownStatusEnum.WAIT.getCode(), captor.getValue().getParamNameValuePairs().values().stream()
				.filter(DeviceDownStatusEnum.WAIT.getCode()::equals).findFirst().orElse(null));
	}

	@Test
	public void claimDispatchDetail_rejectsOldBatch() throws Exception {
		SmtSecurityTaskDetailsMapper detailsMapper = mock(SmtSecurityTaskDetailsMapper.class);
		SmtSecurityTaskDetailsServiceImpl detailsService = new SmtSecurityTaskDetailsServiceImpl();
		setField(detailsService, "baseMapper", detailsMapper);
		when(detailsMapper.update(any(), any())).thenReturn(0);

		boolean claimed = detailsService.claimDispatchDetail(101L, 9000L);

		assertFalse("旧批次不得领取明细", claimed);
		ArgumentCaptor<LambdaUpdateWrapper<SmtSecurityTaskDetails>> captor = ArgumentCaptor.forClass(LambdaUpdateWrapper.class);
		verify(detailsMapper).update(any(), captor.capture());
		String sql = captor.getValue().getSqlSegment().toLowerCase(java.util.Locale.ROOT);
		assertTrue("领取条件必须包含批次围栏", sql.contains("dispatch_batch_id"));
		assertTrue("领取条件必须限定待领取状态", sql.contains("status"));
	}

	private SmtSecurityAuthApply approvedApply(Long id) {
		SmtSecurityAuthApply apply = new SmtSecurityAuthApply();
		apply.setId(id);
		apply.setOaStatus(ApproveListStateEnum.AGREE.getCode());
		apply.setDeviceStatus(DeviceDownStatusEnum.WAIT.getCode());
		return apply;
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
		throw new NoSuchFieldException(name);
	}
}
