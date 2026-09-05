package com.tce.smart.platform.service.admittance.impl;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.tce.smart.common.core.exception.SmartException;
import com.tce.smart.common.core.util.DateUtils;
import com.tce.smart.common.security.service.SmartUser;
import com.tce.smart.platform.api.dto.req.admittance.VisitorManualAuthReqDTO;
import com.tce.smart.platform.api.dto.resp.admittance.VisitorManualAuthOptionsRespDTO;
import com.tce.smart.platform.core.dto.DeviceTaskVO;
import com.tce.smart.platform.core.entity.SmtDevice;
import com.tce.smart.platform.core.entity.SmtDeviceAuthority;
import com.tce.smart.platform.core.entity.SmtDeviceAuthorityRelation;
import com.tce.smart.platform.core.entity.admittance.SmtAdmittanceApply;
import com.tce.smart.platform.core.entity.admittance.SmtAdmittanceFellow;
import com.tce.smart.platform.core.mapper.SmtAdmittanceApplyMapper;
import com.tce.smart.platform.core.service.SmtDeviceService;
import com.tce.smart.platform.core.service.SmtDeviceTaskService;
import com.tce.smart.platform.service.SmtDeviceAuthorityRelationService;
import com.tce.smart.platform.service.SmtDeviceAuthorityService;
import com.tce.smart.platform.service.admittance.SmtAdmittanceFellowService;
import com.tce.smart.tool.constant.DeviceTaskConstants;
import com.tce.smart.tool.enums.AdmittanceTypeEnum;
import com.tce.smart.tool.enums.DeviceTaskActionEnum;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionTemplate;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * 访客手动下发服务的真实业务行为测试。
 * 仅 mock 数据访问与任务保存边界，任务字段和拒绝副作用由真实服务逻辑验证。
 */
public class VisitorManualAuthServiceImplTest {

	@BeforeClass
	public static void initMybatisPlusLambdaCache() {
		MapperBuilderAssistant assistant = new MapperBuilderAssistant(new MybatisConfiguration(), "");
		TableInfoHelper.initTableInfo(assistant, SmtAdmittanceApply.class);
		TableInfoHelper.initTableInfo(assistant, SmtDeviceAuthority.class);
		TableInfoHelper.initTableInfo(assistant, SmtDeviceAuthorityRelation.class);
		TableInfoHelper.initTableInfo(assistant, SmtDevice.class);
	}

	private SmtAdmittanceApplyMapper applyMapper;
	private SmtAdmittanceFellowService fellowService;
	private SmtDeviceAuthorityService authorityService;
	private SmtDeviceAuthorityRelationService relationService;
	private SmtDeviceService deviceService;
	private SmtDeviceTaskService taskService;
	private PlatformTransactionManager transactionManager;
	private TransactionStatus transactionStatus;
	private VisitorManualAuthServiceImpl service;
	private SmtAdmittanceApply apply;
	private SmtAdmittanceFellow fellow;

	@Before
	public void setUp() throws Exception {
		applyMapper = Mockito.mock(SmtAdmittanceApplyMapper.class);
		fellowService = Mockito.mock(SmtAdmittanceFellowService.class);
		authorityService = Mockito.mock(SmtDeviceAuthorityService.class);
		relationService = Mockito.mock(SmtDeviceAuthorityRelationService.class);
		deviceService = Mockito.mock(SmtDeviceService.class);
		taskService = Mockito.mock(SmtDeviceTaskService.class);
		service = new VisitorManualAuthServiceImpl();
		setField(service, "baseMapper", applyMapper);
		setField(service, "smtAdmittanceFellowService", fellowService);
		setField(service, "smtDeviceAuthorityService", authorityService);
		setField(service, "smtDeviceAuthorityRelationService", relationService);
		setField(service, "smtDeviceService", deviceService);
		setField(service, "smtDeviceTaskService", taskService);
		setField(service, "putOffsetHour", 2);
		setField(service, "transactionTemplate", newTransactionTemplate());

		apply = new SmtAdmittanceApply();
		apply.setId(101L);
		apply.setParkId(7);
		apply.setStatus(0);
		apply.setApplyType(AdmittanceTypeEnum.PERSON.getCode());
		apply.setStartTime(LocalDateTime.now().plusHours(1).withNano(0));
		apply.setEndTime(LocalDateTime.now().plusHours(5).withNano(0));
		fellow = new SmtAdmittanceFellow();
		fellow.setId(201L);
		fellow.setVisitorId(apply.getId());
		fellow.setFellowName("测试访客");
		fellow.setFellowPhotoId("photo-201");
		fellow.setCertNo("cert-201");
		Mockito.when(applyMapper.update(Mockito.isNull(), Mockito.any())).thenReturn(1);
		Mockito.when(applyMapper.selectById(apply.getId())).thenReturn(apply);
		Mockito.when(fellowService.getByApplyId(apply.getId())).thenReturn(Collections.singletonList(fellow));
		loginWithPark(7);
	}

