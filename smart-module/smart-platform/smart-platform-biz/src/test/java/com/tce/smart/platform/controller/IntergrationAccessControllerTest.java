package com.tce.smart.platform.controller;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.platform.api.dto.BridgeListenerDTO;
import com.tce.smart.platform.core.dto.SaveSnapPersonDTO;
import com.tce.smart.platform.core.entity.SmtDeviceTask;
import com.tce.smart.platform.core.entity.SmtIscDownRecord;
import com.tce.smart.platform.core.entity.admittance.SmtAdmittanceApply;
import com.tce.smart.platform.core.entity.admittance.SmtAdmittanceFellow;
import com.tce.smart.platform.core.entity.admittance.SmtAdmittanceVehicle;
import com.tce.smart.platform.core.service.SmtDeviceTaskService;
import com.tce.smart.platform.core.service.SmtIscDownRecordService;
import com.tce.smart.platform.service.SmtSnapPersonService;
import com.tce.smart.platform.service.SmtFellowVisitorService;
import com.tce.smart.platform.service.SmtStaffService;
import com.tce.smart.platform.service.SmtVisitorService;
import com.tce.smart.platform.service.admittance.SmtAdmittanceApplyService;
import com.tce.smart.platform.service.admittance.SmtAdmittanceFellowService;
import com.tce.smart.platform.service.admittance.SmtAdmittanceVehicleService;
import com.tce.smart.tool.constant.DeviceTaskConstants;
import com.tce.smart.tool.enums.DeviceTaskActionEnum;
import com.tce.smart.tool.enums.DeviceTaskStatusEnum;
import com.tce.smart.tool.enums.DeviceTypeEnum;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import com.tce.smart.platform.core.dto.authtransport.AuthDirectTakeover.*;
import com.tce.smart.platform.core.dto.authworkflow.AuthWorkflow.Received;
import com.tce.smart.platform.core.dto.authoperation.AuthOperationReceiptResult;
import com.tce.smart.platform.core.dto.authversion.AuthVersion.EvidenceResult;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collections;

public class IntergrationAccessControllerTest {

	@BeforeClass
	public static void initMybatisPlusLambdaCache() {
		TableInfoHelper.initTableInfo(new MapperBuilderAssistant(new MybatisConfiguration(), ""), SmtDeviceTask.class);
		TableInfoHelper.initTableInfo(new MapperBuilderAssistant(new MybatisConfiguration(), ""), SmtIscDownRecord.class);
	}

