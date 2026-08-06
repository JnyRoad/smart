package com.tce.smart.schedule.service.platform.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tce.smart.common.core.constant.SecurityConstants;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.dispatcher.api.dto.req.DispatcherDTO;
import com.tce.smart.dispatcher.api.enums.EventEnum;
import com.tce.smart.dispatcher.api.feign.RemoteDispatcherService;
import com.tce.smart.platform.core.entity.SmtIscCardTask;
import com.tce.smart.platform.core.enums.ISCDeviceTaskEnum;
import com.tce.smart.platform.core.service.SmtIscCardTaskService;
import com.tce.smart.platform.core.service.SmtIscStaffCardService;
import com.tce.smart.tool.enums.DeviceTaskActionEnum;
import com.tce.smart.tool.enums.DeviceTaskStatusEnum;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@SuppressWarnings({"rawtypes", "unchecked"})
public class ISCCardTaskServiceImplTest {

	@Test
	public void syncCardTasksAddsCardByParkWithoutDeviceJoin() {
		RemoteDispatcherService dispatcherService = Mockito.mock(RemoteDispatcherService.class);
		SmtIscCardTaskService taskService = Mockito.mock(SmtIscCardTaskService.class);
		SmtIscStaffCardService staffCardService = Mockito.mock(SmtIscStaffCardService.class);
		ISCCardTaskServiceImpl service = new ISCCardTaskServiceImpl(dispatcherService, taskService, staffCardService);
		SmtIscCardTask task = cardTask(DeviceTaskActionEnum.DOWN.getCode(), null);
		Page<SmtIscCardTask> page = new Page<>(1, 500);
		page.setRecords(java.util.Collections.singletonList(task));
		Mockito.when(taskService.getPendingTasks(Mockito.any(), Mockito.anyLong(), Mockito.anyInt())).thenReturn(page);
		Mockito.when(taskService.markDoing(Mockito.any(SmtIscCardTask.class), Mockito.anyLong(), Mockito.anyLong(), Mockito.anyInt())).thenReturn(true);
		Mockito.when(taskService.isCurrentStaffCardAddTask(Mockito.any(SmtIscCardTask.class))).thenReturn(true);
		Mockito.when(taskService.updateDoingTask(Mockito.any(SmtIscCardTask.class), Mockito.anyString(), Mockito.anyString())).thenReturn(true);
		Mockito.when(dispatcherService.dispatch(Mockito.any(), Mockito.eq(SecurityConstants.FROM_IN), Mockito.eq(SecurityConstants.INTERNAL_SERVICE_AUTH_REQUIRED)))
				.thenReturn(Result.success("{\"list\":[{\"personId\":\"deleted-person\",\"status\":-1},{\"personId\":\"isc-person-1\",\"status\":1}]}"))
				.thenReturn(Result.success("{}"));

		service.syncCardTasks();

		ArgumentCaptor<DispatcherDTO> requestCaptor = ArgumentCaptor.forClass(DispatcherDTO.class);
		Mockito.verify(dispatcherService, Mockito.times(2)).dispatch(requestCaptor.capture(), Mockito.eq(SecurityConstants.FROM_IN), Mockito.eq(SecurityConstants.INTERNAL_SERVICE_AUTH_REQUIRED));
		Assert.assertEquals(EventEnum.ISC_PERSON_GET.getCode(), requestCaptor.getAllValues().get(0).getEventType());
		Assert.assertEquals(Integer.valueOf(5000021), requestCaptor.getAllValues().get(0).getParkId());
		Assert.assertEquals(EventEnum.ISC_CARD_ADD.getCode(), requestCaptor.getAllValues().get(1).getEventType());
		Assert.assertEquals(Integer.valueOf(5000021), requestCaptor.getAllValues().get(1).getParkId());
		Map cardRequest = (Map) requestCaptor.getAllValues().get(1).getData();
		List cardList = (List) cardRequest.get("cardList");
		Assert.assertEquals(1, cardList.size());
		Map cardItem = (Map) cardList.get(0);
		Assert.assertEquals("isc-person-1", cardItem.get("personId"));
		Assert.assertEquals("12345678", cardItem.get("cardNo"));
		Assert.assertEquals(1, cardItem.get("cardType"));

		ArgumentCaptor<SmtIscCardTask> taskCaptor = ArgumentCaptor.forClass(SmtIscCardTask.class);
		ArgumentCaptor<String> activeKeyCaptor = ArgumentCaptor.forClass(String.class);
		Mockito.verify(taskService, Mockito.atLeastOnce()).updateDoingTask(taskCaptor.capture(), activeKeyCaptor.capture(), Mockito.anyString());
		SmtIscCardTask lastUpdate = taskCaptor.getAllValues().get(taskCaptor.getAllValues().size() - 1);
		Assert.assertEquals(taskActiveKey(DeviceTaskActionEnum.DOWN.getCode()), activeKeyCaptor.getAllValues().get(activeKeyCaptor.getAllValues().size() - 1));
		Assert.assertEquals(DeviceTaskStatusEnum.SUCCESS.getCode(), lastUpdate.getStatus());
		Assert.assertEquals(ISCDeviceTaskEnum.DEVICE_OK.getCode(), lastUpdate.getCode());
		Assert.assertEquals("isc-person-1", lastUpdate.getPersonId());
		Assert.assertNull(lastUpdate.getActiveKey());
		Mockito.verify(staffCardService).markAddTaskSuccess(lastUpdate);
	}

