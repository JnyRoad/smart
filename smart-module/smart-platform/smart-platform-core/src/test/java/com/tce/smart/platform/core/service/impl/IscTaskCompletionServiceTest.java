package com.tce.smart.platform.core.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.tce.smart.platform.core.entity.SmtIscDeviceTask;
import com.tce.smart.platform.core.enums.ISCDeviceTaskEnum;
import com.tce.smart.platform.core.service.SmtIscDeviceTaskService;
import com.tce.smart.platform.core.service.SmtIscDownRecordService;
import com.tce.smart.tool.constant.DeviceTaskConstants;
import com.tce.smart.tool.enums.DeviceTaskActionEnum;
import com.tce.smart.tool.enums.DeviceTaskStatusEnum;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.apache.ibatis.builder.MapperBuilderAssistant;

/**
 * ISC设备成功回执本地收敛服务测试。
 */
public class IscTaskCompletionServiceTest {

	@BeforeClass
	public static void initMybatisPlusLambdaCache() {
		TableInfoHelper.initTableInfo(new MapperBuilderAssistant(new MybatisConfiguration(), ""), SmtIscDeviceTask.class);
	}

	/**
	 * 验证条件更新成功后才维护下发记录，并把原状态和原ISC批次纳入更新条件。
	 */
	@Test
	public void completeSuccessMaintainsRecordAfterMatchingTaskUpdate() {
		SmtIscDeviceTaskService taskService = Mockito.mock(SmtIscDeviceTaskService.class);
		SmtIscDownRecordService downRecordService = Mockito.mock(SmtIscDownRecordService.class);
		IscTaskCompletionService service = new IscTaskCompletionService(taskService, downRecordService);
		SmtIscDeviceTask task = task(DeviceTaskStatusEnum.DOING.getCode(), "isc-task-1");
		Mockito.when(taskService.update(Mockito.any(LambdaUpdateWrapper.class))).thenReturn(true);

		Assert.assertTrue(service.completeSuccess(task, "设备确认成功"));

		ArgumentCaptor<LambdaUpdateWrapper> wrapperCaptor = ArgumentCaptor.forClass(LambdaUpdateWrapper.class);
		Mockito.verify(taskService).update(wrapperCaptor.capture());
		LambdaUpdateWrapper wrapper = wrapperCaptor.getValue();
		String sqlSegment = wrapper.getSqlSegment().toUpperCase();
		Assert.assertTrue(sqlSegment.contains("STATUS"));
		Assert.assertTrue(sqlSegment.contains("ISC_TASK_ID"));
		Assert.assertTrue(wrapper.getParamNameValuePairs().values().contains(DeviceTaskStatusEnum.DOING.getCode()));
		Assert.assertTrue(wrapper.getParamNameValuePairs().values().contains("isc-task-1"));
		Assert.assertEquals(DeviceTaskStatusEnum.SUCCESS.getCode(), task.getStatus());
		Assert.assertEquals(ISCDeviceTaskEnum.DEVICE_OK.getCode(), task.getCode());
		Assert.assertEquals("设备确认成功", task.getRemark());
		Mockito.verify(downRecordService).handleTaskDownRecord(task);
	}

	/**
	 * 验证原任务条件未命中时只报告未收敛，不重复维护下发记录。
	 */
	@Test
	public void completeSuccessSkipsRecordWhenConditionalTaskUpdateMisses() {
		SmtIscDeviceTaskService taskService = Mockito.mock(SmtIscDeviceTaskService.class);
		SmtIscDownRecordService downRecordService = Mockito.mock(SmtIscDownRecordService.class);
		IscTaskCompletionService service = new IscTaskCompletionService(taskService, downRecordService);
		SmtIscDeviceTask task = task(DeviceTaskStatusEnum.DOING.getCode(), "isc-task-1");
		Mockito.when(taskService.update(Mockito.any(LambdaUpdateWrapper.class))).thenReturn(false);

		Assert.assertFalse(service.completeSuccess(task, "设备确认成功"));

		Assert.assertEquals(DeviceTaskStatusEnum.DOING.getCode(), task.getStatus());
		Mockito.verify(downRecordService, Mockito.never()).handleTaskDownRecord(Mockito.any(SmtIscDeviceTask.class));
	}

	/**
	 * 验证下发记录维护异常向调用方暴露，避免成功任务被静默吞掉。
	 */
	@Test(expected = IllegalStateException.class)
	public void completeSuccessPropagatesDownRecordFailure() {
		SmtIscDeviceTaskService taskService = Mockito.mock(SmtIscDeviceTaskService.class);
		SmtIscDownRecordService downRecordService = Mockito.mock(SmtIscDownRecordService.class);
		IscTaskCompletionService service = new IscTaskCompletionService(taskService, downRecordService);
		SmtIscDeviceTask task = task(DeviceTaskStatusEnum.DOING.getCode(), "isc-task-1");
		Mockito.when(taskService.update(Mockito.any(LambdaUpdateWrapper.class))).thenReturn(true);
		Mockito.doThrow(new IllegalStateException("下发记录维护失败"))
				.when(downRecordService).handleTaskDownRecord(task);

		service.completeSuccess(task, "设备确认成功");
	}

	/**
	 * 验证已经成功的终态任务不会被同一回执再次更新或重复维护下发记录。
	 */
	@Test
	public void completeSuccessRejectsAlreadySuccessfulTask() {
		SmtIscDeviceTaskService taskService = Mockito.mock(SmtIscDeviceTaskService.class);
		SmtIscDownRecordService downRecordService = Mockito.mock(SmtIscDownRecordService.class);
		IscTaskCompletionService service = new IscTaskCompletionService(taskService, downRecordService);
		SmtIscDeviceTask task = task(DeviceTaskStatusEnum.SUCCESS.getCode(), "isc-task-1");

		Assert.assertFalse(service.completeSuccess(task, "重复设备确认成功"));

		Mockito.verify(taskService, Mockito.never()).update(Mockito.any(LambdaUpdateWrapper.class));
		Mockito.verify(downRecordService, Mockito.never()).handleTaskDownRecord(Mockito.any(SmtIscDeviceTask.class));
	}

	/**
	 * 构造包含持久任务原状态和ISC外部批次的可信回执测试数据。
	 *
	 * @param status 任务持久状态
	 * @param iscTaskId ISC外部任务批次号
	 * @return 可用于完成器测试的任务
	 */
	private SmtIscDeviceTask task(Integer status, String iscTaskId) {
		SmtIscDeviceTask task = new SmtIscDeviceTask();
		task.setId(1L);
		task.setStatus(status);
		task.setIscTaskId(iscTaskId);
		task.setAction(DeviceTaskActionEnum.DOWN.getCode());
		task.setDeviceType(DeviceTaskConstants.CARD);
		task.setDeviceCode("device-1");
		task.setCardNo("staff-1");
		task.setPersonId("isc-person-1");
		return task;
	}
}
