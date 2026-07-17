package com.tce.smart.platform.service.securityzone;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.AbstractWrapper;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.tce.smart.platform.core.entity.SmtIscDeviceTask;
import com.tce.smart.platform.core.entity.SmtStaff;
import com.tce.smart.platform.core.entity.SmtStaffDeviceAuth;
import com.tce.smart.platform.core.entity.securityzone.SmtSecurityAuthApply;
import com.tce.smart.platform.core.entity.securityzone.SmtSecurityTaskDetails;
import com.tce.smart.platform.core.mapper.SmtSecurityAuthApplyMapper;
import com.tce.smart.platform.core.mapper.SmtSecurityTaskDetailsMapper;
import com.tce.smart.platform.core.service.SmtIscDeviceTaskService;
import com.tce.smart.platform.core.service.SmtImageService;
import com.tce.smart.platform.service.SmtStaffDeviceAuthService;
import com.tce.smart.platform.service.SmtStaffService;
import com.tce.smart.platform.service.securityzone.impl.SmtSecurityTaskDetailsServiceImpl;
import com.tce.smart.tool.enums.DeviceDownStatusEnum;
import com.tce.smart.tool.enums.DeviceTaskStatusEnum;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collections;
import java.util.Locale;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** 当前批次必须只由真实 ISC 任务聚合，旧批次迟到结果不得污染。 */
@SuppressWarnings({"rawtypes", "unchecked"})
public class SecurityAuthIscAggregationTest {

	@BeforeClass
	public static void initMybatisPlusLambdaCache() {
		TableInfoHelper.initTableInfo(new MapperBuilderAssistant(new MybatisConfiguration(), ""), SmtSecurityTaskDetails.class);
		TableInfoHelper.initTableInfo(new MapperBuilderAssistant(new MybatisConfiguration(), ""), SmtSecurityAuthApply.class);
		TableInfoHelper.initTableInfo(new MapperBuilderAssistant(new MybatisConfiguration(), ""), SmtIscDeviceTask.class);
	}

	@Test
	public void syncTaskStatus_requiresAllCurrentBatchIscTasksToSucceed() throws Exception {
		Fixture fixture = fixture();
		SmtSecurityTaskDetails detail = detail();
		doReturn(Collections.singletonList(detail)).when(fixture.service).list(any());
		when(fixture.iscTaskService.list(any())).thenReturn(Arrays.asList(
				iscTask(DeviceTaskStatusEnum.SUCCESS.getCode(), null),
				iscTask(DeviceTaskStatusEnum.DOING.getCode(), null)));

		fixture.service.syncTaskStatus(1001L);

		assertEquals("仍有设备处理中时人员不能提前成功", DeviceDownStatusEnum.IN_WORK.getCode(), detail.getStatus());
		ArgumentCaptor<Wrapper<SmtIscDeviceTask>> query = ArgumentCaptor.forClass(Wrapper.class);
		verify(fixture.iscTaskService).list(query.capture());
		String sql = query.getValue().getSqlSegment().toLowerCase(Locale.ROOT);
		assertTrue("ISC 聚合必须限定业务来源", sql.contains("source_type"));
		assertTrue("ISC 聚合必须限定当前批次", sql.contains("batch_id"));
		assertTrue("ISC 聚合必须按人员明细关联", sql.contains("source_detail_id"));
	}

	@Test
	public void syncTaskStatus_recordsCurrentBatchDeviceFailureReason() throws Exception {
		Fixture fixture = fixture();
		SmtSecurityTaskDetails detail = detail();
		doReturn(Collections.singletonList(detail)).when(fixture.service).list(any());
		when(fixture.iscTaskService.list(any())).thenReturn(Arrays.asList(
				iscTask(DeviceTaskStatusEnum.SUCCESS.getCode(), null),
				iscTask(DeviceTaskStatusEnum.FAIL.getCode(), "设备拒绝权限")));

		fixture.service.syncTaskStatus(1001L);

		assertEquals(DeviceDownStatusEnum.FAIL.getCode(), detail.getStatus());
		assertEquals("设备拒绝权限", detail.getRemark());
	}