	@Test
	public void syncCardTasksDeletesCardWithStoredPersonIdWhenPersonCannotBeQueried() {
		RemoteDispatcherService dispatcherService = Mockito.mock(RemoteDispatcherService.class);
		SmtIscCardTaskService taskService = Mockito.mock(SmtIscCardTaskService.class);
		SmtIscStaffCardService staffCardService = Mockito.mock(SmtIscStaffCardService.class);
		ISCCardTaskServiceImpl service = new ISCCardTaskServiceImpl(dispatcherService, taskService, staffCardService);
		SmtIscCardTask task = cardTask(DeviceTaskActionEnum.DEL.getCode(), "isc-person-1");
		Page<SmtIscCardTask> page = new Page<>(1, 500);
		page.setRecords(java.util.Collections.singletonList(task));
		Mockito.when(taskService.getPendingTasks(Mockito.any(), Mockito.anyLong(), Mockito.anyInt())).thenReturn(page);
		Mockito.when(taskService.markDoing(Mockito.any(SmtIscCardTask.class), Mockito.anyLong(), Mockito.anyLong(), Mockito.anyInt())).thenReturn(true);
		Mockito.when(taskService.isCurrentStaffCardAddTask(Mockito.any(SmtIscCardTask.class))).thenReturn(true);
		Mockito.when(taskService.updateDoingTask(Mockito.any(SmtIscCardTask.class), Mockito.anyString(), Mockito.anyString())).thenReturn(true);
		Mockito.when(dispatcherService.dispatch(Mockito.any(), Mockito.eq(SecurityConstants.FROM_IN), Mockito.eq(SecurityConstants.INTERNAL_SERVICE_AUTH_REQUIRED)))
				.thenReturn(Result.success("{}"));

		service.syncCardTasks();

		ArgumentCaptor<DispatcherDTO> requestCaptor = ArgumentCaptor.forClass(DispatcherDTO.class);
		Mockito.verify(dispatcherService, Mockito.times(1)).dispatch(requestCaptor.capture(), Mockito.eq(SecurityConstants.FROM_IN), Mockito.eq(SecurityConstants.INTERNAL_SERVICE_AUTH_REQUIRED));
		Assert.assertEquals(EventEnum.ISC_CARD_DELETE.getCode(), requestCaptor.getValue().getEventType());
		Assert.assertEquals(Integer.valueOf(5000021), requestCaptor.getValue().getParkId());
		Map cardRequest = (Map) requestCaptor.getValue().getData();
		Assert.assertEquals("isc-person-1", cardRequest.get("personId"));
		Assert.assertEquals("12345678", cardRequest.get("cardNumber"));
	}

	@Test
	public void syncCardTasksKeepsResolvedPersonIdWhenDeleteCardFails() {
		RemoteDispatcherService dispatcherService = Mockito.mock(RemoteDispatcherService.class);
		SmtIscCardTaskService taskService = Mockito.mock(SmtIscCardTaskService.class);
		SmtIscStaffCardService staffCardService = Mockito.mock(SmtIscStaffCardService.class);
		ISCCardTaskServiceImpl service = new ISCCardTaskServiceImpl(dispatcherService, taskService, staffCardService);
		SmtIscCardTask task = cardTask(DeviceTaskActionEnum.DEL.getCode(), null);
		Page<SmtIscCardTask> page = new Page<>(1, 500);
		page.setRecords(java.util.Collections.singletonList(task));
		Result<String> failedDelete = Result.fail(500, "delete failed", null);
		Mockito.when(taskService.getPendingTasks(Mockito.any(), Mockito.anyLong(), Mockito.anyInt())).thenReturn(page);
		Mockito.when(taskService.markDoing(Mockito.any(SmtIscCardTask.class), Mockito.anyLong(), Mockito.anyLong(), Mockito.anyInt())).thenReturn(true);
		Mockito.when(taskService.isCurrentStaffCardAddTask(Mockito.any(SmtIscCardTask.class))).thenReturn(true);
		Mockito.when(taskService.updateDoingTask(Mockito.any(SmtIscCardTask.class), Mockito.anyString(), Mockito.anyString())).thenReturn(true);
		Mockito.when(dispatcherService.dispatch(Mockito.any(), Mockito.eq(SecurityConstants.FROM_IN), Mockito.eq(SecurityConstants.INTERNAL_SERVICE_AUTH_REQUIRED)))
				.thenReturn(Result.success("{\"list\":[{\"personId\":\"isc-person-1\",\"status\":-1}]}"))
				.thenReturn(failedDelete);

		service.syncCardTasks();

		ArgumentCaptor<SmtIscCardTask> taskCaptor = ArgumentCaptor.forClass(SmtIscCardTask.class);
		Mockito.verify(taskService, Mockito.atLeastOnce()).updateDoingTask(taskCaptor.capture(), Mockito.eq(taskActiveKey(DeviceTaskActionEnum.DEL.getCode())), Mockito.anyString());
		SmtIscCardTask lastUpdate = taskCaptor.getAllValues().get(taskCaptor.getAllValues().size() - 1);
		Assert.assertEquals(DeviceTaskStatusEnum.INIT.getCode(), lastUpdate.getStatus());
		Assert.assertEquals("isc-person-1", lastUpdate.getPersonId());
		Assert.assertEquals("delete failed", lastUpdate.getRemark());
	}