	@After
	public void tearDown() {
		SecurityContextHolder.clearContext();
	}

	@Test
	public void submitCreatesIscCardTasksWithDeduplicatedDevicesAndExactWindow() throws Exception {
		SmtDeviceAuthority firstAuthority = authority(401, "公共组一", 0);
		SmtDeviceAuthority secondAuthority = authority(402, "公共组二", 0);
		SmtDevice firstDevice = device("isc-gate-a");
		SmtDevice secondDevice = device("isc-gate-b");
		Mockito.when(authorityService.list(Mockito.any())).thenReturn(Arrays.asList(firstAuthority, secondAuthority));
		Mockito.when(relationService.getRelationByAuthId(Mockito.anyList())).thenReturn(Arrays.asList(
			relation(401, "isc-gate-a"), relation(402, "isc-gate-a"), relation(402, "isc-gate-b")));
		Mockito.when(deviceService.getById("isc-gate-a")).thenReturn(firstDevice);
		Mockito.when(deviceService.getById("isc-gate-b")).thenReturn(secondDevice);
		Mockito.when(taskService.saveTask(Mockito.any(DeviceTaskVO.class))).thenReturn("9001", "9002");

		VisitorManualAuthReqDTO request = request(101L, 201L, Arrays.asList(401, 402));
		String batchId = service.submit(request);

		assertNotNull(batchId);
		Assert.assertNull("手动批次不能覆盖自动审批进度指针", apply.getIscSubmitBatch());
		ArgumentCaptor<DeviceTaskVO> taskCaptor = ArgumentCaptor.forClass(DeviceTaskVO.class);
		Mockito.verify(taskService, Mockito.times(2)).saveTask(taskCaptor.capture());
		List<DeviceTaskVO> tasks = taskCaptor.getAllValues();
		assertEquals(Arrays.asList("isc-gate-a", "isc-gate-b"), Arrays.asList(tasks.get(0).getDeviceCode(), tasks.get(1).getDeviceCode()));
		for (DeviceTaskVO task : tasks) {
			assertEquals("201", task.getCardNo());
			assertEquals(DeviceTaskConstants.CARD, task.getDeviceType());
			assertEquals(DeviceTaskActionEnum.DOWN.getCode(), task.getAction());
			assertEquals(DeviceTaskConstants.CARD_ADMITTANCE, task.getServiceType());
			assertEquals(apply.getId(), task.getApplyId());
			assertEquals(Long.valueOf(batchId), task.getBatchId());
			assertEquals("photo-201", task.getImageId());
			assertEquals("cert-201", task.getApplyBadge());
			assertEquals(Long.valueOf(apply.getStartTime().minusHours(2).atZone(ZoneId.systemDefault()).toEpochSecond()), task.getStartTime());
			assertEquals(Long.valueOf(apply.getEndTime().atZone(ZoneId.systemDefault()).toEpochSecond()), task.getOverTime());
		}
	}