	@Test
	public void syncTaskStatus_treatsCanceledIscTaskAsDetailAndApplyFailure() throws Exception {
		Fixture fixture = fixture();
		SmtSecurityTaskDetails detail = detail();
		doReturn(Collections.singletonList(detail)).when(fixture.service).list(any());
		when(fixture.iscTaskService.list(any())).thenReturn(
				Collections.singletonList(iscTask(DeviceTaskStatusEnum.CANCEL.getCode(), "任务已被接管取消")));

		fixture.service.syncTaskStatus(1001L);

		assertEquals("取消任务必须使人员明细失败", DeviceDownStatusEnum.FAIL.getCode(), detail.getStatus());
		ArgumentCaptor<Wrapper<SmtSecurityAuthApply>> applyUpdate = ArgumentCaptor.forClass(Wrapper.class);
		verify(fixture.applyMapper).update(any(), applyUpdate.capture());
		java.util.Collection<Object> params = ((AbstractWrapper) applyUpdate.getValue())
				.getParamNameValuePairs().values();
		assertTrue("取消人员必须使当前申请单最终失败", params.contains(DeviceDownStatusEnum.FAIL.getCode()));
	}

	@Test
	public void syncTaskStatus_rereadsDetailsAndCasPreventsTerminalStateRegression() throws Exception {
		Fixture fixture = fixture();
		SmtSecurityTaskDetails staleInWork = detail();
		SmtSecurityTaskDetails concurrentlyCompleted = detail();
		concurrentlyCompleted.setStatus(DeviceDownStatusEnum.SUCCESS.getCode());
		doReturn(Collections.singletonList(staleInWork), Collections.singletonList(concurrentlyCompleted))
				.when(fixture.service).list(any());
		when(fixture.iscTaskService.list(any()))
				.thenReturn(Collections.singletonList(iscTask(DeviceTaskStatusEnum.DOING.getCode(), null)));
		when(fixture.detailsMapper.update(any(), any())).thenReturn(0);

		fixture.service.syncTaskStatus(1001L);

		ArgumentCaptor<Wrapper<SmtSecurityTaskDetails>> detailUpdate = ArgumentCaptor.forClass(Wrapper.class);
		verify(fixture.detailsMapper).update(any(), detailUpdate.capture());
		String detailWhere = detailUpdate.getValue().getSqlSegment().toLowerCase(Locale.ROOT);
		assertTrue("明细聚合必须以 IN_WORK 作为 CAS 条件，终态不能被旧回调覆盖", detailWhere.contains("status"));

		ArgumentCaptor<Wrapper<SmtSecurityAuthApply>> applyUpdate = ArgumentCaptor.forClass(Wrapper.class);
		verify(fixture.applyMapper).update(any(), applyUpdate.capture());
		String applyWhere = applyUpdate.getValue().getSqlSegment().toLowerCase(Locale.ROOT);
		assertTrue("申请单聚合必须只允许 WAIT/IN_WORK 推进", applyWhere.contains("device_status in"));
		java.util.Collection<Object> applyParams = ((AbstractWrapper) applyUpdate.getValue())
				.getParamNameValuePairs().values();
		assertTrue(applyParams.contains(DeviceDownStatusEnum.WAIT.getCode()));
		assertTrue(applyParams.contains(DeviceDownStatusEnum.IN_WORK.getCode()));
		assertTrue("聚合主单前必须重读明细，采用并发方已落库的 SUCCESS",
				applyParams.contains(DeviceDownStatusEnum.SUCCESS.getCode()));
	}