	@Test
	public void syncCardTasksSkipsSameStaffDifferentParkAddAfterDeleteRetry() {
		RemoteDispatcherService dispatcherService = Mockito.mock(RemoteDispatcherService.class);
		SmtIscCardTaskService taskService = Mockito.mock(SmtIscCardTaskService.class);
		SmtIscStaffCardService staffCardService = Mockito.mock(SmtIscStaffCardService.class);
		ISCCardTaskServiceImpl service = new ISCCardTaskServiceImpl(dispatcherService, taskService, staffCardService);
		SmtIscCardTask deleteTask = cardTask(DeviceTaskActionEnum.DEL.getCode(), null);
		SmtIscCardTask addTask = cardTask(DeviceTaskActionEnum.DOWN.getCode(), null);
		addTask.setId(11L);
		addTask.setParkId(6000001);
		addTask.setCardNo("87654321");
		addTask.setActiveKey("STAFF|1001|JA26086|6000001|87654321|1");
		Page<SmtIscCardTask> page = new Page<>(1, 500);
		page.setRecords(Arrays.asList(deleteTask, addTask));
		Result<String> failedDelete = Result.fail(500, "delete failed", null);
		Mockito.when(taskService.getPendingTasks(Mockito.any(), Mockito.anyLong(), Mockito.anyInt())).thenReturn(page);
		Mockito.when(taskService.markDoing(Mockito.any(SmtIscCardTask.class), Mockito.anyLong(), Mockito.anyLong(), Mockito.anyInt())).thenReturn(true);
		Mockito.when(taskService.isCurrentStaffCardAddTask(Mockito.any(SmtIscCardTask.class))).thenReturn(true);
		Mockito.when(taskService.updateDoingTask(Mockito.any(SmtIscCardTask.class), Mockito.anyString(), Mockito.anyString())).thenReturn(true);
		Mockito.when(dispatcherService.dispatch(Mockito.any(), Mockito.eq(SecurityConstants.FROM_IN), Mockito.eq(SecurityConstants.INTERNAL_SERVICE_AUTH_REQUIRED)))
				.thenReturn(Result.success("{\"list\":[{\"personId\":\"isc-person-1\",\"status\":1}]}"))
				.thenReturn(failedDelete);

		service.syncCardTasks();

		ArgumentCaptor<DispatcherDTO> requestCaptor = ArgumentCaptor.forClass(DispatcherDTO.class);
		Mockito.verify(dispatcherService, Mockito.times(2)).dispatch(requestCaptor.capture(), Mockito.eq(SecurityConstants.FROM_IN), Mockito.eq(SecurityConstants.INTERNAL_SERVICE_AUTH_REQUIRED));
		Assert.assertEquals(EventEnum.ISC_PERSON_GET.getCode(), requestCaptor.getAllValues().get(0).getEventType());
		Assert.assertEquals(EventEnum.ISC_CARD_DELETE.getCode(), requestCaptor.getAllValues().get(1).getEventType());
		Mockito.verify(taskService, Mockito.never()).markDoing(
				Mockito.argThat((SmtIscCardTask task) -> Long.valueOf(11L).equals(task.getId())),
				Mockito.anyLong(), Mockito.anyLong(), Mockito.anyInt());
	}

	@Test
	public void syncCardTasksRetriesAddWhenPersonDoesNotExistWithoutCreatingPerson() {
		RemoteDispatcherService dispatcherService = Mockito.mock(RemoteDispatcherService.class);
		SmtIscCardTaskService taskService = Mockito.mock(SmtIscCardTaskService.class);
		SmtIscStaffCardService staffCardService = Mockito.mock(SmtIscStaffCardService.class);
		ISCCardTaskServiceImpl service = new ISCCardTaskServiceImpl(dispatcherService, taskService, staffCardService);
		SmtIscCardTask task = cardTask(DeviceTaskActionEnum.DOWN.getCode(), null);
		Page<SmtIscCardTask> page = new Page<>(1, 500);
		page.setRecords(java.util.Collections.singletonList(task));
		Mockito.when(taskService.getPendingTasks(Mockito.any(), Mockito.anyLong(), Mockito.anyInt())).thenReturn(page);
		Mockito.when(taskService.markDoing(Mockito.any(SmtIscCardTask.class), Mockito.anyLong(), Mockito.anyLong(), Mockito.anyInt())).thenReturn(true);
		Mockito.when(taskService.isCurrentStaffCardAddTask(Mockito.any(SmtIscCardTask.class))).thenReturn(true);
		Mockito.when(taskService.updateDoingTask(Mockito.any(SmtIscCardTask.class), Mockito.anyString(), Mockito.anyString())).thenReturn(true);
		Mockito.when(dispatcherService.dispatch(Mockito.any(), Mockito.eq(SecurityConstants.FROM_IN), Mockito.eq(SecurityConstants.INTERNAL_SERVICE_AUTH_REQUIRED)))
				.thenReturn(Result.success("{\"list\":[]}"));

		service.syncCardTasks();

		ArgumentCaptor<DispatcherDTO> requestCaptor = ArgumentCaptor.forClass(DispatcherDTO.class);
		Mockito.verify(dispatcherService, Mockito.times(1)).dispatch(requestCaptor.capture(), Mockito.eq(SecurityConstants.FROM_IN), Mockito.eq(SecurityConstants.INTERNAL_SERVICE_AUTH_REQUIRED));
		Assert.assertEquals(EventEnum.ISC_PERSON_GET.getCode(), requestCaptor.getValue().getEventType());

		ArgumentCaptor<SmtIscCardTask> taskCaptor = ArgumentCaptor.forClass(SmtIscCardTask.class);
		Mockito.verify(taskService).updateDoingTask(taskCaptor.capture(), Mockito.eq(taskActiveKey(DeviceTaskActionEnum.DOWN.getCode())), Mockito.anyString());
		SmtIscCardTask lastUpdate = taskCaptor.getValue();
		Assert.assertEquals(DeviceTaskStatusEnum.INIT.getCode(), lastUpdate.getStatus());
		Assert.assertEquals(ISCDeviceTaskEnum.AUTH_CONFIG_DOWN_FAIL.getCode(), lastUpdate.getCode());
		Assert.assertEquals("未解析到ISC人员ID", lastUpdate.getRemark());
	}