	@Test
	public void submitRejectsVehicleAndDoesNotCreateTask() {
		VisitorManualAuthReqDTO request = request(101L, null, Collections.singletonList(401));
		request.setVehicleId(301L);

		expectSmartException("车辆权限暂不支持下发", request);
		Mockito.verify(taskService, Mockito.never()).saveTask(Mockito.any(DeviceTaskVO.class));
	}

	@Test
	public void submitRejectsAuthorityOutsideApplyPark() {
		SmtDeviceAuthority authority = authority(401, "其他园区组", 0);
		authority.setParkId(8);
		Mockito.when(authorityService.list(Mockito.any())).thenReturn(Collections.singletonList(authority));

		VisitorManualAuthReqDTO request = request(101L, 201L, Collections.singletonList(401));
		expectSmartException("权限组不属于当前申请园区", request);
		Mockito.verify(taskService, Mockito.never()).saveTask(Mockito.any(DeviceTaskVO.class));
	}

	@Test
	public void submitRejectsSecretAuthorityBeforeSavingTask() {
		SmtDeviceAuthority authority = authority(401, "涉密组", 1);
		Mockito.when(authorityService.list(Mockito.any())).thenReturn(Collections.singletonList(authority));

		VisitorManualAuthReqDTO request = request(101L, 201L, Collections.singletonList(401));
		expectSmartException("保密考试校验尚未接入，暂不支持下发保密权限", request);
		Mockito.verify(taskService, Mockito.never()).saveTask(Mockito.any(DeviceTaskVO.class));
	}

	@Test
	public void submitRejectsNonIscDeviceInsteadOfSkippingIt() {
		SmtDeviceAuthority authority = authority(401, "公共组", 0);
		SmtDevice nonIscDevice = device("legacy-gate");
		nonIscDevice.setIsSync(0);
		Mockito.when(authorityService.list(Mockito.any())).thenReturn(Collections.singletonList(authority));
		Mockito.when(relationService.getRelationByAuthId(Mockito.anyList())).thenReturn(Collections.singletonList(relation(401, "legacy-gate")));
		Mockito.when(deviceService.getById("legacy-gate")).thenReturn(nonIscDevice);

		VisitorManualAuthReqDTO request = request(101L, 201L, Collections.singletonList(401));
		expectSmartException("权限组包含不支持的设备，无法下发", request);
		Mockito.verify(taskService, Mockito.never()).saveTask(Mockito.any(DeviceTaskVO.class));
	}

	@Test
	public void submitRejectsDisabledIscDeviceBeforeSavingTask() {
		SmtDeviceAuthority authority = authority(401, "公共组", 0);
		SmtDevice disabledDevice = device("disabled-gate");
		disabledDevice.setEnableStatus(2);
		Mockito.when(authorityService.list(Mockito.any())).thenReturn(Collections.singletonList(authority));
		Mockito.when(relationService.getRelationByAuthId(Mockito.anyList())).thenReturn(Collections.singletonList(relation(401, "disabled-gate")));
		Mockito.when(deviceService.getById("disabled-gate")).thenReturn(disabledDevice);

		VisitorManualAuthReqDTO request = request(101L, 201L, Collections.singletonList(401));
		expectSmartException("权限组包含不支持的设备，无法下发", request);
		Mockito.verify(taskService, Mockito.never()).saveTask(Mockito.any(DeviceTaskVO.class));
	}

	@Test
	public void submitRejectsFellowFromAnotherApplyBeforeLoadingAuthorities() {
		fellow.setVisitorId(999L);
		VisitorManualAuthReqDTO request = request(101L, 201L, Collections.singletonList(401));

		expectSmartException("人员不属于当前申请", request);
		Mockito.verifyZeroInteractions(authorityService, relationService, deviceService, taskService);
	}

