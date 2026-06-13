package com.tce.smart.platform.service.admittance.impl;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tce.smart.common.core.constant.enums.SmtVisitorEnum;
import com.tce.smart.common.core.exception.SmartException;
import com.tce.smart.common.security.service.SmartUser;
import com.tce.smart.platform.core.dto.DeviceTaskVO;
import com.tce.smart.platform.core.dto.SearchSmtVisitorDTO;
import com.tce.smart.platform.core.entity.SmtDeviceAuthorityRelation;
import com.tce.smart.platform.core.entity.SmtDeviceTask;
import com.tce.smart.platform.core.entity.SmtIscDeviceTask;
import com.tce.smart.platform.core.entity.SmtIscDownRecord;
import com.tce.smart.platform.core.entity.SmtStaff;
import com.tce.smart.platform.core.entity.SmtTaskDownRecord;
import com.tce.smart.platform.core.entity.admittance.SmtAdmittanceAreaTypeAuth;
import com.tce.smart.platform.core.entity.admittance.SmtAdmittanceApply;
import com.tce.smart.platform.core.entity.admittance.SmtAdmittanceFellow;
import com.tce.smart.platform.core.entity.admittance.SmtAdmittanceVehicle;
import com.tce.smart.platform.core.enums.ISCDeviceTaskEnum;
import com.tce.smart.platform.core.mapper.SmtAdmittanceApplyMapper;
import com.tce.smart.platform.core.service.SmtDeviceTaskService;
import com.tce.smart.platform.core.service.SmtIscDeviceTaskService;
import com.tce.smart.platform.core.service.SmtIscDownRecordService;
import com.tce.smart.platform.core.service.SmtTaskDownRecordService;
import com.tce.smart.platform.core.vo.SearchSmtVisitorVO;
import com.tce.smart.platform.service.ImageService;
import com.tce.smart.platform.service.SmtStaffService;
import com.tce.smart.platform.service.SmtDeviceAuthorityRelationService;
import com.tce.smart.platform.api.dto.req.admittance.AdmittanceFellowReqDTO;
import com.tce.smart.platform.api.dto.req.admittance.SaveAdmittanceApplyReqDTO;
import com.tce.smart.platform.service.admittance.SmtAdmittanceAreaTypeAuthService;
import com.tce.smart.platform.service.admittance.SmtAdmittanceFellowService;
import com.tce.smart.platform.service.admittance.SmtAdmittanceVehicleService;
import com.tce.smart.tool.constant.DeviceTaskConstants;
import com.tce.smart.tool.enums.AdmittanceTypeEnum;
import com.tce.smart.tool.enums.DeviceTaskActionEnum;
import com.tce.smart.tool.enums.DeviceTaskStatusEnum;
import com.tce.smart.tool.enums.DeviceTypeEnum;
import com.tce.smart.tool.enums.OneOrZeroEnum;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collections;
import java.util.List;

@SuppressWarnings({"rawtypes", "unchecked"})
public class SmtAdmittanceApplyServiceImplTest {

	@BeforeClass
	public static void initMybatisPlusLambdaCache() {
		TableInfoHelper.initTableInfo(new MapperBuilderAssistant(new MybatisConfiguration(), ""), SmtTaskDownRecord.class);
		TableInfoHelper.initTableInfo(new MapperBuilderAssistant(new MybatisConfiguration(), ""), SmtIscDownRecord.class);
		TableInfoHelper.initTableInfo(new MapperBuilderAssistant(new MybatisConfiguration(), ""), SmtDeviceTask.class);
		TableInfoHelper.initTableInfo(new MapperBuilderAssistant(new MybatisConfiguration(), ""), SmtIscDeviceTask.class);
	}

	@Test
	public void addCardKeepsAdmittanceExactEndTimeAndServiceType() throws Exception {
		SmtDeviceTaskService taskService = Mockito.mock(SmtDeviceTaskService.class);
		SmtAdmittanceApplyServiceImpl service = new SmtAdmittanceApplyServiceImpl();
		setField(service, "smtDeviceTaskService", taskService);
		setField(service, "putOffsetHour", 0);
		LocalDateTime startTime = LocalDateTime.of(2026, 6, 2, 8, 30, 0);
		LocalDateTime endTime = LocalDateTime.of(2026, 6, 2, 18, 0, 0);
		SmtAdmittanceApply apply = new SmtAdmittanceApply();
		apply.setStartTime(startTime);
		apply.setEndTime(endTime);
		SmtAdmittanceFellow fellow = new SmtAdmittanceFellow();
		fellow.setId(1001L);
		fellow.setFellowName("admittance visitor");
		fellow.setFellowPhotoId("image-1");
		fellow.setCertNo("cert-1");

		Method addCard = SmtAdmittanceApplyServiceImpl.class.getDeclaredMethod("addCard",
				SmtAdmittanceApply.class, SmtAdmittanceFellow.class, String.class);
		addCard.setAccessible(true);
		addCard.invoke(service, apply, fellow, "device-1");

		ArgumentCaptor<DeviceTaskVO> captor = ArgumentCaptor.forClass(DeviceTaskVO.class);
		Mockito.verify(taskService).saveTask(captor.capture());
		DeviceTaskVO task = captor.getValue();
		Assert.assertEquals(DeviceTaskConstants.CARD_ADMITTANCE, task.getServiceType());
		Assert.assertEquals(SmtVisitorEnum.CARD_TYPE_7.getType(), task.getCardType());
		Assert.assertEquals(Long.valueOf(endTime.atZone(ZoneId.systemDefault()).toEpochSecond()), task.getOverTime());
		Assert.assertEquals("cert-1", task.getApplyBadge());
	}