	@Test
	public void syncCardTasksSkipsTaskWhenDoingLeaseCannotBeAcquired() {
		RemoteDispatcherService dispatcherService = Mockito.mock(RemoteDispatcherService.class);
		SmtIscCardTaskService taskService = Mockito.mock(SmtIscCardTaskService.class);
		SmtIscStaffCardService staffCardService = Mockito.mock(SmtIscStaffCardService.class);
		ISCCardTaskServiceImpl service = new ISCCardTaskServiceImpl(dispatcherService, taskService, staffCardService);
		SmtIscCardTask task = cardTask(DeviceTaskActionEnum.DOWN.getCode(), null);
		Page<SmtIscCardTask> page = new Page<>(1, 500);
		page.setRecords(java.util.Collections.singletonList(task));
		Mockito.when(taskService.getPendingTasks(Mockito.any(), Mockito.anyLong(), Mockito.anyInt())).thenReturn(page);
		Mockito.when(taskService.markDoing(Mockito.any(SmtIscCardTask.class), Mockito.anyLong(), Mockito.anyLong(), Mockito.anyInt())).thenReturn(false);

		service.syncCardTasks();

		Mockito.verify(dispatcherService, Mockito.never()).dispatch(Mockito.any(), Mockito.anyString(), Mockito.anyString());
		Mockito.verify(taskService, Mockito.never()).updateDoingTask(Mockito.any(SmtIscCardTask.class), Mockito.anyString(), Mockito.anyString());
	}

	@Test
	public void syncCardTasksCancelsVirtualCardTaskWithoutDispatchingToIsc() {
		RemoteDispatcherService dispatcherService = Mockito.mock(RemoteDispatcherService.class);
		SmtIscCardTaskService taskService = Mockito.mock(SmtIscCardTaskService.class);
		SmtIscStaffCardService staffCardService = Mockito.mock(SmtIscStaffCardService.class);
		ISCCardTaskServiceImpl service = new ISCCardTaskServiceImpl(dispatcherService, taskService, staffCardService);
		SmtIscCardTask task = cardTask(DeviceTaskActionEnum.DOWN.getCode(), null);
		task.setCardNo("9990000001");
		Page<SmtIscCardTask> page = new Page<>(1, 500);
		page.setRecords(java.util.Collections.singletonList(task));
		Mockito.when(taskService.getPendingTasks(Mockito.any(), Mockito.anyLong(), Mockito.anyInt())).thenReturn(page);
		Mockito.when(taskService.markDoing(Mockito.any(SmtIscCardTask.class), Mockito.anyLong(), Mockito.anyLong(), Mockito.anyInt())).thenReturn(true);
		Mockito.when(taskService.isCurrentStaffCardAddTask(Mockito.any(SmtIscCardTask.class))).thenReturn(true);
		Mockito.when(taskService.updateDoingTask(Mockito.any(SmtIscCardTask.class), Mockito.anyString(), Mockito.anyString())).thenReturn(true);

		service.syncCardTasks();

		Mockito.verify(dispatcherService, Mockito.never()).dispatch(Mockito.any(), Mockito.anyString(), Mockito.anyString());
		ArgumentCaptor<SmtIscCardTask> taskCaptor = ArgumentCaptor.forClass(SmtIscCardTask.class);
		Mockito.verify(taskService).updateDoingTask(taskCaptor.capture(), Mockito.eq(taskActiveKey(DeviceTaskActionEnum.DOWN.getCode())), Mockito.anyString());
		SmtIscCardTask lastUpdate = taskCaptor.getValue();
		Assert.assertEquals(DeviceTaskStatusEnum.CANCEL.getCode(), lastUpdate.getStatus());
		Assert.assertEquals("ISC虚拟卡号不执行同步", lastUpdate.getRemark());
		Assert.assertNull(lastUpdate.getActiveKey());
		Mockito.verify(staffCardService).markAddTaskFailed(lastUpdate, true);
	}

