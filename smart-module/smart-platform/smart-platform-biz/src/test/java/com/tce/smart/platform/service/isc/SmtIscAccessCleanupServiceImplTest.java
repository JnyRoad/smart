package com.tce.smart.platform.service.isc;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tce.smart.platform.api.dto.req.isc.IscAccessCleanupExecuteReqDTO;
import com.tce.smart.platform.api.dto.req.isc.IscAccessCleanupPageReqDTO;
import com.tce.smart.platform.api.dto.resp.isc.IscAccessCleanupExecuteRespDTO;
import com.tce.smart.platform.api.dto.resp.isc.IscAccessCleanupSummaryRespDTO;
import com.tce.smart.platform.core.entity.SmtIscDeviceTask;
import com.tce.smart.platform.core.entity.SmtIscDownRecord;
import com.tce.smart.platform.core.entity.SmtStaff;
import com.tce.smart.platform.core.enums.ISCDeviceTaskEnum;
import com.tce.smart.platform.core.mapper.SmtIscAccessCleanupMapper;
import com.tce.smart.platform.core.mapper.SmtStaffMapper;
import com.tce.smart.platform.core.service.SmtIscDeviceTaskService;
import com.tce.smart.platform.core.service.SmtIscDownRecordService;
import com.tce.smart.platform.service.isc.impl.SmtIscAccessCleanupServiceImpl;
import com.tce.smart.tool.constant.DeviceTaskConstants;
import com.tce.smart.tool.enums.DeviceTaskActionEnum;
import com.tce.smart.tool.enums.DeviceTaskStatusEnum;
import com.tce.smart.tool.enums.StaffStatusEnum;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;

@SuppressWarnings({"rawtypes", "unchecked"})
public class SmtIscAccessCleanupServiceImplTest {

	@BeforeClass
	public static void initMybatisPlusLambdaCache() {
		TableInfoHelper.initTableInfo(new MapperBuilderAssistant(new MybatisConfiguration(), ""), SmtIscDownRecord.class);
		TableInfoHelper.initTableInfo(new MapperBuilderAssistant(new MybatisConfiguration(), ""), SmtIscDeviceTask.class);
	}

	@Test
	public void pageDisablesHeavyAutomaticCountAndUsesSummaryTotal() {
		SmtIscAccessCleanupMapper cleanupMapper = Mockito.mock(SmtIscAccessCleanupMapper.class);
		SmtIscDownRecordService downRecordService = Mockito.mock(SmtIscDownRecordService.class);
		SmtIscDeviceTaskService deviceTaskService = Mockito.mock(SmtIscDeviceTaskService.class);
		SmtStaffMapper staffMapper = Mockito.mock(SmtStaffMapper.class);
		SmtIscAccessCleanupService service = new SmtIscAccessCleanupServiceImpl(cleanupMapper,
				downRecordService, deviceTaskService, staffMapper);
		IscAccessCleanupSummaryRespDTO summary = new IscAccessCleanupSummaryRespDTO();
		summary.setTotalCount(42);
		Page resultPage = new Page(1, 20);
		Mockito.when(cleanupMapper.getSummary(Mockito.any(), Mockito.any())).thenReturn(summary);
		Mockito.when(cleanupMapper.getPage(Mockito.any(Page.class), Mockito.any(), Mockito.any())).thenReturn(resultPage);

		IPage result = service.getPage(new Page(1, 20), new IscAccessCleanupPageReqDTO(),
				Collections.singletonList(5000021));

		Assert.assertEquals(42L, result.getTotal());
		ArgumentCaptor<Page> pageCaptor = ArgumentCaptor.forClass(Page.class);
		Mockito.verify(cleanupMapper).getPage(pageCaptor.capture(), Mockito.any(), Mockito.any());
		Assert.assertFalse(pageCaptor.getValue().isSearchCount());
	}

	@Test
	public void summaryDoesNotLoadDetailRowsOrDecorateDeleteTasks() {
		SmtIscAccessCleanupMapper cleanupMapper = Mockito.mock(SmtIscAccessCleanupMapper.class);
		SmtIscDownRecordService downRecordService = Mockito.mock(SmtIscDownRecordService.class);
		SmtIscDeviceTaskService deviceTaskService = Mockito.mock(SmtIscDeviceTaskService.class);
		SmtStaffMapper staffMapper = Mockito.mock(SmtStaffMapper.class);
		SmtIscAccessCleanupService service = new SmtIscAccessCleanupServiceImpl(cleanupMapper,
				downRecordService, deviceTaskService, staffMapper);
		IscAccessCleanupSummaryRespDTO mapperSummary = new IscAccessCleanupSummaryRespDTO();
		mapperSummary.setTotalCount(7);
		Mockito.when(cleanupMapper.getSummary(Mockito.any(), Mockito.any())).thenReturn(mapperSummary);

		IscAccessCleanupSummaryRespDTO result = service.getSummary(new IscAccessCleanupPageReqDTO(),
				Collections.singletonList(5000021));

		Assert.assertSame(mapperSummary, result);
		Mockito.verify(cleanupMapper).getSummary(Mockito.any(), Mockito.any());
		Mockito.verify(cleanupMapper, Mockito.never()).listRecords(Mockito.any(), Mockito.any(), Mockito.anyInt());
		Mockito.verify(deviceTaskService, Mockito.never()).list(Mockito.any(LambdaQueryWrapper.class));
	}