	@Test
	public void addCarCardKeepsAdmittanceExactEndTimeAndServiceType() throws Exception {
		SmtDeviceTaskService taskService = Mockito.mock(SmtDeviceTaskService.class);
		SmtAdmittanceApplyServiceImpl service = new SmtAdmittanceApplyServiceImpl();
		setField(service, "smtDeviceTaskService", taskService);
		setField(service, "putOffsetHour", 0);
		LocalDateTime startTime = LocalDateTime.of(2026, 6, 2, 8, 30, 0);
		LocalDateTime endTime = LocalDateTime.of(2026, 6, 2, 18, 0, 0);
		SmtAdmittanceApply apply = new SmtAdmittanceApply();
		apply.setStartTime(startTime);
		apply.setEndTime(endTime);
		apply.setCertNo("cert-apply");
		SmtAdmittanceVehicle vehicle = new SmtAdmittanceVehicle();
		vehicle.setId(4001L);
		vehicle.setPlate("粤B12345");

		Method addCarCard = SmtAdmittanceApplyServiceImpl.class.getDeclaredMethod("addCarCard",
				SmtAdmittanceApply.class, SmtAdmittanceVehicle.class, String.class);
		addCarCard.setAccessible(true);
		addCarCard.invoke(service, apply, vehicle, "car-device-1");

		ArgumentCaptor<DeviceTaskVO> captor = ArgumentCaptor.forClass(DeviceTaskVO.class);
		Mockito.verify(taskService).saveTask(captor.capture());
		DeviceTaskVO task = captor.getValue();
		Assert.assertEquals(DeviceTaskConstants.CAR, task.getDeviceType());
		Assert.assertEquals(DeviceTaskConstants.CAT_ADMITTANCE, task.getServiceType());
		Assert.assertEquals(Long.valueOf(endTime.atZone(ZoneId.systemDefault()).toEpochSecond()), task.getOverTime());
		Assert.assertEquals("cert-apply", task.getApplyBadge());
	}

	@Test
	public void addCarCardListKeepsAdmittanceExactEndTimeAndServiceType() throws Exception {
		SmtDeviceTaskService taskService = Mockito.mock(SmtDeviceTaskService.class);
		SmtAdmittanceApplyServiceImpl service = new SmtAdmittanceApplyServiceImpl();
		setField(service, "smtDeviceTaskService", taskService);
		setField(service, "putOffsetHour", 0);
		LocalDateTime startTime = LocalDateTime.of(2026, 6, 2, 8, 30, 0);
		LocalDateTime endTime = LocalDateTime.of(2026, 6, 2, 18, 0, 0);
		SmtAdmittanceApply apply = new SmtAdmittanceApply();
		apply.setStartTime(startTime);
		apply.setEndTime(endTime);
		apply.setCertNo("cert-apply");
		SmtAdmittanceVehicle vehicle = new SmtAdmittanceVehicle();
		vehicle.setId(4001L);
		vehicle.setPlate("粤B12345");

		Method addCarCard = SmtAdmittanceApplyServiceImpl.class.getDeclaredMethod("addCarCard",
				SmtAdmittanceApply.class, SmtAdmittanceVehicle.class, List.class);
		addCarCard.setAccessible(true);
		addCarCard.invoke(service, apply, vehicle, Collections.singletonList("car-device-1"));

		ArgumentCaptor<DeviceTaskVO> captor = ArgumentCaptor.forClass(DeviceTaskVO.class);
		Mockito.verify(taskService).saveTask(captor.capture());
		DeviceTaskVO task = captor.getValue();
		Assert.assertEquals(DeviceTaskConstants.CAR, task.getDeviceType());
		Assert.assertEquals(DeviceTaskConstants.CAT_ADMITTANCE, task.getServiceType());
		Assert.assertEquals(Long.valueOf(endTime.atZone(ZoneId.systemDefault()).toEpochSecond()), task.getOverTime());
		Assert.assertEquals("cert-apply", task.getApplyBadge());
	}