	@Test
	public void syncCardTasksCancelsInvalidCardNoWithoutDispatchingToIsc() {
		RemoteDispatcherService dispatcherService = Mockito.mock(RemoteDispatcherService.class);
		SmtIscCardTaskService taskService = Mockito.mock(SmtIscCardTaskService.class);
		SmtIscStaffCardService staffCardService = Mockito.mock(SmtIscStaffCardService.class);
		ISCCardTaskServiceImpl service = new ISCCardTaskServiceImpl(dispatcherService, taskService, staffCardService);
		SmtIscCardTask task = cardTask(DeviceTaskActionEnum.DOWN.getCode(), null);
		task.setCardNo("111111");
		task.setActiveKey("STAFF|1001|JA26086|5000021|111111|1");
		Page<SmtIscCardTask> page = new Page<>(1, 500);
		page.setRecords(java.util.Collections.singletonList(task));
		Mockito.when(taskService.getPendingTasks(Mockito.any(), Mockito.anyLong(), Mockito.anyInt())).thenReturn(page);
		Mockito.when(taskService.markDoing(Mockito.any(SmtIscCardTask.class), Mockito.anyLong(), Mockito.anyLong(), Mockito.anyInt())).thenReturn(true);
		Mockito.when(taskService.isCurrentStaffCardAddTask(Mockito.any(SmtIscCardTask.class))).thenReturn(true);
		Mockito.when(taskService.updateDoingTask(Mockito.any(SmtIscCardTask.class), Mockito.anyString(), Mockito.anyString())).thenReturn(true);

		service.syncCardTasks();

		Mockito.verify(dispatcherService, Mockito.never()).dispatch(Mockito.any(), Mockito.anyString(), Mockito.anyString());
		ArgumentCaptor<SmtIscCardTask> taskCaptor = ArgumentCaptor.forClass(SmtIscCardTask.class);
		Mockito.verify(taskService).updateDoingTask(taskCaptor.capture(), Mockito.eq("STAFF|1001|JA26086|5000021|111111|1"), Mockito.anyString());
		SmtIscCardTask lastUpdate = taskCaptor.getValue();
		Assert.assertEquals(DeviceTaskStatusEnum.CANCEL.getCode(), lastUpdate.getStatus());
		Assert.assertTrue(lastUpdate.getRemark().contains("8-20位数字或大写字母"));
		Assert.assertNull(lastUpdate.getActiveKey());
		Mockito.verify(staffCardService).markAddTaskFailed(lastUpdate, true);
	}

	@Test
	public void syncCardTasksTreatsDeleteCardNotExistsAsSuccess() {
		RemoteDispatcherService dispatcherService = Mockito.mock(RemoteDispatcherService.class);
		SmtIscCardTaskService taskService = Mockito.mock(SmtIscCardTaskService.class);
		SmtIscStaffCardService staffCardService = Mockito.mock(SmtIscStaffCardService.class);
		ISCCardTaskServiceImpl service = new ISCCardTaskServiceImpl(dispatcherService, taskService, staffCardService);
		SmtIscCardTask task = cardTask(DeviceTaskActionEnum.DEL.getCode(), "isc-person-1");
		Page<SmtIscCardTask> page = new Page<>(1, 500);
		page.setRecords(java.util.Collections.singletonList(task));
		Mockito.when(taskService.getPendingTasks(Mockito.any(), Mockito.anyLong(), Mockito.anyInt())).thenReturn(page);
		Mockito.when(taskService.markDoing(Mockito.any(SmtIscCardTask.class), Mockito.anyLong(), Mockito.anyLong(), Mockito.anyInt())).thenReturn(true);
		Mockito.when(taskService.isCurrentStaffCardAddTask(Mockito.any(SmtIscCardTask.class))).thenReturn(true);
		Mockito.when(taskService.updateDoingTask(Mockito.any(SmtIscCardTask.class), Mockito.anyString(), Mockito.anyString())).thenReturn(true);
		Mockito.when(dispatcherService.dispatch(Mockito.any(), Mockito.eq(SecurityConstants.FROM_IN), Mockito.eq(SecurityConstants.INTERNAL_SERVICE_AUTH_REQUIRED)))
				.thenReturn(Result.fail(77668387, "ISC接口请求异常: 卡号不存在 (code=0x04a12023, rawMsg=cardNo 12345678 is not exists)", null));

		service.syncCardTasks();

		ArgumentCaptor<SmtIscCardTask> taskCaptor = ArgumentCaptor.forClass(SmtIscCardTask.class);
		Mockito.verify(taskService).updateDoingTask(taskCaptor.capture(), Mockito.eq(taskActiveKey(DeviceTaskActionEnum.DEL.getCode())), Mockito.anyString());
		SmtIscCardTask lastUpdate = taskCaptor.getValue();
		Assert.assertEquals(DeviceTaskStatusEnum.SUCCESS.getCode(), lastUpdate.getStatus());
		Assert.assertEquals(ISCDeviceTaskEnum.DEVICE_OK.getCode(), lastUpdate.getCode());
		Assert.assertEquals("isc-person-1", lastUpdate.getPersonId());
		Assert.assertTrue(lastUpdate.getRemark().contains("ISC卡片已不存在"));
		Assert.assertNull(lastUpdate.getActiveKey());
	}