	@Test
	public void executeSkipsExpiredVisitorWhenSameIdentityStillHasActiveAccess() {
		SmtIscDownRecordService downRecordService = Mockito.mock(SmtIscDownRecordService.class);
		SmtIscDeviceTaskService deviceTaskService = Mockito.mock(SmtIscDeviceTaskService.class);
		SmtIscAccessCleanupService service = service(downRecordService, deviceTaskService);
		SmtIscDownRecord expiredVisitor = visitorRecord(1L, "old-card", "visitor-cert-1", "isc-person-1");
		Mockito.when(downRecordService.getById(1L)).thenReturn(expiredVisitor);
		Mockito.when(downRecordService.count(Mockito.any(LambdaQueryWrapper.class))).thenReturn(1);

		IscAccessCleanupExecuteRespDTO result = service.execute(executeReq(1L), Collections.singletonList(5000021));

		Assert.assertEquals(Integer.valueOf(1), result.getSkipCount());
		Assert.assertEquals(Integer.valueOf(0), result.getCreatedCount());
		Assert.assertEquals(Integer.valueOf(0), result.getUpdatedCount());
		Mockito.verify(deviceTaskService, Mockito.never()).save(Mockito.any(SmtIscDeviceTask.class));
		Mockito.verify(deviceTaskService, Mockito.never()).updateById(Mockito.any(SmtIscDeviceTask.class));
	}

	@Test
	public void executeCreatesDeleteTaskForResignedStaffDownRecord() {
		SmtIscDownRecordService downRecordService = Mockito.mock(SmtIscDownRecordService.class);
		SmtIscDeviceTaskService deviceTaskService = Mockito.mock(SmtIscDeviceTaskService.class);
		SmtIscAccessCleanupService service = service(downRecordService, deviceTaskService);
		SmtIscDownRecord resignedStaffRecord = staffRecord(2L, "1001");
		Mockito.when(downRecordService.getById(2L)).thenReturn(resignedStaffRecord);
		Mockito.when(deviceTaskService.list(Mockito.any(LambdaQueryWrapper.class))).thenReturn(Collections.emptyList());
		Mockito.when(deviceTaskService.save(Mockito.any(SmtIscDeviceTask.class))).thenReturn(Boolean.TRUE);

		IscAccessCleanupExecuteRespDTO result = service.execute(executeReq(2L), Collections.singletonList(5000021));

		Assert.assertEquals(Integer.valueOf(1), result.getCreatedCount());
		Assert.assertEquals(Integer.valueOf(0), result.getSkipCount());
		ArgumentCaptor<SmtIscDeviceTask> taskCaptor = ArgumentCaptor.forClass(SmtIscDeviceTask.class);
		Mockito.verify(deviceTaskService).save(taskCaptor.capture());
		SmtIscDeviceTask deleteTask = taskCaptor.getValue();
		Assert.assertEquals(DeviceTaskActionEnum.DEL.getCode(), deleteTask.getAction());
		Assert.assertEquals(DeviceTaskStatusEnum.INIT.getCode(), deleteTask.getStatus());
		Assert.assertEquals(DeviceTaskConstants.CARD_STAFF_IMPORT, deleteTask.getServiceType());
		Assert.assertEquals("1001", deleteTask.getCardNo());
		Assert.assertEquals("device-1", deleteTask.getDeviceCode());
	}

	@Test
	public void executeSkipsStaffRecordWhenStaffIsStillActive() {
		SmtIscDownRecordService downRecordService = Mockito.mock(SmtIscDownRecordService.class);
		SmtIscDeviceTaskService deviceTaskService = Mockito.mock(SmtIscDeviceTaskService.class);
		SmtStaffMapper staffMapper = Mockito.mock(SmtStaffMapper.class);
		SmtIscAccessCleanupService service = service(downRecordService, deviceTaskService, staffMapper);
		SmtIscDownRecord activeStaffRecord = staffRecord(4L, "1002");
		SmtStaff activeStaff = new SmtStaff();
		activeStaff.setId(1002L);
		activeStaff.setStatus(StaffStatusEnum.STAFF_STATUS_IN.getCode());
		Mockito.when(downRecordService.getById(4L)).thenReturn(activeStaffRecord);
		Mockito.when(staffMapper.selectById(1002L)).thenReturn(activeStaff);

		IscAccessCleanupExecuteRespDTO result = service.execute(executeReq(4L), Collections.singletonList(5000021));

		Assert.assertEquals(Integer.valueOf(1), result.getSkipCount());
		Mockito.verify(deviceTaskService, Mockito.never()).save(Mockito.any(SmtIscDeviceTask.class));
		Mockito.verify(deviceTaskService, Mockito.never()).updateById(Mockito.any(SmtIscDeviceTask.class));
	}