	@Test
	public void visitorEqualCheckIgnoresSameNameWhenCertNoDiffers() throws Exception {
		SmtAdmittanceApplyMapper mapper = Mockito.mock(SmtAdmittanceApplyMapper.class);
		SmtStaffService staffService = Mockito.mock(SmtStaffService.class);
		SmtAdmittanceApplyServiceImpl service = new SmtAdmittanceApplyServiceImpl();
		setField(service, "baseMapper", mapper);
		setField(service, "smtStaffService", staffService);
		LocalDateTime startTime = LocalDateTime.of(2026, 6, 8, 18, 28, 0);
		LocalDateTime endTime = LocalDateTime.of(2026, 10, 8, 15, 20, 0);
		SmtStaff receptionist = new SmtStaff();
		receptionist.setCertno("500101199001010000");
		Mockito.when(staffService.getSimpleSttaffByBadge("host-1")).thenReturn(receptionist);
		Mockito.when(mapper.selectCount(Mockito.any())).thenReturn(1);
		AdmittanceFellowReqDTO fellow = new AdmittanceFellowReqDTO();
		fellow.setFellowName("张鑫");
		fellow.setCertNo("411281199606254513");
		fellow.setIsMain(1);
		SaveAdmittanceApplyReqDTO request = new SaveAdmittanceApplyReqDTO();
		request.setReceptionistBadge("host-1");
		request.setStartTime(startTime);
		request.setEndTime(endTime);
		request.setFellowList(Collections.singletonList(fellow));

		Assert.assertTrue(service.visitorEqualCheck(request));
		Mockito.verify(mapper).countActiveMainFellowOverlapByCertNo(fellow.getCertNo(), startTime, endTime);
		Mockito.verify(mapper, Mockito.never()).selectCount(Mockito.any());
	}

	@Test
	public void visitorEqualCheckRejectsOverlappingMainFellowByCertNo() throws Exception {
		SmtAdmittanceApplyMapper mapper = Mockito.mock(SmtAdmittanceApplyMapper.class);
		SmtStaffService staffService = Mockito.mock(SmtStaffService.class);
		SmtAdmittanceApplyServiceImpl service = new SmtAdmittanceApplyServiceImpl();
		setField(service, "baseMapper", mapper);
		setField(service, "smtStaffService", staffService);
		LocalDateTime startTime = LocalDateTime.of(2026, 6, 8, 18, 28, 0);
		LocalDateTime endTime = LocalDateTime.of(2026, 10, 8, 15, 20, 0);
		SmtStaff receptionist = new SmtStaff();
		receptionist.setCertno("500101199001010000");
		Mockito.when(staffService.getSimpleSttaffByBadge("host-1")).thenReturn(receptionist);
		Mockito.when(mapper.countActiveMainFellowOverlapByCertNo("411281199606254513", startTime, endTime))
				.thenReturn(1);
		AdmittanceFellowReqDTO fellow = new AdmittanceFellowReqDTO();
		fellow.setFellowName("张鑫");
		fellow.setCertNo("411281199606254513");
		fellow.setIsMain(1);
		SaveAdmittanceApplyReqDTO request = new SaveAdmittanceApplyReqDTO();
		request.setReceptionistBadge("host-1");
		request.setStartTime(startTime);
		request.setEndTime(endTime);
		request.setFellowList(Collections.singletonList(fellow));

		try {
			service.visitorEqualCheck(request);
			Assert.fail("Expected overlapping certNo to be rejected");
		} catch (SmartException error) {
			Assert.assertTrue(error.getMessage().contains("已有预约，不能重复申请"));
		}
		Mockito.verify(mapper).countActiveMainFellowOverlapByCertNo(fellow.getCertNo(), startTime, endTime);
		Mockito.verify(mapper, Mockito.never()).selectCount(Mockito.any());
	}