 @Test public void boundCallbackUsesPersistentVersionCoordinatesAndSkipsLegacyCompletion() throws Exception {
  IntergrationAccessController controller=new IntergrationAccessController();
  com.tce.smart.platform.core.service.SmtDeviceTaskService tasks=Mockito.mock(com.tce.smart.platform.core.service.SmtDeviceTaskService.class);
  com.tce.smart.platform.core.service.impl.DirectTaskCompletionService legacy=Mockito.mock(com.tce.smart.platform.core.service.impl.DirectTaskCompletionService.class);
  com.tce.smart.platform.core.service.impl.AuthOperationTransportGuard guard=Mockito.mock(com.tce.smart.platform.core.service.impl.AuthOperationTransportGuard.class);
  com.tce.smart.platform.core.service.impl.AuthOperationTransportService transport=Mockito.mock(com.tce.smart.platform.core.service.impl.AuthOperationTransportService.class);
  setField(controller,"smtDeviceTaskService",tasks);setField(controller,"directTaskCompletionService",legacy);controller.setAuthTransport(guard,transport);
  com.tce.smart.platform.core.entity.SmtDeviceTask task=new com.tce.smart.platform.core.entity.SmtDeviceTask();task.setId(42);task.setDeviceType(1);task.setSerialNo("command-v2");Mockito.when(tasks.getOne(Mockito.any())).thenReturn(task);Mockito.when(guard.bound("DIRECT","42")).thenReturn(true);
  com.tce.smart.platform.core.entity.SmtAuthTransportPhase phase=new com.tce.smart.platform.core.entity.SmtAuthTransportPhase();phase.setId(99L);phase.setParkId(1);phase.setInstanceId("instance");phase.setDeviceId("d");Mockito.when(transport.byTask("DIRECT","42")).thenReturn(java.util.Collections.singletonList(phase));
  phase.setTargetId(10L);phase.setAttemptId(11L);
  Mockito.when(guard.admitLegacyReceipt(Mockito.eq(42),Mockito.anyString(),Mockito.anyInt(),Mockito.anyString())).thenReturn(Decision.builder().outcome(Outcome.OWNED_BY_TRANSPORT).phase(phase).build());
  Mockito.when(transport.receipt(Mockito.anyInt(),Mockito.anyString(),Mockito.anyLong(),Mockito.isNull(),Mockito.anyString(),Mockito.anyString(),Mockito.anyString(),Mockito.anyBoolean(),Mockito.anyString()))
      .thenReturn(Received.builder().receipt(AuthOperationReceiptResult.builder().targetId(10L).attemptId(11L).eventId(12L).build()).evidence(EvidenceResult.builder().outcome("STALE_REPLAY").build()).sourceConverged(false).build());
  Assert.assertTrue(controller.doReplyOfCard("{\"code\":200,\"data\":{\"serialNo\":\"command-v2\"}}").isSuccess());
  Mockito.verify(transport).receipt(1,"instance",99L,null,"d","command-v2","command-v2:200",true,"code=200");Mockito.verifyZeroInteractions(legacy);
 }
    @Test public void protectedRawAndRejectedOwnedReceiptsNeverReachLegacyCompletion() throws Exception {
        for(String outcome:new String[]{"RAW","UNTRUSTED","VERIFYING","FUTURE","MISSING"}) {
            IntergrationAccessController controller=new IntergrationAccessController();
            SmtDeviceTaskService tasks=Mockito.mock(SmtDeviceTaskService.class);
            com.tce.smart.platform.core.service.impl.DirectTaskCompletionService completion=Mockito.mock(com.tce.smart.platform.core.service.impl.DirectTaskCompletionService.class);
            com.tce.smart.platform.core.service.impl.AuthOperationTransportGuard guard=Mockito.mock(com.tce.smart.platform.core.service.impl.AuthOperationTransportGuard.class);
            com.tce.smart.platform.core.service.impl.AuthOperationTransportService transport=Mockito.mock(com.tce.smart.platform.core.service.impl.AuthOperationTransportService.class);
            controller.setAuthTransport(guard,transport);setField(controller,"smtDeviceTaskService",tasks);setField(controller,"directTaskCompletionService",completion);
            SmtDeviceTask task=new SmtDeviceTask();task.setId(7);task.setDeviceType(1);task.setSerialNo("s");Mockito.when(tasks.getOne(Mockito.any())).thenReturn(task);
            com.tce.smart.platform.core.entity.SmtAuthTransportPhase p=new com.tce.smart.platform.core.entity.SmtAuthTransportPhase();p.setId(8L);p.setParkId(10);p.setInstanceId("owner");p.setDeviceId("d");p.setTargetId(11L);p.setAttemptId(12L);
            Mockito.when(guard.admitLegacyReceipt(Mockito.any(),Mockito.anyString(),Mockito.anyInt(),Mockito.anyString())).thenReturn(Decision.builder().outcome("RAW".equals(outcome)?Outcome.VERIFYING:Outcome.OWNED_BY_TRANSPORT).reason("review").phase(p).build());
            Mockito.when(transport.receipt(Mockito.anyInt(),Mockito.anyString(),Mockito.anyLong(),Mockito.isNull(),Mockito.anyString(),Mockito.anyString(),Mockito.anyString(),Mockito.anyBoolean(),Mockito.anyString()))
                .thenReturn("MISSING".equals(outcome)?null:Received.builder().receipt(AuthOperationReceiptResult.builder().targetId(11L).attemptId(12L).eventId(13L).build()).evidence(EvidenceResult.builder().outcome(outcome).build()).build());
            Assert.assertFalse(outcome,controller.doReplyOfCard("{\"code\":200,\"data\":{\"serialNo\":\"s\"}}").isSuccess());Mockito.verifyZeroInteractions(completion);
        }
    }
	@Test
	public void replyOfAccessMapsIscPersonIdToTemporaryVisitorCardNoWhenCardNoMissing() throws Exception {
		SmtSnapPersonService snapPersonService = Mockito.mock(SmtSnapPersonService.class);
		SmtStaffService staffService = Mockito.mock(SmtStaffService.class);
		SmtIscDownRecordService downRecordService = Mockito.mock(SmtIscDownRecordService.class);
		IntergrationAccessController controller = new IntergrationAccessController();
		setField(controller, "smtSnapPersonService", snapPersonService);
		setField(controller, "smtStaffService", staffService);
		setField(controller, "smtIscDownRecordService", downRecordService);
		SmtIscDownRecord downRecord = new SmtIscDownRecord();
		downRecord.setCardNo("1001");
		downRecord.setPersonId("isc-person-1");
		downRecord.setDeviceCode("device-1");
		downRecord.setDeviceType(DeviceTaskConstants.CARD);
		downRecord.setServiceType(DeviceTaskConstants.CARD_VISITOR);
		Mockito.when(downRecordService.getOne(Mockito.any())).thenReturn(downRecord);
		Mockito.when(snapPersonService.addSnapPerson(Mockito.any(SaveSnapPersonDTO.class))).thenReturn(Result.success(true));
		BridgeListenerDTO listenerDTO = new BridgeListenerDTO();
		listenerDTO.setContent("{\"isISC\":true,\"personId\":\"isc-person-1\",\"deviceCode\":\"device-1\","
				+ "\"openMethod\":1,\"letPass\":1,\"eventTime\":1780360200}");

		controller.replyOfAccess(listenerDTO);

		ArgumentCaptor<SaveSnapPersonDTO> captor = ArgumentCaptor.forClass(SaveSnapPersonDTO.class);
		Mockito.verify(snapPersonService).addSnapPerson(captor.capture());
		Assert.assertEquals("1001", captor.getValue().getCardNo());
		ArgumentCaptor<LambdaQueryWrapper> queryCaptor = ArgumentCaptor.forClass(LambdaQueryWrapper.class);
		Mockito.verify(downRecordService).getOne(queryCaptor.capture());
		assertQueryHasParam(queryCaptor.getValue(), "isc-person-1");
		assertQueryHasParam(queryCaptor.getValue(), "device-1");
		assertQueryHasParam(queryCaptor.getValue(), DeviceTaskConstants.CARD_VISITOR);
		Mockito.verify(staffService, Mockito.never()).getOne(Mockito.any(), Mockito.anyBoolean());
	}