	@Test
	public void submitRejectsCrossParkApplyBeforeReadingApplyAgain() throws Exception {
		apply.setParkId(8);
		Mockito.when(applyMapper.update(Mockito.isNull(), Mockito.any())).thenReturn(0);
		VisitorManualAuthReqDTO request = request(101L, 201L, Collections.singletonList(401));

		expectSmartException("申请单不存在、已过期或当前状态不可下发", request);
		Mockito.verify(applyMapper, Mockito.never()).selectById(101L);
		Mockito.verifyZeroInteractions(authorityService, relationService, deviceService, taskService);
	}

	@Test
	public void emptyParkClaimIsRejectedBeforeDatabaseAccess() {
		SmartUser user = new SmartUser(1, 1, "tester", Collections.singletonList(null),
				"password", true, true, true, true, Collections.emptyList());
		SecurityContextHolder.getContext().setAuthentication(
				new UsernamePasswordAuthenticationToken(user, "password", Collections.emptyList()));
		VisitorManualAuthReqDTO request = request(101L, 201L, Collections.singletonList(401));

		expectSmartException("无可访问园区，无法操作访客权限", request);
		Mockito.verifyZeroInteractions(applyMapper, fellowService, authorityService, relationService, deviceService, taskService);
	}

	@Test
	public void missingAuthenticationIsRejectedBeforeDatabaseAccess() {
		SecurityContextHolder.clearContext();
		VisitorManualAuthReqDTO request = request(101L, 201L, Collections.singletonList(401));

		expectSmartException("未登录，无法操作访客权限", request);
		Mockito.verifyZeroInteractions(applyMapper, fellowService, authorityService, relationService, deviceService, taskService);
	}

	@Test
	public void submitRejectsVisitorTypeAuthorityForPersonManualAuth() {
		SmtDeviceAuthority authority = authority(401, "访客组", 0);
		authority.setType(2);
		Mockito.when(authorityService.list(Mockito.any())).thenReturn(Collections.singletonList(authority));
		VisitorManualAuthReqDTO request = request(101L, 201L, Collections.singletonList(401));

		expectSmartException("仅支持人员权限组", request);
		Mockito.verify(taskService, Mockito.never()).saveTask(Mockito.any(DeviceTaskVO.class));
	}

	@Test
	public void submitRejectsPendingApplyBeforeReadingObjects() {
		apply.setStatus(2);
		Mockito.when(applyMapper.update(Mockito.isNull(), Mockito.any())).thenReturn(0);
		VisitorManualAuthReqDTO request = request(101L, 201L, Collections.singletonList(401));

		expectSmartException("申请单不存在、已过期或当前状态不可下发", request);
		Mockito.verifyZeroInteractions(fellowService, authorityService, relationService, deviceService, taskService);
	}

	@Test
	public void submitRejectsReversedApplyWindowAfterLockedReread() {
		apply.setStartTime(LocalDateTime.now().plusHours(5).withNano(0));
		apply.setEndTime(LocalDateTime.now().plusHours(1).withNano(0));
		VisitorManualAuthReqDTO request = request(101L, 201L, Collections.singletonList(401));

		expectSmartException("申请单时间范围无效", request);
		Mockito.verifyZeroInteractions(authorityService, relationService, deviceService, taskService);
	}

	@Test
	public void submitRejectsExistingTaskInsteadOfReturningEmptyBatch() {
		SmtDeviceAuthority authority = authority(401, "公共组", 0);
		Mockito.when(authorityService.list(Mockito.any())).thenReturn(Collections.singletonList(authority));
		Mockito.when(relationService.getRelationByAuthId(Mockito.anyList())).thenReturn(Collections.singletonList(relation(401, "isc-gate-a")));
		Mockito.when(deviceService.getById("isc-gate-a")).thenReturn(device("isc-gate-a"));
		Mockito.when(taskService.saveTask(Mockito.any(DeviceTaskVO.class))).thenReturn("任务已存在");

		VisitorManualAuthReqDTO request = request(101L, 201L, Collections.singletonList(401));
		expectSmartException("权限任务已存在，请勿重复下发", request);
		Assert.assertNull("任务未创建时不能返回新的批次号", apply.getIscSubmitBatch());
	}