	@Test
	public void visitorEqualCheckRejectsDuplicateWhenReceptionistCertNoIsMissing() throws Exception {
		SmtAdmittanceApplyMapper mapper = Mockito.mock(SmtAdmittanceApplyMapper.class);
		SmtStaffService staffService = Mockito.mock(SmtStaffService.class);
		SmtAdmittanceApplyServiceImpl service = new SmtAdmittanceApplyServiceImpl();
		setField(service, "baseMapper", mapper);
		setField(service, "smtStaffService", staffService);
		LocalDateTime startTime = LocalDateTime.of(2026, 6, 8, 18, 28, 0);
		LocalDateTime endTime = LocalDateTime.of(2026, 10, 8, 15, 20, 0);
		SmtStaff receptionist = new SmtStaff();
		Mockito.when(staffService.getSimpleSttaffByBadge("host-1")).thenReturn(receptionist);
		Mockito.when(mapper.countActiveMainFellowOverlapByCertNo("411281199606254513", startTime, endTime))
				.thenReturn(1);
		AdmittanceFellowReqDTO fellow = new AdmittanceFellowReqDTO();
		fellow.setFellowName("张鑫");
		fellow.setCertNo("411281199606254513");
		fellow.setIsMain(1);
		SaveAdmittanceApplyReqDTO request = new SaveAdmittanceApplyReqDTO();
		request.setReceptionistBadge("host-1");
		request.setStartTime(startTime);
		request.setEndTime(endTime);
		request.setFellowList(Collections.singletonList(fellow));

		try {
			service.visitorEqualCheck(request);
			Assert.fail("Expected duplicate certNo to be rejected even when receptionist certNo is missing");
		} catch (SmartException error) {
			Assert.assertTrue(error.getMessage().contains("已有预约，不能重复申请"));
		}
		Mockito.verify(mapper).countActiveMainFellowOverlapByCertNo(fellow.getCertNo(), startTime, endTime);
	}

	@Test
	public void saveAdmittanceApplyRunsDuplicateCheckBeforePersisting() throws Exception {
		SmtAdmittanceApplyMapper mapper = Mockito.mock(SmtAdmittanceApplyMapper.class);
		SmtStaffService staffService = Mockito.mock(SmtStaffService.class);
		SmtAdmittanceApplyServiceImpl service = new SmtAdmittanceApplyServiceImpl();
		setField(service, "baseMapper", mapper);
		setField(service, "smtStaffService", staffService);
		LocalDateTime startTime = LocalDateTime.of(2026, 6, 8, 18, 28, 0);
		LocalDateTime endTime = LocalDateTime.of(2026, 10, 8, 15, 20, 0);
		SmtStaff receptionist = new SmtStaff();
		receptionist.setCertno("500101199001010000");
		Mockito.when(staffService.getSimpleSttaffByBadge("host-1")).thenReturn(receptionist);
		Mockito.when(mapper.countActiveMainFellowOverlapByCertNo("411281199606254513", startTime, endTime))
				.thenReturn(1);
		AdmittanceFellowReqDTO fellow = new AdmittanceFellowReqDTO();
		fellow.setFellowName("张鑫");
		fellow.setCertNo("411281199606254513");
		fellow.setIsMain(1);
		SaveAdmittanceApplyReqDTO request = new SaveAdmittanceApplyReqDTO();
		request.setReceptionistBadge("host-1");
		request.setStartTime(startTime);
		request.setEndTime(endTime);
		request.setFellowList(Collections.singletonList(fellow));

		try {
			service.saveAdmittanceApply(request);
			Assert.fail("Expected saveAdmittanceApply to reject duplicate certNo before persisting");
		} catch (SmartException error) {
			Assert.assertTrue(error.getMessage().contains("已有预约，不能重复申请"));
		}
		Mockito.verify(mapper).countActiveMainFellowOverlapByCertNo(fellow.getCertNo(), startTime, endTime);
		Mockito.verify(mapper, Mockito.never()).insert(Mockito.any());
	}