	@Test
	public void replyOfAccessMapsIscPersonIdToTemporaryVisitorCardNoWhenCardNoIsVirtual() throws Exception {
		SmtSnapPersonService snapPersonService = Mockito.mock(SmtSnapPersonService.class);
		SmtStaffService staffService = Mockito.mock(SmtStaffService.class);
		SmtIscDownRecordService downRecordService = Mockito.mock(SmtIscDownRecordService.class);
		IntergrationAccessController controller = new IntergrationAccessController();
		setField(controller, "smtSnapPersonService", snapPersonService);
		setField(controller, "smtStaffService", staffService);
		setField(controller, "smtIscDownRecordService", downRecordService);
		SmtIscDownRecord downRecord = new SmtIscDownRecord();
		downRecord.setCardNo("1002");
		downRecord.setPersonId("isc-person-2");
		downRecord.setDeviceCode("device-2");
		downRecord.setDeviceType(DeviceTaskConstants.CARD);
		downRecord.setServiceType(DeviceTaskConstants.CARD_VISITOR);
		Mockito.when(downRecordService.getOne(Mockito.any())).thenReturn(downRecord);
		Mockito.when(snapPersonService.addSnapPerson(Mockito.any(SaveSnapPersonDTO.class))).thenReturn(Result.success(true));
		BridgeListenerDTO listenerDTO = new BridgeListenerDTO();
		listenerDTO.setContent("{\"isISC\":true,\"personId\":\"isc-person-2\",\"cardNo\":\"9990000001\","
				+ "\"deviceCode\":\"device-2\",\"openMethod\":1,\"letPass\":1,\"eventTime\":1780360200}");

		controller.replyOfAccess(listenerDTO);

		ArgumentCaptor<SaveSnapPersonDTO> captor = ArgumentCaptor.forClass(SaveSnapPersonDTO.class);
		Mockito.verify(snapPersonService).addSnapPerson(captor.capture());
		Assert.assertEquals("1002", captor.getValue().getCardNo());
		Mockito.verify(downRecordService).getOne(Mockito.any());
		Mockito.verify(staffService, Mockito.never()).getOne(Mockito.any(), Mockito.anyBoolean());
	}