	@Test
	public void submitRejectsEmptyTaskSaveResult() {
		SmtDeviceAuthority authority = authority(401, "公共组", 0);
		Mockito.when(authorityService.list(Mockito.any())).thenReturn(Collections.singletonList(authority));
		Mockito.when(relationService.getRelationByAuthId(Mockito.anyList())).thenReturn(Collections.singletonList(relation(401, "isc-gate-a")));
		Mockito.when(deviceService.getById("isc-gate-a")).thenReturn(device("isc-gate-a"));
		Mockito.when(taskService.saveTask(Mockito.any(DeviceTaskVO.class))).thenReturn(null);

		VisitorManualAuthReqDTO request = request(101L, 201L, Collections.singletonList(401));
		expectSmartException("设备任务保存失败：任务保存结果为空", request);
		Assert.assertNull("空保存结果不能产生批次成功", apply.getIscSubmitBatch());
	}

	@Test
	public void submitRollsBackWhenSecondDeviceSaveReturnsNonNumericResult() {
		SmtDeviceAuthority authority = authority(401, "公共组", 0);
		Mockito.when(authorityService.list(Mockito.any())).thenReturn(Collections.singletonList(authority));
		Mockito.when(relationService.getRelationByAuthId(Mockito.anyList())).thenReturn(Arrays.asList(
			relation(401, "isc-gate-a"), relation(401, "isc-gate-b")));
		Mockito.when(deviceService.getById("isc-gate-a")).thenReturn(device("isc-gate-a"));
		Mockito.when(deviceService.getById("isc-gate-b")).thenReturn(device("isc-gate-b"));
		Mockito.when(taskService.saveTask(Mockito.any(DeviceTaskVO.class))).thenReturn("9001", "not-a-task-id");

		VisitorManualAuthReqDTO request = request(101L, 201L, Collections.singletonList(401));
		expectSmartException("设备任务保存失败：not-a-task-id", request);
		Mockito.verify(taskService, Mockito.times(2)).saveTask(Mockito.any(DeviceTaskVO.class));
		Mockito.verify(transactionManager).rollback(transactionStatus);
		Mockito.verify(transactionManager, Mockito.never()).commit(transactionStatus);
	}

	@Test
	public void submitRejectsMissingPhoto() {
		fellow.setFellowPhotoId(" ");
		VisitorManualAuthReqDTO request = request(101L, 201L, Collections.singletonList(401));

		expectSmartException("人员照片不存在，无法下发", request);
		Mockito.verifyZeroInteractions(authorityService, relationService, deviceService, taskService);
	}

	@Test
	public void optionsReturnsStringIdsAndNoVehicleOptions() {
		SmtDeviceAuthority authority = authority(401, "公共组", 0);
		Mockito.when(authorityService.list(Mockito.any())).thenReturn(Collections.singletonList(authority));
		Mockito.when(relationService.getRelationByAuthId(Mockito.anyList())).thenReturn(Collections.singletonList(relation(401, "isc-gate-a")));
		Mockito.when(deviceService.getById("isc-gate-a")).thenReturn(device("isc-gate-a"));

		VisitorManualAuthOptionsRespDTO response = service.getOptions(101L);

		assertEquals("101", response.getApplyId());
		assertEquals(DateUtils.convert(DateUtils.DEFAULT_DATE_TIME_FORMAT, apply.getStartTime().minusHours(2)), response.getStartTime());
		assertEquals(1, response.getFellows().size());
		assertEquals("201", response.getFellows().get(0).getId());
		assertEquals("测试访客", response.getFellows().get(0).getName());
		assertTrue(response.getVehicles().isEmpty());
		assertEquals(1, response.getAuthorities().size());
		assertEquals(Integer.valueOf(401), response.getAuthorities().get(0).getId());
	}