	@Test
	public void getSmtVisitorPageCountsCardAdmittanceDownRecords() throws Exception {
		SmtAdmittanceApplyMapper mapper = Mockito.mock(SmtAdmittanceApplyMapper.class);
		SmtAdmittanceFellowService fellowService = Mockito.mock(SmtAdmittanceFellowService.class);
		SmtTaskDownRecordService taskDownRecordService = Mockito.mock(SmtTaskDownRecordService.class);
		SmtIscDownRecordService iscDownRecordService = Mockito.mock(SmtIscDownRecordService.class);
		SmtAdmittanceApplyServiceImpl service = new SmtAdmittanceApplyServiceImpl();
		setField(service, "baseMapper", mapper);
		setField(service, "smtAdmittanceFellowService", fellowService);
		setField(service, "smtTaskDownRecordService", taskDownRecordService);
		setField(service, "smtIscDownRecordService", iscDownRecordService);
		setField(service, "imageService", Mockito.mock(ImageService.class));
		SearchSmtVisitorVO record = new SearchSmtVisitorVO();
		record.setId("2001");
		record.setApplyType(AdmittanceTypeEnum.PERSON.getCode());
		Page<SearchSmtVisitorVO> pageResult = new Page<>();
		pageResult.setRecords(Collections.singletonList(record));
		SmtAdmittanceFellow fellow = new SmtAdmittanceFellow();
		fellow.setId(3001L);
		Mockito.when(mapper.getSmtVisitorPage(Mockito.any(), Mockito.any(), Mockito.anyList())).thenReturn(pageResult);
		Mockito.when(fellowService.getByApplyId(2001L)).thenReturn(Collections.singletonList(fellow));
		Mockito.when(taskDownRecordService.count(Mockito.any())).thenReturn(1);
		SmartUser smartUser = new SmartUser(1, 1, "tester", Collections.singletonList(5000021),
				"password", true, true, true, true, Collections.emptyList());
		SecurityContextHolder.getContext().setAuthentication(
				new UsernamePasswordAuthenticationToken(smartUser, "password", Collections.emptyList()));
		try {
			IPage<SearchSmtVisitorVO> result = service.getSmtVisitorPage(new Page<>(), new SearchSmtVisitorDTO());

			Assert.assertEquals(OneOrZeroEnum.ONE.getCode(), result.getRecords().get(0).getHasAuth());
			ArgumentCaptor<LambdaQueryWrapper> countQueryCaptor = ArgumentCaptor.forClass(LambdaQueryWrapper.class);
			Mockito.verify(taskDownRecordService).count(countQueryCaptor.capture());
			countQueryCaptor.getValue().getSqlSegment();
			Assert.assertTrue(countQueryCaptor.getValue().getParamNameValuePairs().values().stream()
					.anyMatch(value -> DeviceTaskConstants.CARD.toString().equals(String.valueOf(value))));
			Assert.assertTrue(countQueryCaptor.getValue().getParamNameValuePairs().values().stream()
					.anyMatch(value -> DeviceTaskConstants.CARD_ADMITTANCE.toString().equals(String.valueOf(value))));
			Assert.assertFalse(countQueryCaptor.getValue().getParamNameValuePairs().values().stream()
					.anyMatch(value -> DeviceTaskConstants.CARD_VISITOR.toString().equals(String.valueOf(value))));
			Mockito.verify(iscDownRecordService, Mockito.never()).count(Mockito.any());
		} finally {
			SecurityContextHolder.clearContext();
		}
	}

	@Test
	public void delTaskCreatesCardAdmittanceDeleteTask() throws Exception {
		SmtDeviceTaskService taskService = Mockito.mock(SmtDeviceTaskService.class);
		SmtIscDeviceTaskService iscTaskService = Mockito.mock(SmtIscDeviceTaskService.class);
		SmtAdmittanceFellowService fellowService = Mockito.mock(SmtAdmittanceFellowService.class);
		SmtAdmittanceAreaTypeAuthService areaTypeAuthService = Mockito.mock(SmtAdmittanceAreaTypeAuthService.class);
		SmtDeviceAuthorityRelationService relationService = Mockito.mock(SmtDeviceAuthorityRelationService.class);
		SmtAdmittanceApplyServiceImpl service = new SmtAdmittanceApplyServiceImpl();
		setField(service, "smtDeviceTaskService", taskService);
		setField(service, "smtIscDeviceTaskService", iscTaskService);
		setField(service, "smtAdmittanceFellowService", fellowService);
		setField(service, "smtAdmittanceAreaTypeAuthService", areaTypeAuthService);
		setField(service, "smtDeviceAuthorityRelationService", relationService);
		SmtAdmittanceApply apply = new SmtAdmittanceApply();
		apply.setId(2001L);
		apply.setAreaType("10");
		apply.setParkId(5000021);
		apply.setIsVehicle(OneOrZeroEnum.ZERO.getCode());
		apply.setVisitorPhotoId("image-1");
		apply.setVisitorName("admittance visitor");
		SmtAdmittanceFellow fellow = new SmtAdmittanceFellow();
		fellow.setId(3001L);
		fellow.setCertNo("cert-1");
		SmtAdmittanceAreaTypeAuth auth = SmtAdmittanceAreaTypeAuth.builder().authId(9001).build();
		SmtDeviceAuthorityRelation relation = new SmtDeviceAuthorityRelation();
		relation.setDeviceId("device-1");
		Mockito.when(fellowService.getByApplyId(apply.getId())).thenReturn(Collections.singletonList(fellow));
		Mockito.when(areaTypeAuthService.getAuthByType(apply.getAreaType(), DeviceTypeEnum.DEVICE_TYPE_1.getCode(), apply.getParkId()))
				.thenReturn(Collections.singletonList(auth));
		Mockito.when(relationService.getRelationByAuthId(Mockito.anyList())).thenReturn(Collections.singletonList(relation));

		Method delTask = SmtAdmittanceApplyServiceImpl.class.getDeclaredMethod("delTask", SmtAdmittanceApply.class);
		delTask.setAccessible(true);
		delTask.invoke(service, apply);

		ArgumentCaptor<DeviceTaskVO> captor = ArgumentCaptor.forClass(DeviceTaskVO.class);
		Mockito.verify(taskService).saveTask(captor.capture());
		DeviceTaskVO task = captor.getValue();
		Assert.assertEquals(DeviceTaskActionEnum.DEL.getCode(), task.getAction());
		Assert.assertEquals(DeviceTaskConstants.CARD, task.getDeviceType());
		Assert.assertEquals(DeviceTaskConstants.CARD_ADMITTANCE, task.getServiceType());
		Assert.assertEquals("3001", task.getCardNo());
		Assert.assertEquals("device-1", task.getDeviceCode());
		Assert.assertEquals("cert-1", task.getApplyBadge());
		ArgumentCaptor<LambdaQueryWrapper> deviceDeleteQueryCaptor = ArgumentCaptor.forClass(LambdaQueryWrapper.class);
		Mockito.verify(taskService).list(deviceDeleteQueryCaptor.capture());
		assertQueryHasParam(deviceDeleteQueryCaptor.getValue(), DeviceTaskConstants.CARD);
		ArgumentCaptor<LambdaQueryWrapper> iscDeleteQueryCaptor = ArgumentCaptor.forClass(LambdaQueryWrapper.class);
		Mockito.verify(iscTaskService).list(iscDeleteQueryCaptor.capture());
		assertQueryHasParam(iscDeleteQueryCaptor.getValue(), DeviceTaskConstants.CARD);
	}