	@Test
	public void replyOfAccessClearsVirtualIscCardNoWhenTemporaryVisitorRecordMissing() throws Exception {
		SmtSnapPersonService snapPersonService = Mockito.mock(SmtSnapPersonService.class);
		SmtStaffService staffService = Mockito.mock(SmtStaffService.class);
		SmtIscDownRecordService downRecordService = Mockito.mock(SmtIscDownRecordService.class);
		IntergrationAccessController controller = new IntergrationAccessController();
		setField(controller, "smtSnapPersonService", snapPersonService);
		setField(controller, "smtStaffService", staffService);
		setField(controller, "smtIscDownRecordService", downRecordService);
		Mockito.when(downRecordService.getOne(Mockito.any())).thenReturn(null);
		Mockito.when(snapPersonService.addSnapPerson(Mockito.any(SaveSnapPersonDTO.class))).thenReturn(Result.success(true));
		BridgeListenerDTO listenerDTO = new BridgeListenerDTO();
		listenerDTO.setContent("{\"isISC\":true,\"personId\":\"isc-person-3\",\"cardNo\":\"9990000002\","
				+ "\"deviceCode\":\"device-3\",\"openMethod\":1,\"letPass\":1,\"eventTime\":1780360200}");

		controller.replyOfAccess(listenerDTO);

		ArgumentCaptor<SaveSnapPersonDTO> captor = ArgumentCaptor.forClass(SaveSnapPersonDTO.class);
		Mockito.verify(snapPersonService).addSnapPerson(captor.capture());
		Assert.assertNull(captor.getValue().getCardNo());
		Mockito.verify(downRecordService).getOne(Mockito.any());
		Mockito.verify(staffService, Mockito.never()).getOne(Mockito.any(), Mockito.anyBoolean());
	}

	@Test
	public void handelVisitorCreatesAdmittanceDeleteTaskWithExactEndTime() throws Exception {
		SmtDeviceTaskService taskService = Mockito.mock(SmtDeviceTaskService.class);
		Mockito.when(taskService.save(Mockito.any(SmtDeviceTask.class))).thenReturn(true);
		SmtVisitorService visitorService = Mockito.mock(SmtVisitorService.class);
		SmtFellowVisitorService fellowVisitorService = Mockito.mock(SmtFellowVisitorService.class);
		SmtAdmittanceFellowService admittanceFellowService = Mockito.mock(SmtAdmittanceFellowService.class);
		SmtAdmittanceApplyService admittanceApplyService = Mockito.mock(SmtAdmittanceApplyService.class);
		IntergrationAccessController controller = new IntergrationAccessController();
		setField(controller, "smtDeviceTaskService", taskService);
		setField(controller, "smtVisitorService", visitorService);
		setField(controller, "smtFellowVisitorService", fellowVisitorService);
		setField(controller, "smtAdmittanceFellowService", admittanceFellowService);
		setField(controller, "smtAdmittanceApplyService", admittanceApplyService);
		LocalDateTime endTime = LocalDateTime.of(2026, 6, 2, 18, 0, 0);
		SmtAdmittanceFellow fellow = new SmtAdmittanceFellow();
		fellow.setId(3001L);
		fellow.setVisitorId(2001L);
		SmtAdmittanceApply apply = new SmtAdmittanceApply();
		apply.setId(2001L);
		apply.setEndTime(endTime);
		SmtDeviceTask callbackTask = new SmtDeviceTask();
		callbackTask.setDeviceType(DeviceTypeEnum.DEVICE_TYPE_1.getCode());
		callbackTask.setServiceType(DeviceTaskConstants.CARD_ADMITTANCE);
		callbackTask.setAction(DeviceTaskActionEnum.DOWN.getCode());
		callbackTask.setCardNo(fellow.getId().toString());
		callbackTask.setDeviceCode("device-1");
		Mockito.when(admittanceFellowService.getById(fellow.getId())).thenReturn(fellow);
		Mockito.when(admittanceApplyService.getById(fellow.getVisitorId())).thenReturn(apply);
		Mockito.when(taskService.count(Mockito.any())).thenReturn(0);

		controller.handelVisitor(callbackTask);

		ArgumentCaptor<SmtDeviceTask> captor = ArgumentCaptor.forClass(SmtDeviceTask.class);
		Mockito.verify(taskService).save(captor.capture());
		SmtDeviceTask deleteTask = captor.getValue();
		Assert.assertEquals(DeviceTaskActionEnum.DEL.getCode(), deleteTask.getAction());
		Assert.assertEquals(DeviceTaskStatusEnum.INIT.getCode(), deleteTask.getStatus());
		Assert.assertEquals(DeviceTaskConstants.CARD_ADMITTANCE, deleteTask.getServiceType());
		Assert.assertEquals(Long.valueOf(endTime.atZone(ZoneId.systemDefault()).toEpochSecond()), deleteTask.getOverTime());
		ArgumentCaptor<LambdaQueryWrapper> queryCaptor = ArgumentCaptor.forClass(LambdaQueryWrapper.class);
		Mockito.verify(taskService).list(queryCaptor.capture());
		assertQueryHasParam(queryCaptor.getValue(), DeviceTypeEnum.DEVICE_TYPE_1.getCode());
		assertQueryHasParam(queryCaptor.getValue(), DeviceTaskConstants.CARD_ADMITTANCE);
	}