	@Test
	public void optionsOnlyReturnsCurrentApplyFellowsWithPhotos() {
		List<SmtAdmittanceFellow> candidates = new ArrayList<>();
		candidates.add(fellow);
		candidates.add(null);
		for (String photoId : Arrays.asList(null, "", " \t\r\n")) {
			SmtAdmittanceFellow withoutPhoto = new SmtAdmittanceFellow();
			withoutPhoto.setId(202L);
			withoutPhoto.setVisitorId(apply.getId());
			withoutPhoto.setFellowPhotoId(photoId);
			candidates.add(withoutPhoto);
		}
		SmtAdmittanceFellow withoutId = new SmtAdmittanceFellow();
		withoutId.setVisitorId(apply.getId());
		withoutId.setFellowPhotoId("photo-without-id");
		candidates.add(withoutId);
		SmtAdmittanceFellow anotherApply = new SmtAdmittanceFellow();
		anotherApply.setId(203L);
		anotherApply.setVisitorId(999L);
		anotherApply.setFellowPhotoId("photo-203");
		candidates.add(anotherApply);
		Mockito.when(fellowService.getByApplyId(apply.getId())).thenReturn(candidates);

		VisitorManualAuthOptionsRespDTO response = service.getOptions(apply.getId());

		assertEquals(1, response.getFellows().size());
		assertEquals("201", response.getFellows().get(0).getId());
		assertEquals("测试访客", response.getFellows().get(0).getName());
		Mockito.verifyZeroInteractions(taskService);
	}

	@Test
	public void optionsReturnsEmptyFellowsWhenAllPhotosAreMissing() {
		for (String photoId : Arrays.asList(null, "", " \t\r\n")) {
			fellow.setFellowPhotoId(photoId);

			assertTrue("无照片的人员不能成为可下发选项", service.getOptions(apply.getId()).getFellows().isEmpty());
		}
		Mockito.verifyZeroInteractions(taskService);
	}

	private void expectSmartException(String message, VisitorManualAuthReqDTO request) {
		try {
			service.submit(request);
			fail("应拒绝非法手动授权请求");
		} catch (SmartException error) {
			assertEquals(message, error.getMessage());
		}
	}

	private VisitorManualAuthReqDTO request(Long applyId, Long fellowId, List<Integer> authIds) {
		VisitorManualAuthReqDTO request = new VisitorManualAuthReqDTO();
		request.setApplyId(applyId);
		request.setFellowId(fellowId);
		request.setAuthIds(authIds);
		return request;
	}

	private SmtDeviceAuthority authority(Integer id, String name, Integer areaType) {
		SmtDeviceAuthority authority = new SmtDeviceAuthority();
		authority.setId(id);
		authority.setAuthorityName(name);
		authority.setType(1);
		authority.setParkId(7);
		authority.setAreaType(areaType);
		return authority;
	}

	private SmtDeviceAuthorityRelation relation(Integer authorityId, String deviceId) {
		SmtDeviceAuthorityRelation relation = new SmtDeviceAuthorityRelation();
		relation.setAuthorityId(authorityId);
		relation.setDeviceId(deviceId);
		relation.setParkId(7);
		return relation;
	}

	private SmtDevice device(String id) {
		SmtDevice device = new SmtDevice();
		device.setId(id);
		device.setParkId(7);
		device.setDeviceType(1);
		device.setIsSync(1);
		device.setEnableStatus(1);
		return device;
	}

	private void loginWithPark(Integer parkId) {
		SmartUser user = new SmartUser(1, 1, "tester", Collections.singletonList(parkId),
				"password", true, true, true, true, Collections.emptyList());
		SecurityContextHolder.getContext().setAuthentication(
				new UsernamePasswordAuthenticationToken(user, "password", Collections.emptyList()));
	}

	private TransactionTemplate newTransactionTemplate() {
		transactionManager = Mockito.mock(PlatformTransactionManager.class);
		transactionStatus = Mockito.mock(TransactionStatus.class);
		Mockito.when(transactionManager.getTransaction(Mockito.any())).thenReturn(transactionStatus);
		return new TransactionTemplate(transactionManager);
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