	@Test
	public void delTaskIgnoresHistoricalCardAdmittanceDeleteTask() throws Exception {
		SmtDeviceTaskService taskService = Mockito.mock(SmtDeviceTaskService.class);
		SmtIscDeviceTaskService iscTaskService = Mockito.mock(SmtIscDeviceTaskService.class);
		SmtAdmittanceFellowService fellowService = Mockito.mock(SmtAdmittanceFellowService.class);
		SmtAdmittanceAreaTypeAuthService areaTypeAuthService = Mockito.mock(SmtAdmittanceAreaTypeAuthService.class);
		SmtDeviceAuthorityRelationService relationService = Mockito.mock(SmtDeviceAuthorityRelationService.class);
		SmtAdmittanceApplyServiceImpl service = new SmtAdmittanceApplyServiceImpl();
		setField(service, "smtDeviceTaskService", taskService);
		setField(service, "smtIscDeviceTaskService", iscTaskService);
		setField(service, "smtAdmittanceFellowService", fellowService);
		setField(service, "smtAdmittanceAreaTypeAuthService", areaTypeAuthService);
		setField(service, "smtDeviceAuthorityRelationService", relationService);
		SmtAdmittanceApply apply = new SmtAdmittanceApply();
		apply.setId(2001L);
		apply.setAreaType("10");
		apply.setParkId(5000021);
		apply.setIsVehicle(OneOrZeroEnum.ZERO.getCode());
		SmtAdmittanceFellow fellow = new SmtAdmittanceFellow();
		fellow.setId(3001L);
		fellow.setCertNo("cert-1");
		SmtAdmittanceAreaTypeAuth auth = SmtAdmittanceAreaTypeAuth.builder().authId(9001).build();
		SmtDeviceAuthorityRelation relation = new SmtDeviceAuthorityRelation();
		relation.setDeviceId("device-1");
		SmtDeviceTask historicalDeleteTask = new SmtDeviceTask();
		historicalDeleteTask.setStatus(DeviceTaskStatusEnum.SUCCESS.getCode());
		Mockito.when(fellowService.getByApplyId(apply.getId())).thenReturn(Collections.singletonList(fellow));
		Mockito.when(areaTypeAuthService.getAuthByType(apply.getAreaType(), DeviceTypeEnum.DEVICE_TYPE_1.getCode(), apply.getParkId()))
				.thenReturn(Collections.singletonList(auth));
		Mockito.when(relationService.getRelationByAuthId(Mockito.anyList())).thenReturn(Collections.singletonList(relation));
		Mockito.when(taskService.list(Mockito.any())).thenReturn(Collections.singletonList(historicalDeleteTask));

		Method delTask = SmtAdmittanceApplyServiceImpl.class.getDeclaredMethod("delTask", SmtAdmittanceApply.class);
		delTask.setAccessible(true);
		delTask.invoke(service, apply);

		Mockito.verify(taskService, Mockito.never()).updateById(Mockito.eq(historicalDeleteTask));
		ArgumentCaptor<LambdaQueryWrapper> deviceDeleteQueryCaptor = ArgumentCaptor.forClass(LambdaQueryWrapper.class);
		Mockito.verify(taskService).list(deviceDeleteQueryCaptor.capture());
		assertQueryHasParam(deviceDeleteQueryCaptor.getValue(), DeviceTaskConstants.CARD);
		ArgumentCaptor<DeviceTaskVO> captor = ArgumentCaptor.forClass(DeviceTaskVO.class);
		Mockito.verify(taskService).saveTask(captor.capture());
		Assert.assertEquals(DeviceTaskConstants.CARD, captor.getValue().getDeviceType());
		Assert.assertEquals(DeviceTaskConstants.CARD_ADMITTANCE, captor.getValue().getServiceType());
		Assert.assertEquals("3001", captor.getValue().getCardNo());
	}

