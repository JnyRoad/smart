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