	@Test
	public void executeSkipsSelectedRecordWhenNoParkAllowed() {
		SmtIscDownRecordService downRecordService = Mockito.mock(SmtIscDownRecordService.class);
		SmtIscDeviceTaskService deviceTaskService = Mockito.mock(SmtIscDeviceTaskService.class);
		SmtIscAccessCleanupService service = service(downRecordService, deviceTaskService);
		SmtIscDownRecord resignedStaffRecord = staffRecord(5L, "1001");
		Mockito.when(downRecordService.getById(5L)).thenReturn(resignedStaffRecord);

		IscAccessCleanupExecuteRespDTO result = service.execute(executeReq(5L), Collections.emptyList());

		Assert.assertEquals(Integer.valueOf(1), result.getSkipCount());
		Assert.assertEquals(Integer.valueOf(0), result.getCreatedCount());
		Mockito.verify(deviceTaskService, Mockito.never()).save(Mockito.any(SmtIscDeviceTask.class));
		Mockito.verify(deviceTaskService, Mockito.never()).updateById(Mockito.any(SmtIscDeviceTask.class));
	}

	@Test
	public void executeRefreshesFailedDeleteTaskInsteadOfCreatingDuplicate() {
		SmtIscDownRecordService downRecordService = Mockito.mock(SmtIscDownRecordService.class);
		SmtIscDeviceTaskService deviceTaskService = Mockito.mock(SmtIscDeviceTaskService.class);
		SmtIscAccessCleanupService service = service(downRecordService, deviceTaskService);
		SmtIscDownRecord expiredVisitor = visitorRecord(3L, "old-card", "visitor-cert-2", "isc-person-2");
		SmtIscDeviceTask failedDeleteTask = new SmtIscDeviceTask();
		failedDeleteTask.setId(30L);
		failedDeleteTask.setStatus(DeviceTaskStatusEnum.FAIL.getCode());
		failedDeleteTask.setCode(500);
		failedDeleteTask.setRemark("old failure");
		failedDeleteTask.setIscTaskId("isc-task-old");
		Mockito.when(downRecordService.getById(3L)).thenReturn(expiredVisitor);
		Mockito.when(downRecordService.count(Mockito.any(LambdaQueryWrapper.class))).thenReturn(0);
		Mockito.when(deviceTaskService.list(Mockito.any(LambdaQueryWrapper.class)))
				.thenReturn(Collections.singletonList(failedDeleteTask));
		Mockito.when(deviceTaskService.updateById(Mockito.any(SmtIscDeviceTask.class))).thenReturn(Boolean.TRUE);

		IscAccessCleanupExecuteRespDTO result = service.execute(executeReq(3L), Collections.singletonList(5000021));

		Assert.assertEquals(Integer.valueOf(1), result.getUpdatedCount());
		Assert.assertEquals(Integer.valueOf(0), result.getCreatedCount());
		ArgumentCaptor<SmtIscDeviceTask> taskCaptor = ArgumentCaptor.forClass(SmtIscDeviceTask.class);
		Mockito.verify(deviceTaskService).updateById(taskCaptor.capture());
		SmtIscDeviceTask refreshedTask = taskCaptor.getValue();
		Assert.assertEquals(DeviceTaskStatusEnum.INIT.getCode(), refreshedTask.getStatus());
		Assert.assertNull(refreshedTask.getCode());
		Assert.assertNull(refreshedTask.getRemark());
		Assert.assertNull(refreshedTask.getIscTaskId());
		Mockito.verify(deviceTaskService, Mockito.never()).save(Mockito.any(SmtIscDeviceTask.class));
	}