	@Test
	public void handelVisitorIgnoresHistoricalAdmittanceDeleteTask() throws Exception {
		SmtDeviceTaskService taskService = Mockito.mock(SmtDeviceTaskService.class);
		Mockito.when(taskService.save(Mockito.any(SmtDeviceTask.class))).thenReturn(true);
		SmtVisitorService visitorService = Mockito.mock(SmtVisitorService.class);
		SmtFellowVisitorService fellowVisitorService = Mockito.mock(SmtFellowVisitorService.class);
		SmtAdmittanceFellowService admittanceFellowService = Mockito.mock(SmtAdmittanceFellowService.class);
		SmtAdmittanceApplyService admittanceApplyService = Mockito.mock(SmtAdmittanceApplyService.class);
		IntergrationAccessController controller = new IntergrationAccessController();
		setField(controller, "smtDeviceTaskService", taskService);
		setField(controller, "smtVisitorService", visitorService);
		setField(controller, "smtFellowVisitorService", fellowVisitorService);
		setField(controller, "smtAdmittanceFellowService", admittanceFellowService);
		setField(controller, "smtAdmittanceApplyService", admittanceApplyService);
		LocalDateTime endTime = LocalDateTime.of(2026, 6, 2, 18, 0, 0);
		SmtAdmittanceFellow fellow = new SmtAdmittanceFellow();
		fellow.setId(3001L);
		fellow.setVisitorId(2001L);
		SmtAdmittanceApply apply = new SmtAdmittanceApply();
		apply.setId(2001L);
		apply.setEndTime(endTime);
		SmtDeviceTask callbackTask = new SmtDeviceTask();
		callbackTask.setDeviceType(DeviceTypeEnum.DEVICE_TYPE_1.getCode());
		callbackTask.setServiceType(DeviceTaskConstants.CARD_ADMITTANCE);
		callbackTask.setAction(DeviceTaskActionEnum.DOWN.getCode());
		callbackTask.setCardNo(fellow.getId().toString());
		callbackTask.setDeviceCode("device-1");
		SmtDeviceTask historicalDeleteTask = new SmtDeviceTask();
		historicalDeleteTask.setStatus(DeviceTaskStatusEnum.SUCCESS.getCode());
		Mockito.when(admittanceFellowService.getById(fellow.getId())).thenReturn(fellow);
		Mockito.when(admittanceApplyService.getById(fellow.getVisitorId())).thenReturn(apply);
		Mockito.when(taskService.list(Mockito.any())).thenReturn(Collections.singletonList(historicalDeleteTask));

		controller.handelVisitor(callbackTask);

		Mockito.verify(taskService, Mockito.never()).updateById(Mockito.eq(historicalDeleteTask));
		ArgumentCaptor<SmtDeviceTask> captor = ArgumentCaptor.forClass(SmtDeviceTask.class);
		Mockito.verify(taskService).save(captor.capture());
		Assert.assertEquals(DeviceTaskStatusEnum.INIT.getCode(), captor.getValue().getStatus());
		Assert.assertEquals(DeviceTaskConstants.CARD_ADMITTANCE, captor.getValue().getServiceType());
	}