	@Test
	public void syncCardTasksCancelsAddWhenIscReportsPermanentCardNoFormatError() {
		RemoteDispatcherService dispatcherService = Mockito.mock(RemoteDispatcherService.class);
		SmtIscCardTaskService taskService = Mockito.mock(SmtIscCardTaskService.class);
		SmtIscStaffCardService staffCardService = Mockito.mock(SmtIscStaffCardService.class);
		ISCCardTaskServiceImpl service = new ISCCardTaskServiceImpl(dispatcherService, taskService, staffCardService);
		SmtIscCardTask task = cardTask(DeviceTaskActionEnum.DOWN.getCode(), null);
		Page<SmtIscCardTask> page = new Page<>(1, 500);
		page.setRecords(java.util.Collections.singletonList(task));
		Mockito.when(taskService.getPendingTasks(Mockito.any(), Mockito.anyLong(), Mockito.anyInt())).thenReturn(page);
		Mockito.when(taskService.markDoing(Mockito.any(SmtIscCardTask.class), Mockito.anyLong(), Mockito.anyLong(), Mockito.anyInt())).thenReturn(true);
		Mockito.when(taskService.isCurrentStaffCardAddTask(Mockito.any(SmtIscCardTask.class))).thenReturn(true);
		Mockito.when(taskService.updateDoingTask(Mockito.any(SmtIscCardTask.class), Mockito.anyString(), Mockito.anyString())).thenReturn(true);
		Mockito.when(dispatcherService.dispatch(Mockito.any(), Mockito.eq(SecurityConstants.FROM_IN), Mockito.eq(SecurityConstants.INTERNAL_SERVICE_AUTH_REQUIRED)))
				.thenReturn(Result.success("{\"list\":[{\"personId\":\"isc-person-1\",\"status\":1}]}"))
				.thenReturn(Result.fail(466947, "ISC接口请求异常: 参数错误：参数格式不正确 (code=0x072003, rawMsg=the required parameter cardNo format error)", null));

		service.syncCardTasks();

		ArgumentCaptor<SmtIscCardTask> taskCaptor = ArgumentCaptor.forClass(SmtIscCardTask.class);
		Mockito.verify(taskService, Mockito.atLeastOnce()).updateDoingTask(taskCaptor.capture(), Mockito.eq(taskActiveKey(DeviceTaskActionEnum.DOWN.getCode())), Mockito.anyString());
		SmtIscCardTask lastUpdate = taskCaptor.getAllValues().get(taskCaptor.getAllValues().size() - 1);
		Assert.assertEquals(DeviceTaskStatusEnum.CANCEL.getCode(), lastUpdate.getStatus());
		Assert.assertEquals(ISCDeviceTaskEnum.AUTH_CONFIG_DOWN_FAIL.getCode(), lastUpdate.getCode());
		Assert.assertTrue(lastUpdate.getRemark().contains("cardNo format error"));
		Assert.assertNull(lastUpdate.getActiveKey());
		Mockito.verify(staffCardService).markAddTaskFailed(lastUpdate, true);
	}

	@Test
	public void syncCardTasksCancelsAddWhenIscReportsOfficialPermanentCardErrorCode() {
		RemoteDispatcherService dispatcherService = Mockito.mock(RemoteDispatcherService.class);
		SmtIscCardTaskService taskService = Mockito.mock(SmtIscCardTaskService.class);
		SmtIscStaffCardService staffCardService = Mockito.mock(SmtIscStaffCardService.class);
		ISCCardTaskServiceImpl service = new ISCCardTaskServiceImpl(dispatcherService, taskService, staffCardService);
		SmtIscCardTask task = cardTask(DeviceTaskActionEnum.DOWN.getCode(), null);
		Page<SmtIscCardTask> page = new Page<>(1, 500);
		page.setRecords(java.util.Collections.singletonList(task));
		Mockito.when(taskService.getPendingTasks(Mockito.any(), Mockito.anyLong(), Mockito.anyInt())).thenReturn(page);
		Mockito.when(taskService.markDoing(Mockito.any(SmtIscCardTask.class), Mockito.anyLong(), Mockito.anyLong(), Mockito.anyInt())).thenReturn(true);
		Mockito.when(taskService.isCurrentStaffCardAddTask(Mockito.any(SmtIscCardTask.class))).thenReturn(true);
		Mockito.when(taskService.updateDoingTask(Mockito.any(SmtIscCardTask.class), Mockito.anyString(), Mockito.anyString())).thenReturn(true);
		Mockito.when(dispatcherService.dispatch(Mockito.any(), Mockito.eq(SecurityConstants.FROM_IN), Mockito.eq(SecurityConstants.INTERNAL_SERVICE_AUTH_REQUIRED)))
				.thenReturn(Result.success("{\"list\":[{\"personId\":\"isc-person-1\",\"status\":1}]}"))
				.thenReturn(Result.fail(0x04a12703, "ISC接口请求异常: 人员与卡号不是所属关系 (code=0x04a12703)", null));

		service.syncCardTasks();

		ArgumentCaptor<SmtIscCardTask> taskCaptor = ArgumentCaptor.forClass(SmtIscCardTask.class);
		Mockito.verify(taskService, Mockito.atLeastOnce()).updateDoingTask(taskCaptor.capture(), Mockito.eq(taskActiveKey(DeviceTaskActionEnum.DOWN.getCode())), Mockito.anyString());
		SmtIscCardTask lastUpdate = taskCaptor.getAllValues().get(taskCaptor.getAllValues().size() - 1);
		Assert.assertEquals(DeviceTaskStatusEnum.CANCEL.getCode(), lastUpdate.getStatus());
		Assert.assertEquals(ISCDeviceTaskEnum.AUTH_CONFIG_DOWN_FAIL.getCode(), lastUpdate.getCode());
		Assert.assertEquals("ISC接口请求异常: 人员与卡号不是所属关系 (code=0x04a12703)", lastUpdate.getRemark());
		Assert.assertNull(lastUpdate.getActiveKey());
		Mockito.verify(staffCardService).markAddTaskFailed(lastUpdate, false);
	}