	@Test
	public void dispatchCurrentBatchDetails_claimsThenCreatesSecurityIscTasksWithContext() throws Exception {
		Fixture fixture = fixture();
		SmtStaffService staffService = mock(SmtStaffService.class);
		SmtImageService imageService = mock(SmtImageService.class);
		SmtStaffDeviceAuthService staffAuthService = mock(SmtStaffDeviceAuthService.class);
		SmtSecurityTaskDetails detail = detail();
		detail.setStaffId(501L);
		detail.setAuthId(77);
		SmtStaff staff = new SmtStaff();
		staff.setId(501L);
		staff.setFacePicId("face-1");
		doReturn(Collections.singletonList(detail)).when(fixture.service).list(any());
		when(fixture.detailsMapper.update(any(), any())).thenReturn(1);
		when(staffService.getById(501L)).thenReturn(staff);
		when(staffAuthService.list(any())).thenReturn(Collections.emptyList());
		when(imageService.getImageBase64ByCode("face-1")).thenReturn("AQID");
		setField(fixture.service, "smtStaffService", staffService);
		setField(fixture.service, "smtImageService", imageService);
		setField(fixture.service, "smtStaffDeviceAuthService", staffAuthService);

		int processed = fixture.service.dispatchCurrentBatchDetails(1001L, 9002L, "APPLY",
				Collections.singletonList(501L));

		assertEquals(1, processed);
		ArgumentCaptor<com.tce.smart.platform.core.dto.SecurityAuthDispatchContext> context =
				ArgumentCaptor.forClass(com.tce.smart.platform.core.dto.SecurityAuthDispatchContext.class);
		verify(staffService).updatePersonCardForSecurityDispatch(any(), any(), any(), any(), any(), any(),
				context.capture());
		assertEquals(Long.valueOf(1001L), context.getValue().getSourceId());
		assertEquals(Long.valueOf(101L), context.getValue().getSourceDetailId());
		assertEquals(Long.valueOf(9002L), context.getValue().getBatchId());
	}

	private Fixture fixture() throws Exception {
		Fixture fixture = new Fixture();
		fixture.service = spy(new SmtSecurityTaskDetailsServiceImpl());
		fixture.detailsMapper = mock(SmtSecurityTaskDetailsMapper.class);
		fixture.applyMapper = mock(SmtSecurityAuthApplyMapper.class);
		fixture.iscTaskService = mock(SmtIscDeviceTaskService.class);
		SmtSecurityAuthApply apply = new SmtSecurityAuthApply();
		apply.setId(1001L);
		apply.setCurrentDispatchBatchId(9002L);
		apply.setDeviceStatus(DeviceDownStatusEnum.IN_WORK.getCode());
		when(fixture.applyMapper.selectById(1001L)).thenReturn(apply);
		when(fixture.detailsMapper.update(any(), any())).thenReturn(1);
		when(fixture.applyMapper.update(any(), any())).thenReturn(1);
		setField(fixture.service, "baseMapper", fixture.detailsMapper);
		setField(fixture.service, "smtSecurityAuthApplyMapper", fixture.applyMapper);
		setField(fixture.service, "smtIscDeviceTaskService", fixture.iscTaskService);
		return fixture;
	}

	private SmtSecurityTaskDetails detail() {
		return SmtSecurityTaskDetails.builder().id(101L).applyId(1001L).dispatchBatchId(9002L)
				.status(DeviceDownStatusEnum.IN_WORK.getCode()).build();
	}

	private SmtIscDeviceTask iscTask(Integer status, String remark) {
		SmtIscDeviceTask task = new SmtIscDeviceTask();
		task.setStatus(status);
		task.setRemark(remark);
		return task;
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

	private static final class Fixture {
		private SmtSecurityTaskDetailsServiceImpl service;
		private SmtSecurityTaskDetailsMapper detailsMapper;
		private SmtSecurityAuthApplyMapper applyMapper;
		private SmtIscDeviceTaskService iscTaskService;
	}
}