	@Test
	public void handelVisitorCreatesCarAdmittanceDeleteTaskWithExactEndTime() throws Exception {
		SmtDeviceTaskService taskService = Mockito.mock(SmtDeviceTaskService.class);
		Mockito.when(taskService.save(Mockito.any(SmtDeviceTask.class))).thenReturn(true);
		SmtVisitorService visitorService = Mockito.mock(SmtVisitorService.class);
		SmtFellowVisitorService fellowVisitorService = Mockito.mock(SmtFellowVisitorService.class);
		SmtAdmittanceFellowService admittanceFellowService = Mockito.mock(SmtAdmittanceFellowService.class);
		SmtAdmittanceApplyService admittanceApplyService = Mockito.mock(SmtAdmittanceApplyService.class);
		SmtAdmittanceVehicleService admittanceVehicleService = Mockito.mock(SmtAdmittanceVehicleService.class);
		IntergrationAccessController controller = new IntergrationAccessController();
		setField(controller, "smtDeviceTaskService", taskService);
		setField(controller, "smtVisitorService", visitorService);
		setField(controller, "smtFellowVisitorService", fellowVisitorService);
		setField(controller, "smtAdmittanceFellowService", admittanceFellowService);
		setField(controller, "smtAdmittanceApplyService", admittanceApplyService);
		setField(controller, "smtAdmittanceVehicleService", admittanceVehicleService);
		LocalDateTime endTime = LocalDateTime.of(2026, 6, 2, 18, 0, 0);
		SmtAdmittanceVehicle vehicle = new SmtAdmittanceVehicle();
		vehicle.setId(4001L);
		vehicle.setVisitorId(2001L);
		SmtAdmittanceApply apply = new SmtAdmittanceApply();
		apply.setId(2001L);
		apply.setEndTime(endTime);
		SmtDeviceTask callbackTask = new SmtDeviceTask();
		callbackTask.setDeviceType(DeviceTaskConstants.CAR);
		callbackTask.setServiceType(DeviceTaskConstants.CAT_ADMITTANCE);
		callbackTask.setAction(DeviceTaskActionEnum.DOWN.getCode());
		callbackTask.setCardNo(vehicle.getId().toString());
		callbackTask.setDeviceCode("car-device-1");
		Mockito.when(admittanceVehicleService.getById(vehicle.getId())).thenReturn(vehicle);
		Mockito.when(admittanceApplyService.getById(vehicle.getVisitorId())).thenReturn(apply);
		Mockito.when(taskService.list(Mockito.any())).thenReturn(Collections.emptyList());

		controller.handelVisitor(callbackTask);

		ArgumentCaptor<LambdaQueryWrapper> queryCaptor = ArgumentCaptor.forClass(LambdaQueryWrapper.class);
		Mockito.verify(taskService).list(queryCaptor.capture());
		assertQueryHasParam(queryCaptor.getValue(), DeviceTaskConstants.CAR);
		assertQueryHasParam(queryCaptor.getValue(), DeviceTaskConstants.CAT_ADMITTANCE);
		ArgumentCaptor<SmtDeviceTask> captor = ArgumentCaptor.forClass(SmtDeviceTask.class);
		Mockito.verify(taskService).save(captor.capture());
		SmtDeviceTask deleteTask = captor.getValue();
		Assert.assertEquals(DeviceTaskActionEnum.DEL.getCode(), deleteTask.getAction());
		Assert.assertEquals(DeviceTaskConstants.CAR, deleteTask.getDeviceType());
		Assert.assertEquals(DeviceTaskConstants.CAT_ADMITTANCE, deleteTask.getServiceType());
		Assert.assertEquals(Long.valueOf(endTime.atZone(ZoneId.systemDefault()).toEpochSecond()), deleteTask.getOverTime());
	}

	@Test(expected = IllegalStateException.class)
	public void generatedDeleteSaveFailureMustPropagate() throws Exception {
		SmtDeviceTaskService tasks = Mockito.mock(SmtDeviceTaskService.class);
		IntergrationAccessController controller = new IntergrationAccessController();
		setField(controller, "smtDeviceTaskService", tasks);
		SmtDeviceTask task = new SmtDeviceTask();
		task.setDeviceType(DeviceTaskConstants.CARD); task.setServiceType(DeviceTaskConstants.CARD_VISITOR);
		controller.genDeviceTask(task, new java.util.Date(1790000000000L));
	}

	@Test(expected = IllegalStateException.class)
	public void generatedDeleteUpdateFailureMustPropagate() throws Exception {
		SmtDeviceTaskService tasks = Mockito.mock(SmtDeviceTaskService.class);
		IntergrationAccessController controller = new IntergrationAccessController();
		setField(controller, "smtDeviceTaskService", tasks);
		SmtDeviceTask task = new SmtDeviceTask();
		task.setDeviceType(DeviceTaskConstants.CARD); task.setServiceType(DeviceTaskConstants.CARD_VISITOR);
		SmtDeviceTask pending = new SmtDeviceTask(); pending.setId(21); pending.setStatus(0);
		Mockito.when(tasks.list(Mockito.any())).thenReturn(Collections.singletonList(pending));
		controller.genDeviceTask(task, new java.util.Date(1790000000000L));
	}

	@Test public void unknownSerialIsNotAcknowledgedAsSuccessfulCompletion() throws Exception {
		IntergrationAccessController controller = new IntergrationAccessController();
		setField(controller, "smtDeviceTaskService", Mockito.mock(SmtDeviceTaskService.class));
		Assert.assertFalse(controller.doReplyOfCard("{\"code\":200,\"data\":{\"serialNo\":\"unknown\"}}").isSuccess());
	}