	@Test
	public void syncCardTasksCancelsObsoleteAddTaskBeforeQueryingIscPerson() {
		RemoteDispatcherService dispatcherService = Mockito.mock(RemoteDispatcherService.class);
		SmtIscCardTaskService taskService = Mockito.mock(SmtIscCardTaskService.class);
		SmtIscStaffCardService staffCardService = Mockito.mock(SmtIscStaffCardService.class);
		ISCCardTaskServiceImpl service = new ISCCardTaskServiceImpl(dispatcherService, taskService, staffCardService);
		SmtIscCardTask task = cardTask(DeviceTaskActionEnum.DOWN.getCode(), null);
		Page<SmtIscCardTask> page = new Page<>(1, 500);
		page.setRecords(java.util.Collections.singletonList(task));
		Mockito.when(taskService.getPendingTasks(Mockito.any(), Mockito.anyLong(), Mockito.anyInt())).thenReturn(page);
		Mockito.when(taskService.markDoing(Mockito.any(SmtIscCardTask.class), Mockito.anyLong(), Mockito.anyLong(), Mockito.anyInt())).thenReturn(true);
		Mockito.when(taskService.isCurrentStaffCardAddTask(task)).thenReturn(false);
		Mockito.when(taskService.updateDoingTask(Mockito.any(SmtIscCardTask.class), Mockito.anyString(), Mockito.anyString())).thenReturn(true);

		service.syncCardTasks();

		Mockito.verify(dispatcherService, Mockito.never()).dispatch(Mockito.any(), Mockito.anyString(), Mockito.anyString());
		ArgumentCaptor<SmtIscCardTask> taskCaptor = ArgumentCaptor.forClass(SmtIscCardTask.class);
		Mockito.verify(taskService).updateDoingTask(taskCaptor.capture(), Mockito.eq(taskActiveKey(DeviceTaskActionEnum.DOWN.getCode())), Mockito.anyString());
		SmtIscCardTask lastUpdate = taskCaptor.getValue();
		Assert.assertEquals(DeviceTaskStatusEnum.CANCEL.getCode(), lastUpdate.getStatus());
		Assert.assertEquals("ISC卡片新增任务已不是当前卡号", lastUpdate.getRemark());
		Assert.assertNull(lastUpdate.getActiveKey());
	}

	@Test
	public void addCardAlreadyExistsOwnedByPersonIsTreatedAsSuccess() {
		RemoteDispatcherService dispatcherService = Mockito.mock(RemoteDispatcherService.class);
		SmtIscCardTaskService taskService = Mockito.mock(SmtIscCardTaskService.class);
		SmtIscStaffCardService staffCardService = Mockito.mock(SmtIscStaffCardService.class);
		ISCCardTaskServiceImpl service = new ISCCardTaskServiceImpl(dispatcherService, taskService, staffCardService);
		SmtIscCardTask task = cardTask(DeviceTaskActionEnum.DOWN.getCode(), "isc-person-1");
		Page<SmtIscCardTask> page = new Page<>(1, 500);
		page.setRecords(java.util.Collections.singletonList(task));
		Mockito.when(taskService.getPendingTasks(Mockito.any(), Mockito.anyLong(), Mockito.anyInt())).thenReturn(page);
		Mockito.when(taskService.markDoing(Mockito.any(SmtIscCardTask.class), Mockito.anyLong(), Mockito.anyLong(), Mockito.anyInt())).thenReturn(true);
		Mockito.when(taskService.isCurrentStaffCardAddTask(Mockito.any(SmtIscCardTask.class))).thenReturn(true);
		Mockito.when(taskService.updateDoingTask(Mockito.any(SmtIscCardTask.class), Mockito.anyString(), Mockito.anyString())).thenReturn(true);
		Result<String> cardExists = Result.fail(0x04a12700, "卡号已存在");
		Mockito.when(dispatcherService.dispatch(Mockito.any(), Mockito.eq(SecurityConstants.FROM_IN), Mockito.eq(SecurityConstants.INTERNAL_SERVICE_AUTH_REQUIRED)))
				.thenReturn(cardExists)
				.thenReturn(Result.success("{\"total\":1,\"list\":[{\"personId\":\"isc-person-1\",\"cardNo\":\"12345678\"}]}"));

		service.syncCardTasks();

		ArgumentCaptor<SmtIscCardTask> taskCaptor = ArgumentCaptor.forClass(SmtIscCardTask.class);
		Mockito.verify(taskService, Mockito.atLeastOnce()).updateDoingTask(taskCaptor.capture(), Mockito.anyString(), Mockito.anyString());
		SmtIscCardTask lastUpdate = taskCaptor.getAllValues().get(taskCaptor.getAllValues().size() - 1);
		Assert.assertEquals(DeviceTaskStatusEnum.SUCCESS.getCode(), lastUpdate.getStatus());
		Mockito.verify(staffCardService).markAddTaskSuccess(Mockito.any(SmtIscCardTask.class));
	}

