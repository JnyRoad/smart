package com.tce.smart.platform.service.securityzone;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
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