	@Test public void allCardReplyRoutesCompleteInitialTaskThroughCompletionBean() throws Exception {
		for (int route = 0; route < 3; route++) {
			SmtDeviceTaskService tasks = Mockito.mock(SmtDeviceTaskService.class);
			com.tce.smart.platform.core.service.SmtTaskDownRecordService records =
					Mockito.mock(com.tce.smart.platform.core.service.SmtTaskDownRecordService.class);
			IntergrationAccessController controller = new IntergrationAccessController();
			setField(controller, "smtDeviceTaskService", tasks);
            com.tce.smart.platform.core.service.impl.AuthOperationDirectTakeoverService gate=Mockito.mock(com.tce.smart.platform.core.service.impl.AuthOperationDirectTakeoverService.class);
            Mockito.when(gate.admitLegacyDirect(Mockito.any(),Mockito.any())).thenReturn(Decision.builder().outcome(Outcome.LEGACY_ALLOWED).build());
            com.tce.smart.platform.core.service.impl.DirectTaskCompletionService completion=new com.tce.smart.platform.core.service.impl.DirectTaskCompletionService(tasks,records);completion.setDirectTakeover(gate);
            setField(controller,"directTaskCompletionService",completion);
            com.tce.smart.platform.core.service.impl.AuthOperationTransportGuard guard=Mockito.mock(com.tce.smart.platform.core.service.impl.AuthOperationTransportGuard.class);
            Mockito.when(guard.admitLegacyReceipt(Mockito.any(),Mockito.anyString(),Mockito.anyInt(),Mockito.anyString())).thenReturn(Decision.builder().outcome(Outcome.LEGACY_ALLOWED).build());controller.setAuthTransport(guard,null);
			SmtDeviceTask task = new SmtDeviceTask();
			task.setId(17); task.setSerialNo("route-command"); task.setStatus(0); task.setAction(1);
			task.setDeviceType(DeviceTaskConstants.CARD); task.setServiceType(DeviceTaskConstants.CARD_STAFF_IMPORT);
			Mockito.when(tasks.getOne(Mockito.any())).thenReturn(task);
			Mockito.when(tasks.update(Mockito.any(com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper.class))).thenReturn(true);
			BridgeListenerDTO reply = new BridgeListenerDTO();
			reply.setContent("{\"code\":200,\"data\":{\"serialNo\":\"route-command\"}}");
			Result<Boolean> result = route == 0 ? controller.replyOfAddCard(reply)
					: route == 1 ? controller.replyOfDeleteCard(reply) : controller.replyOfUpdateCard(reply);
			Assert.assertTrue(result.isSuccess());
			Mockito.verify(records).handleTaskDownRecord(Mockito.argThat(done -> done.getStatus().equals(1)));
			Mockito.verify(tasks, Mockito.never()).updateById(Mockito.any(SmtDeviceTask.class));
		}
	}

	@Test public void lateCardFailureDoesNotOverwriteTerminalTask() throws Exception {
		for (int terminal : new int[]{1,4}) {
			SmtDeviceTaskService tasks = Mockito.mock(SmtDeviceTaskService.class);
			com.tce.smart.platform.core.service.SmtTaskDownRecordService records =
					Mockito.mock(com.tce.smart.platform.core.service.SmtTaskDownRecordService.class);
			IntergrationAccessController controller = new IntergrationAccessController();
			setField(controller, "smtDeviceTaskService", tasks);
            com.tce.smart.platform.core.service.impl.AuthOperationDirectTakeoverService gate=Mockito.mock(com.tce.smart.platform.core.service.impl.AuthOperationDirectTakeoverService.class);
            Mockito.when(gate.admitLegacyDirect(Mockito.any(),Mockito.any())).thenReturn(Decision.builder().outcome(Outcome.LEGACY_ALLOWED).build());
            com.tce.smart.platform.core.service.impl.DirectTaskCompletionService completion=new com.tce.smart.platform.core.service.impl.DirectTaskCompletionService(tasks,records);completion.setDirectTakeover(gate);
            setField(controller,"directTaskCompletionService",completion);
            com.tce.smart.platform.core.service.impl.AuthOperationTransportGuard guard=Mockito.mock(com.tce.smart.platform.core.service.impl.AuthOperationTransportGuard.class);
            Mockito.when(guard.admitLegacyReceipt(Mockito.any(),Mockito.anyString(),Mockito.anyInt(),Mockito.anyString())).thenReturn(Decision.builder().outcome(Outcome.LEGACY_ALLOWED).build());controller.setAuthTransport(guard,null);
			SmtDeviceTask task = new SmtDeviceTask(); task.setId(17); task.setSerialNo("finished-command");
			task.setStatus(terminal); task.setAction(1);
			Mockito.when(tasks.getOne(Mockito.any())).thenReturn(task);
			Assert.assertTrue(controller.doReplyOfCard("{\"code\":402,\"data\":{\"serialNo\":\"finished-command\"}}").isSuccess());
			Assert.assertEquals(Integer.valueOf(terminal), task.getStatus());
			Mockito.verify(tasks, Mockito.never()).updateById(Mockito.any(SmtDeviceTask.class));
			Mockito.verify(tasks, Mockito.never()).update(Mockito.any(com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper.class));
			Mockito.verifyZeroInteractions(records);
		}
	}