	@Test
	public void addCardAlreadyExistsOwnedByOthersIsCancelled() {
		RemoteDispatcherService dispatcherService = Mockito.mock(RemoteDispatcherService.class);
		SmtIscCardTaskService taskService = Mockito.mock(SmtIscCardTaskService.class);
		SmtIscStaffCardService staffCardService = Mockito.mock(SmtIscStaffCardService.class);
		ISCCardTaskServiceImpl service = new ISCCardTaskServiceImpl(dispatcherService, taskService, staffCardService);
		SmtIscCardTask task = cardTask(DeviceTaskActionEnum.DOWN.getCode(), "isc-person-1");
		Page<SmtIscCardTask> page = new Page<>(1, 500);
		page.setRecords(java.util.Collections.singletonList(task));
		Mockito.when(taskService.getPendingTasks(Mockito.any(), Mockito.anyLong(), Mockito.anyInt())).thenReturn(page);
		Mockito.when(taskService.markDoing(Mockito.any(SmtIscCardTask.class), Mockito.anyLong(), Mockito.anyLong(), Mockito.anyInt())).thenReturn(true);
		Mockito.when(taskService.isCurrentStaffCardAddTask(Mockito.any(SmtIscCardTask.class))).thenReturn(true);
		Mockito.when(taskService.updateDoingTask(Mockito.any(SmtIscCardTask.class), Mockito.anyString(), Mockito.anyString())).thenReturn(true);
		Result<String> cardExists = Result.fail(0x04a12700, "卡号已存在");
		// 本人名下没有这张卡 => 卡绑定在他人名下
		Mockito.when(dispatcherService.dispatch(Mockito.any(), Mockito.eq(SecurityConstants.FROM_IN), Mockito.eq(SecurityConstants.INTERNAL_SERVICE_AUTH_REQUIRED)))
				.thenReturn(cardExists)
				.thenReturn(Result.success("{\"total\":1,\"list\":[{\"personId\":\"isc-person-1\",\"cardNo\":\"99999999\"}]}"));

		service.syncCardTasks();

		ArgumentCaptor<SmtIscCardTask> taskCaptor = ArgumentCaptor.forClass(SmtIscCardTask.class);
		Mockito.verify(taskService, Mockito.atLeastOnce()).updateDoingTask(taskCaptor.capture(), Mockito.anyString(), Mockito.anyString());
		SmtIscCardTask lastUpdate = taskCaptor.getAllValues().get(taskCaptor.getAllValues().size() - 1);
		Assert.assertEquals(DeviceTaskStatusEnum.CANCEL.getCode(), lastUpdate.getStatus());
		Mockito.verify(staffCardService).markAddTaskFailed(Mockito.any(SmtIscCardTask.class), Mockito.eq(false));
	}

	@Test
	public void addCardAlreadyExistsWithOwnershipQueryFailureIsRetried() {
		RemoteDispatcherService dispatcherService = Mockito.mock(RemoteDispatcherService.class);
		SmtIscCardTaskService taskService = Mockito.mock(SmtIscCardTaskService.class);
		SmtIscStaffCardService staffCardService = Mockito.mock(SmtIscStaffCardService.class);
		ISCCardTaskServiceImpl service = new ISCCardTaskServiceImpl(dispatcherService, taskService, staffCardService);
		SmtIscCardTask task = cardTask(DeviceTaskActionEnum.DOWN.getCode(), "isc-person-1");
		Page<SmtIscCardTask> page = new Page<>(1, 500);
		page.setRecords(java.util.Collections.singletonList(task));
		Mockito.when(taskService.getPendingTasks(Mockito.any(), Mockito.anyLong(), Mockito.anyInt())).thenReturn(page);
		Mockito.when(taskService.markDoing(Mockito.any(SmtIscCardTask.class), Mockito.anyLong(), Mockito.anyLong(), Mockito.anyInt())).thenReturn(true);
		Mockito.when(taskService.isCurrentStaffCardAddTask(Mockito.any(SmtIscCardTask.class))).thenReturn(true);
		Mockito.when(taskService.updateDoingTask(Mockito.any(SmtIscCardTask.class), Mockito.anyString(), Mockito.anyString())).thenReturn(true);
		Result<String> cardExists = Result.fail(0x04a12700, "卡号已存在");
		Mockito.when(dispatcherService.dispatch(Mockito.any(), Mockito.eq(SecurityConstants.FROM_IN), Mockito.eq(SecurityConstants.INTERNAL_SERVICE_AUTH_REQUIRED)))
				.thenReturn(cardExists)
				.thenReturn(Result.fail("ISC查询超时"));

		service.syncCardTasks();

		ArgumentCaptor<SmtIscCardTask> taskCaptor = ArgumentCaptor.forClass(SmtIscCardTask.class);
		Mockito.verify(taskService, Mockito.atLeastOnce()).updateDoingTask(taskCaptor.capture(), Mockito.anyString(), Mockito.anyString());
		SmtIscCardTask lastUpdate = taskCaptor.getAllValues().get(taskCaptor.getAllValues().size() - 1);
		Assert.assertEquals(DeviceTaskStatusEnum.INIT.getCode(), lastUpdate.getStatus());
		Mockito.verify(staffCardService, Mockito.never()).markAddTaskSuccess(Mockito.any());
		Mockito.verify(staffCardService, Mockito.never()).markAddTaskFailed(Mockito.any(), Mockito.anyBoolean());
	}

	private SmtIscCardTask cardTask(Integer action, String personId) {
		SmtIscCardTask task = new SmtIscCardTask();
		task.setId(10L);
		task.setAction(action);
		task.setStatus(DeviceTaskStatusEnum.INIT.getCode());
		task.setParkId(5000021);
		task.setSourceType("STAFF");
		task.setSourceId(1001L);
		task.setBadge("JA26086");
		task.setPersonId(personId);
		task.setCardNo("12345678");
		task.setActiveKey(taskActiveKey(action));
		task.setLeaseToken("lease-" + action);
		task.setTimes(0);
		return task;
	}

	private String taskActiveKey(Integer action) {
		return "STAFF|1001|JA26086|5000021|12345678|" + action;
	}
}