	@Test
	public void delTaskDoesNotReuseIscDeleteTaskAfterMaxRetryTimes() throws Exception {
		SmtDeviceTaskService taskService = Mockito.mock(SmtDeviceTaskService.class);
		SmtIscDeviceTaskService iscTaskService = Mockito.mock(SmtIscDeviceTaskService.class);
		SmtAdmittanceFellowService fellowService = Mockito.mock(SmtAdmittanceFellowService.class);
		SmtAdmittanceAreaTypeAuthService areaTypeAuthService = Mockito.mock(SmtAdmittanceAreaTypeAuthService.class);
		SmtDeviceAuthorityRelationService relationService = Mockito.mock(SmtDeviceAuthorityRelationService.class);
		SmtAdmittanceApplyServiceImpl service = new SmtAdmittanceApplyServiceImpl();
		setField(service, "smtDeviceTaskService", taskService);
		setField(service, "smtIscDeviceTaskService", iscTaskService);
		setField(service, "smtAdmittanceFellowService", fellowService);
		setField(service, "smtAdmittanceAreaTypeAuthService", areaTypeAuthService);
		setField(service, "smtDeviceAuthorityRelationService", relationService);
		SmtAdmittanceApply apply = new SmtAdmittanceApply();
		apply.setId(2002L);
		apply.setAreaType("10");
		apply.setParkId(5000021);
		apply.setIsVehicle(OneOrZeroEnum.ZERO.getCode());
		SmtAdmittanceFellow fellow = new SmtAdmittanceFellow();
		fellow.setId(3002L);
		fellow.setCertNo("cert-2");
		SmtAdmittanceAreaTypeAuth auth = SmtAdmittanceAreaTypeAuth.builder().authId(9002).build();
		SmtDeviceAuthorityRelation relation = new SmtDeviceAuthorityRelation();
		relation.setDeviceId("device-2");
		SmtIscDeviceTask retryExceededTask = new SmtIscDeviceTask();
		retryExceededTask.setId(9102L);
		retryExceededTask.setStatus(DeviceTaskStatusEnum.FAIL.getCode());
		retryExceededTask.setTimes(DeviceTaskConstants.AUTH_CONFIG_MAX_RETRY_TIMES);
		retryExceededTask.setCode(ISCDeviceTaskEnum.AUTH_CONFIG_DOWN_FAIL.getCode());
		retryExceededTask.setRemark("请人工介入处理");
		Mockito.when(fellowService.getByApplyId(apply.getId())).thenReturn(Collections.singletonList(fellow));
		Mockito.when(areaTypeAuthService.getAuthByType(apply.getAreaType(), DeviceTypeEnum.DEVICE_TYPE_1.getCode(), apply.getParkId()))
				.thenReturn(Collections.singletonList(auth));
		Mockito.when(relationService.getRelationByAuthId(Mockito.anyList())).thenReturn(Collections.singletonList(relation));
		Mockito.when(taskService.list(Mockito.any())).thenReturn(Collections.emptyList());
		Mockito.when(iscTaskService.list(Mockito.any())).thenReturn(Collections.singletonList(retryExceededTask));

		Method delTask = SmtAdmittanceApplyServiceImpl.class.getDeclaredMethod("delTask", SmtAdmittanceApply.class);
		delTask.setAccessible(true);
		delTask.invoke(service, apply);

		Mockito.verify(iscTaskService, Mockito.never()).updateById(Mockito.eq(retryExceededTask));
		ArgumentCaptor<LambdaQueryWrapper> iscDeleteQueryCaptor = ArgumentCaptor.forClass(LambdaQueryWrapper.class);
		Mockito.verify(iscTaskService).list(iscDeleteQueryCaptor.capture());
		assertQueryHasParam(iscDeleteQueryCaptor.getValue(), DeviceTaskConstants.AUTH_CONFIG_MAX_RETRY_TIMES);
		ArgumentCaptor<DeviceTaskVO> captor = ArgumentCaptor.forClass(DeviceTaskVO.class);
		Mockito.verify(taskService).saveTask(captor.capture());
		Assert.assertEquals(DeviceTaskConstants.CARD, captor.getValue().getDeviceType());
		Assert.assertEquals(DeviceTaskConstants.CARD_ADMITTANCE, captor.getValue().getServiceType());
		Assert.assertEquals("3002", captor.getValue().getCardNo());
		Assert.assertEquals("请人工介入处理", retryExceededTask.getRemark());
		Assert.assertEquals(DeviceTaskConstants.AUTH_CONFIG_MAX_RETRY_TIMES, retryExceededTask.getTimes());
	}