	@Test(expected = IllegalStateException.class)
	public void changedReusableDeleteCannotBeRevivedFromStaleSnapshot() throws Exception {
		SmtDeviceTaskService tasks = Mockito.mock(SmtDeviceTaskService.class);
		IntergrationAccessController controller = new IntergrationAccessController();
		setField(controller, "smtDeviceTaskService", tasks);
		SmtDeviceTask original = new SmtDeviceTask();
		original.setDeviceType(DeviceTaskConstants.CARD); original.setServiceType(DeviceTaskConstants.CARD_VISITOR);
		SmtDeviceTask pending = new SmtDeviceTask();
		pending.setId(21); pending.setStatus(0); pending.setAction(2); pending.setSerialNo("old-delete-command");
		Mockito.when(tasks.list(Mockito.any())).thenReturn(Collections.singletonList(pending));
		// 普通主键更新仍可命中，但并发完成/取消后旧状态条件已不命中，必须中断整个回执事务。
		Mockito.when(tasks.updateById(Mockito.any(SmtDeviceTask.class))).thenReturn(true);
		Mockito.when(tasks.update(Mockito.any(com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper.class))).thenReturn(false);
		controller.genDeviceTask(original, new java.util.Date(1790000000000L));
	}

	@Test public void reusableDeleteUpdateBindsOriginalStatusAndSerial() throws Exception {
		SmtDeviceTaskService tasks = Mockito.mock(SmtDeviceTaskService.class);
		IntergrationAccessController controller = new IntergrationAccessController();
		setField(controller, "smtDeviceTaskService", tasks);
		SmtDeviceTask original = new SmtDeviceTask();
		original.setDeviceType(DeviceTaskConstants.CARD); original.setServiceType(DeviceTaskConstants.CARD_VISITOR);
		SmtDeviceTask pending = new SmtDeviceTask();
		pending.setId(21); pending.setStatus(2); pending.setAction(2); pending.setSerialNo("current-delete-command");
		Mockito.when(tasks.list(Mockito.any())).thenReturn(Collections.singletonList(pending));
		Mockito.when(tasks.updateById(Mockito.any(SmtDeviceTask.class))).thenReturn(true);
		Mockito.when(tasks.update(Mockito.any(com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper.class))).thenReturn(true);
		controller.genDeviceTask(original, new java.util.Date(1790000000000L));
		ArgumentCaptor<com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper> update =
				ArgumentCaptor.forClass(com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper.class);
		Mockito.verify(tasks).update(update.capture());
		String where = update.getValue().getSqlSegment().toUpperCase();
		Assert.assertTrue(where.contains("STATUS") && where.contains("SERIAL_NO") && where.contains("ACTION"));
		Assert.assertTrue(update.getValue().getParamNameValuePairs().containsValue("current-delete-command"));
		Mockito.verify(tasks, Mockito.never()).updateById(Mockito.any(SmtDeviceTask.class));
	}

	private void assertQueryHasParam(LambdaQueryWrapper queryWrapper, Object expected) {
		queryWrapper.getSqlSegment();
		Assert.assertTrue(queryWrapper.getParamNameValuePairs().values().stream()
				.anyMatch(value -> queryParamMatches(value, expected)));
	}

	private boolean queryParamMatches(Object value, Object expected) {
		if (value instanceof Iterable) {
			for (Object item : (Iterable<?>) value) {
				if (String.valueOf(expected).equals(String.valueOf(item))) {
					return true;
				}
			}
			return false;
		}
		return String.valueOf(expected).equals(String.valueOf(value));
	}

	private void setField(Object target, String name, Object value) throws Exception {
		Field field = target.getClass().getDeclaredField(name);
		field.setAccessible(true);
		field.set(target, value);
	}
}