	@Test
	public void executeDoesNotRefreshDeleteTaskAfterMaxRetryTimes() {
		SmtIscDownRecordService downRecordService = Mockito.mock(SmtIscDownRecordService.class);
		SmtIscDeviceTaskService deviceTaskService = Mockito.mock(SmtIscDeviceTaskService.class);
		SmtIscAccessCleanupService service = service(downRecordService, deviceTaskService);
		SmtIscDownRecord expiredVisitor = visitorRecord(6L, "old-card-6", "visitor-cert-6", "isc-person-6");
		SmtIscDeviceTask retryExceededTask = new SmtIscDeviceTask();
		retryExceededTask.setId(60L);
		retryExceededTask.setStatus(DeviceTaskStatusEnum.FAIL.getCode());
		retryExceededTask.setTimes(DeviceTaskConstants.AUTH_CONFIG_MAX_RETRY_TIMES);
		retryExceededTask.setCode(ISCDeviceTaskEnum.AUTH_CONFIG_DOWN_FAIL.getCode());
		retryExceededTask.setRemark("请人工介入处理");
		retryExceededTask.setIscTaskId("isc-task-old-6");
		Mockito.when(downRecordService.getById(6L)).thenReturn(expiredVisitor);
		Mockito.when(downRecordService.count(Mockito.any(LambdaQueryWrapper.class))).thenReturn(0);
		Mockito.when(deviceTaskService.list(Mockito.any(LambdaQueryWrapper.class)))
				.thenReturn(Collections.singletonList(retryExceededTask));
		Mockito.when(deviceTaskService.save(Mockito.any(SmtIscDeviceTask.class))).thenReturn(Boolean.TRUE);

		IscAccessCleanupExecuteRespDTO result = service.execute(executeReq(6L), Collections.singletonList(5000021));

		Assert.assertEquals(Integer.valueOf(0), result.getUpdatedCount());
		Assert.assertEquals(Integer.valueOf(1), result.getCreatedCount());
		Mockito.verify(deviceTaskService, Mockito.never()).updateById(Mockito.eq(retryExceededTask));
		ArgumentCaptor<SmtIscDeviceTask> taskCaptor = ArgumentCaptor.forClass(SmtIscDeviceTask.class);
		Mockito.verify(deviceTaskService).save(taskCaptor.capture());
		Assert.assertEquals(DeviceTaskStatusEnum.INIT.getCode(), taskCaptor.getValue().getStatus());
		Assert.assertEquals("请人工介入处理", retryExceededTask.getRemark());
		Assert.assertEquals(DeviceTaskConstants.AUTH_CONFIG_MAX_RETRY_TIMES, retryExceededTask.getTimes());
	}

	private SmtIscAccessCleanupService service(SmtIscDownRecordService downRecordService,
											   SmtIscDeviceTaskService deviceTaskService) {
		SmtStaffMapper staffMapper = Mockito.mock(SmtStaffMapper.class);
		SmtStaff resignedStaff = new SmtStaff();
		resignedStaff.setId(1001L);
		resignedStaff.setStatus(StaffStatusEnum.STAFF_STATUS_QUIT.getCode());
		Mockito.when(staffMapper.selectById(1001L)).thenReturn(resignedStaff);
		return service(downRecordService, deviceTaskService, staffMapper);
	}

	private SmtIscAccessCleanupService service(SmtIscDownRecordService downRecordService,
											   SmtIscDeviceTaskService deviceTaskService,
											   SmtStaffMapper staffMapper) {
		return new SmtIscAccessCleanupServiceImpl(Mockito.mock(SmtIscAccessCleanupMapper.class),
				downRecordService, deviceTaskService, staffMapper);
	}

	private IscAccessCleanupExecuteReqDTO executeReq(Long... recordIds) {
		IscAccessCleanupExecuteReqDTO reqDTO = new IscAccessCleanupExecuteReqDTO();
		reqDTO.setDownRecordIds(Arrays.asList(recordIds));
		return reqDTO;
	}

	private SmtIscDownRecord visitorRecord(Long id, String cardNo, String badge, String personId) {
		SmtIscDownRecord record = new SmtIscDownRecord();
		record.setId(id);
		record.setParkId(5000021);
		record.setCardNo(cardNo);
		record.setBadge(badge);
		record.setPersonId(personId);
		record.setGeneral("访客张三");
		record.setDeviceCode("device-1");
		record.setDeviceType(DeviceTaskConstants.CARD);
		record.setServiceType(DeviceTaskConstants.CARD_VISITOR);
		record.setStartTime(new Date(1735660800000L));
		record.setOverTime(new Date(1736438400000L));
		return record;
	}

	private SmtIscDownRecord staffRecord(Long id, String cardNo) {
		SmtIscDownRecord record = new SmtIscDownRecord();
		record.setId(id);
		record.setParkId(5000021);
		record.setCardNo(cardNo);
		record.setGeneral("JA26086-张三");
		record.setDeviceCode("device-1");
		record.setDeviceType(DeviceTaskConstants.CARD);
		record.setServiceType(DeviceTaskConstants.CARD_STAFF_IMPORT);
		record.setStartTime(new Date(1735660800000L));
		record.setOverTime(new Date(1893427199000L));
		return record;
	}
}