	@Test
	public void delTaskIgnoresHistoricalCarAdmittanceDeleteTask() throws Exception {
		SmtDeviceTaskService taskService = Mockito.mock(SmtDeviceTaskService.class);
		SmtIscDeviceTaskService iscTaskService = Mockito.mock(SmtIscDeviceTaskService.class);
		SmtAdmittanceFellowService fellowService = Mockito.mock(SmtAdmittanceFellowService.class);
		SmtAdmittanceVehicleService vehicleService = Mockito.mock(SmtAdmittanceVehicleService.class);
		SmtAdmittanceAreaTypeAuthService areaTypeAuthService = Mockito.mock(SmtAdmittanceAreaTypeAuthService.class);
		SmtDeviceAuthorityRelationService relationService = Mockito.mock(SmtDeviceAuthorityRelationService.class);
		SmtAdmittanceApplyServiceImpl service = new SmtAdmittanceApplyServiceImpl();
		setField(service, "smtDeviceTaskService", taskService);
		setField(service, "smtIscDeviceTaskService", iscTaskService);
		setField(service, "smtAdmittanceFellowService", fellowService);
		setField(service, "smtAdmittanceVehicleService", vehicleService);
		setField(service, "smtAdmittanceAreaTypeAuthService", areaTypeAuthService);
		setField(service, "smtDeviceAuthorityRelationService", relationService);
		SmtAdmittanceApply apply = new SmtAdmittanceApply();
		apply.setId(2001L);
		apply.setAreaType("10");
		apply.setParkId(5000021);
		apply.setIsVehicle(OneOrZeroEnum.ONE.getCode());
		SmtAdmittanceVehicle vehicle = new SmtAdmittanceVehicle();
		vehicle.setId(4001L);
		vehicle.setPlate("粤B12345");
		SmtAdmittanceAreaTypeAuth carAuth = SmtAdmittanceAreaTypeAuth.builder().authId(9002).build();
		SmtDeviceAuthorityRelation relation = new SmtDeviceAuthorityRelation();
		relation.setDeviceId("car-device-1");
		SmtDeviceTask historicalDeleteTask = new SmtDeviceTask();
		historicalDeleteTask.setStatus(DeviceTaskStatusEnum.SUCCESS.getCode());
		Mockito.when(fellowService.getByApplyId(apply.getId())).thenReturn(Collections.emptyList());
		Mockito.when(vehicleService.getByApplyId(apply.getId())).thenReturn(Collections.singletonList(vehicle));
		Mockito.when(areaTypeAuthService.getAuthByType(apply.getAreaType(), DeviceTypeEnum.DEVICE_TYPE_1.getCode(), apply.getParkId()))
				.thenReturn(Collections.emptyList());
		Mockito.when(areaTypeAuthService.getAuthByType(apply.getAreaType(), DeviceTypeEnum.DEVICE_TYPE_3.getCode(), apply.getParkId()))
				.thenReturn(Collections.singletonList(carAuth));
		Mockito.when(relationService.getRelationByAuthId(Mockito.anyList())).thenReturn(Collections.singletonList(relation));
		Mockito.when(taskService.list(Mockito.any())).thenReturn(Collections.singletonList(historicalDeleteTask));

		Method delTask = SmtAdmittanceApplyServiceImpl.class.getDeclaredMethod("delTask", SmtAdmittanceApply.class);
		delTask.setAccessible(true);
		delTask.invoke(service, apply);

		Mockito.verify(taskService, Mockito.never()).updateById(Mockito.eq(historicalDeleteTask));
		ArgumentCaptor<LambdaQueryWrapper> deviceDeleteQueryCaptor = ArgumentCaptor.forClass(LambdaQueryWrapper.class);
		Mockito.verify(taskService).list(deviceDeleteQueryCaptor.capture());
		assertQueryHasParam(deviceDeleteQueryCaptor.getValue(), DeviceTaskConstants.CAR);
		ArgumentCaptor<DeviceTaskVO> captor = ArgumentCaptor.forClass(DeviceTaskVO.class);
		Mockito.verify(taskService).saveTask(captor.capture());
		Assert.assertEquals(DeviceTaskConstants.CAR, captor.getValue().getDeviceType());
		Assert.assertEquals(DeviceTaskConstants.CAT_ADMITTANCE, captor.getValue().getServiceType());
		Assert.assertEquals("4001", captor.getValue().getCardNo());
		Assert.assertEquals("car-device-1", captor.getValue().getDeviceCode());
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
