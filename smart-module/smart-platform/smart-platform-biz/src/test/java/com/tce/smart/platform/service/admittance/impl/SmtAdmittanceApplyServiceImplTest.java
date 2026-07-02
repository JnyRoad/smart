package com.tce.smart.platform.service.admittance.impl;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import cn.hutool.core.util.StrUtil;
import com.tce.smart.common.core.constant.enums.SmtVisitorEnum;
import com.tce.smart.common.core.exception.SmartException;
import com.tce.smart.common.security.service.SmartUser;
import com.tce.smart.platform.core.dto.DeviceTaskVO;
import com.tce.smart.platform.core.dto.SearchSmtVisitorDTO;
import com.tce.smart.platform.core.dto.WorkFlowLogDTO;
import com.tce.smart.platform.core.dto.WorkFlowLogDataDTO;
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
import com.tce.smart.platform.core.service.SmtImageService;
import com.tce.smart.platform.core.service.SmtTaskDownRecordService;
import com.tce.smart.platform.core.vo.SearchSmtVisitorVO;
import com.tce.smart.platform.core.service.SmtMsgTemplateService;
import com.tce.smart.platform.service.ImageService;
import com.tce.smart.platform.service.IOAWorkflowService;
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
import com.tce.smart.tool.enums.DeviceDownStatusEnum;
import com.tce.smart.tool.enums.DeviceTaskStatusEnum;
import com.tce.smart.tool.enums.DeviceTypeEnum;
import com.tce.smart.tool.enums.OaFinalStatusEnum;
import com.tce.smart.tool.enums.OneOrZeroEnum;
import com.tce.smart.tool.enums.VisitorStatusEnum;
import com.tce.smart.tool.constant.WorkFlowLogConstants;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mockito;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionTemplate;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@SuppressWarnings({"rawtypes", "unchecked"})
public class SmtAdmittanceApplyServiceImplTest {

	@BeforeClass
	public static void initMybatisPlusLambdaCache() {
		TableInfoHelper.initTableInfo(new MapperBuilderAssistant(new MybatisConfiguration(), ""), SmtTaskDownRecord.class);
		TableInfoHelper.initTableInfo(new MapperBuilderAssistant(new MybatisConfiguration(), ""), SmtIscDownRecord.class);
		TableInfoHelper.initTableInfo(new MapperBuilderAssistant(new MybatisConfiguration(), ""), SmtDeviceTask.class);
		TableInfoHelper.initTableInfo(new MapperBuilderAssistant(new MybatisConfiguration(), ""), SmtIscDeviceTask.class);
		TableInfoHelper.initTableInfo(new MapperBuilderAssistant(new MybatisConfiguration(), ""), SmtAdmittanceApply.class);
	}

	@Test
	public void addCardKeepsAdmittanceExactEndTimeAndServiceType() throws Exception {
		SmtDeviceTaskService taskService = Mockito.mock(SmtDeviceTaskService.class);
		SmtAdmittanceApplyServiceImpl service = new SmtAdmittanceApplyServiceImpl();
		setField(service, "smtDeviceTaskService", taskService);
		setField(service, "putOffsetHour", 0);
		Mockito.when(taskService.saveTask(Mockito.any(DeviceTaskVO.class))).thenReturn("1");
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
		Mockito.when(taskService.saveTask(Mockito.any(DeviceTaskVO.class))).thenReturn("1");
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
		Mockito.when(taskService.saveTask(Mockito.any(DeviceTaskVO.class))).thenReturn("1");
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
				SmtAdmittanceApply.class, SmtAdmittanceVehicle.class, List.class, Long.class);
		addCarCard.setAccessible(true);
		addCarCard.invoke(service, apply, vehicle, Collections.singletonList("car-device-1"), 8001L);

		ArgumentCaptor<DeviceTaskVO> captor = ArgumentCaptor.forClass(DeviceTaskVO.class);
		Mockito.verify(taskService).saveTask(captor.capture());
		DeviceTaskVO task = captor.getValue();
		Assert.assertEquals(DeviceTaskConstants.CAR, task.getDeviceType());
		Assert.assertEquals(DeviceTaskConstants.CAT_ADMITTANCE, task.getServiceType());
		Assert.assertEquals(Long.valueOf(endTime.atZone(ZoneId.systemDefault()).toEpochSecond()), task.getOverTime());
		Assert.assertEquals("cert-apply", task.getApplyBadge());
	}

	@Test
	public void addCarCardTreatsUnsupportedIscVehicleTaskAsSkippedSuccess() throws Exception {
		SmtDeviceTaskService taskService = Mockito.mock(SmtDeviceTaskService.class);
		SmtAdmittanceApplyServiceImpl service = new SmtAdmittanceApplyServiceImpl();
		setField(service, "smtDeviceTaskService", taskService);
		setField(service, "putOffsetHour", 0);
		Mockito.when(taskService.saveTask(Mockito.any(DeviceTaskVO.class))).thenReturn("ISC车辆权限不支持下发");
		SmtAdmittanceApply apply = new SmtAdmittanceApply();
		apply.setStartTime(LocalDateTime.now().plusHours(1));
		apply.setEndTime(LocalDateTime.now().plusHours(3));
		apply.setCertNo("cert-apply");
		SmtAdmittanceVehicle vehicle = new SmtAdmittanceVehicle();
		vehicle.setId(4101L);
		vehicle.setPlate("粤B54321");
		Method addCarCard = SmtAdmittanceApplyServiceImpl.class.getDeclaredMethod("addCarCard",
				SmtAdmittanceApply.class, SmtAdmittanceVehicle.class, String.class);
		addCarCard.setAccessible(true);

		addCarCard.invoke(service, apply, vehicle, "isc-car-device");

		Mockito.verify(taskService).saveTask(Mockito.any(DeviceTaskVO.class));
	}

	@Test
	public void smbPutPhotoReturnsFalseWhenUploadServerDoesNotRespond() throws Exception {
		SmtAdmittanceFellowService fellowService = Mockito.mock(SmtAdmittanceFellowService.class);
		SmtImageService imageService = Mockito.mock(SmtImageService.class);
		SmtAdmittanceApplyServiceImpl service = new SmtAdmittanceApplyServiceImpl();
		setField(service, "smtAdmittanceFellowService", fellowService);
		setField(service, "smtImageService", imageService);
		setField(service, "savePath", "/tmp/");
		SmtAdmittanceFellow fellow = new SmtAdmittanceFellow();
		fellow.setFellowPhotoId("photo-timeout");
		Mockito.when(fellowService.getByApplyId(1010L)).thenReturn(Collections.singletonList(fellow));
		Mockito.when(imageService.getImageBinaryByCode("photo-timeout")).thenReturn(new byte[]{1, 2, 3});
		ExecutorService executorService = Executors.newSingleThreadExecutor();
		try (ServerSocket serverSocket = new ServerSocket(0)) {
			setField(service, "remoteUrl", "http://127.0.0.1:" + serverSocket.getLocalPort() + "/upload");
			executorService.submit(() -> {
				try (Socket ignored = serverSocket.accept()) {
					Thread.sleep(7000L);
				} catch (Exception ignored) {
				}
			});

			long startNanos = System.nanoTime();
			Boolean result = service.smbPutPhoto(1010L);
			long elapsedMillis = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNanos);

			Assert.assertFalse(result);
			Assert.assertTrue("照片上传应在超时时间附近返回，实际耗时：" + elapsedMillis, elapsedMillis < 6500L);
		} finally {
			executorService.shutdownNow();
		}
	}

	// ========== Task 3: updateStatus 解耦 + 批次提交协议 ==========

	/**
	 * 搭建 updateStatus 审批通过分支的最小依赖：
	 * 一名访客、一条闸机设备权限，驱动 addDeviceTask -> submitIscBatch 产生恰好一个下发任务。
	 */
	private UpdateStatusHarness setUpUpdateStatusHarness() throws Exception {
		SmtAdmittanceApplyMapper mapper = Mockito.mock(SmtAdmittanceApplyMapper.class);
		SmtAdmittanceFellowService fellowService = Mockito.mock(SmtAdmittanceFellowService.class);
		SmtAdmittanceVehicleService vehicleService = Mockito.mock(SmtAdmittanceVehicleService.class);
		SmtAdmittanceAreaTypeAuthService areaTypeAuthService = Mockito.mock(SmtAdmittanceAreaTypeAuthService.class);
		SmtDeviceAuthorityRelationService relationService = Mockito.mock(SmtDeviceAuthorityRelationService.class);
		SmtDeviceTaskService taskService = Mockito.mock(SmtDeviceTaskService.class);
		SmtMsgTemplateService msgTemplateService = Mockito.mock(SmtMsgTemplateService.class);

		SmtAdmittanceApplyServiceImpl service = Mockito.spy(new SmtAdmittanceApplyServiceImpl());
		setField(service, "baseMapper", mapper);
		setField(service, "smtAdmittanceFellowService", fellowService);
		setField(service, "smtAdmittanceVehicleService", vehicleService);
		setField(service, "smtAdmittanceAreaTypeAuthService", areaTypeAuthService);
		setField(service, "smtDeviceAuthorityRelationService", relationService);
		setField(service, "smtDeviceTaskService", taskService);
		setField(service, "smtMsgTemplateService", msgTemplateService);
		setField(service, "putOffsetHour", 0);
		setField(service, "transactionTemplate", newTransactionTemplate());
		setField(service, "photoPushEnabled", Boolean.TRUE);

		SmtAdmittanceApply apply = pendingPersonAdmittanceApply(3001L, "oa-photo-decouple");
		apply.setStatus(VisitorStatusEnum.Status_0.getCode());
		apply.setUnionId("");

		SmtAdmittanceFellow fellow = new SmtAdmittanceFellow();
		fellow.setId(9001L);
		fellow.setFellowName("张三");
		fellow.setFellowPhotoId("photo-9001");
		fellow.setCertNo("cert-9001");
		Mockito.when(fellowService.getByApplyId(apply.getId())).thenReturn(Collections.singletonList(fellow));
		Mockito.when(vehicleService.getByApplyId(apply.getId())).thenReturn(Collections.emptyList());

		SmtAdmittanceAreaTypeAuth auth = SmtAdmittanceAreaTypeAuth.builder().authId(9101).build();
		Mockito.when(areaTypeAuthService.getAuthByType(apply.getAreaType(), DeviceTypeEnum.DEVICE_TYPE_1.getCode(), apply.getParkId()))
				.thenReturn(Collections.singletonList(auth));
		Mockito.when(areaTypeAuthService.getAuthByType(apply.getAreaType(), DeviceTypeEnum.DEVICE_TYPE_3.getCode(), apply.getParkId()))
				.thenReturn(Collections.emptyList());
		SmtDeviceAuthorityRelation relation = new SmtDeviceAuthorityRelation();
		relation.setDeviceId("isc-device-1");
		Mockito.when(relationService.getRelationByAuthId(Mockito.anyList())).thenReturn(Collections.singletonList(relation));

		Mockito.when(taskService.saveTask(Mockito.any(DeviceTaskVO.class))).thenReturn("500001");
		Mockito.when(mapper.updateById(Mockito.any())).thenReturn(1);
		Mockito.when(mapper.update(Mockito.any(), Mockito.any())).thenReturn(1);

		UpdateStatusHarness harness = new UpdateStatusHarness();
		harness.service = service;
		harness.mapper = mapper;
		harness.taskService = taskService;
		harness.apply = apply;
		return harness;
	}

	private static final class UpdateStatusHarness {
		SmtAdmittanceApplyServiceImpl service;
		SmtAdmittanceApplyMapper mapper;
		SmtDeviceTaskService taskService;
		SmtAdmittanceApply apply;
	}

	@Test
	public void updateStatus_photoPushFailure_doesNotFailApply() throws Exception {
		UpdateStatusHarness harness = setUpUpdateStatusHarness();
		// smbPutPhoto 返回 false：模拟远程照片推送失败
		Mockito.doReturn(Boolean.FALSE).when(harness.service).smbPutPhoto(harness.apply.getId());

		harness.service.updateStatus(harness.apply);

		// 推送失败不影响下发状态，仍应置为已下发(4)，且不抛异常
		Assert.assertEquals(DeviceDownStatusEnum.ALRAEDY.getCode(), harness.apply.getDeviceStatus());
	}

	@Test
	public void updateStatus_writesSubmitBatchAtomicallyWithTasks() throws Exception {
		UpdateStatusHarness harness = setUpUpdateStatusHarness();
		Mockito.doReturn(Boolean.TRUE).when(harness.service).smbPutPhoto(harness.apply.getId());

		harness.service.updateStatus(harness.apply);

		// 断言任务创建与 isc_submit_batch 更新使用同一批次号，且顺序为：先建任务，后写批次号
		ArgumentCaptor<DeviceTaskVO> taskCaptor = ArgumentCaptor.forClass(DeviceTaskVO.class);
		Mockito.verify(harness.taskService).saveTask(taskCaptor.capture());
		Long batchIdOnTask = taskCaptor.getValue().getBatchId();
		Assert.assertNotNull("批次内任务应写入 batchId", batchIdOnTask);
		Assert.assertEquals(harness.apply.getId(), taskCaptor.getValue().getApplyId());

		ArgumentCaptor<LambdaUpdateWrapper> updateCaptor = ArgumentCaptor.forClass(LambdaUpdateWrapper.class);
		Mockito.verify(harness.mapper).update(Mockito.isNull(), updateCaptor.capture());
		updateCaptor.getValue().getSqlSegment();
		Assert.assertTrue("isc_submit_batch 更新应写入与任务一致的批次号",
				updateCaptor.getValue().getParamNameValuePairs().values().stream()
						.anyMatch(value -> batchIdOnTask.equals(value)));

		InOrder inOrder = Mockito.inOrder(harness.taskService, harness.mapper);
		inOrder.verify(harness.taskService).saveTask(Mockito.any(DeviceTaskVO.class));
		inOrder.verify(harness.mapper).update(Mockito.isNull(), Mockito.any());
	}

	@Test
	public void updateStatus_pushDisabled_skipsPhotoPush() throws Exception {
		UpdateStatusHarness harness = setUpUpdateStatusHarness();
		setField(harness.service, "photoPushEnabled", Boolean.FALSE);

		harness.service.updateStatus(harness.apply);

		// 开关关闭时不应调用照片推送
		Mockito.verify(harness.service, Mockito.never()).smbPutPhoto(Mockito.anyLong());
	}

	@Test
	public void updateStatus_taskCreationFailure_throwsAndLeavesSubmitBatchNull() throws Exception {
		UpdateStatusHarness harness = setUpUpdateStatusHarness();
		// 任务创建失败：saveTask 返回非法结果，saveRequiredDeviceTask 应抛出异常
		Mockito.when(harness.taskService.saveTask(Mockito.any(DeviceTaskVO.class))).thenReturn("设备isc-device-1不存在");

		try {
			harness.service.updateStatus(harness.apply);
			Assert.fail("Expected device task creation failure to propagate");
		} catch (IllegalStateException expected) {
			// 期望异常
		}

		// 批次未提交成功，isc_submit_batch 不应被写入
		Mockito.verify(harness.mapper, Mockito.never()).update(Mockito.isNull(), Mockito.any());
	}

	// ========== Task 5: 重新下发批次化 ==========

	private static final class RepeatAuthHarness {
		SmtAdmittanceApplyServiceImpl service;
		SmtAdmittanceApplyMapper mapper;
		SmtDeviceTaskService taskService;
		SmtIscDeviceTaskService iscTaskService;
		SmtAdmittanceApply apply;
	}

	/**
	 * 搭建 repeatVisitorDeviceAuth 人员分支的最小依赖：
	 * 一名访客、一条闸机设备权限、旧批次号 iscSubmitBatch=OLD_BATCH_ID，
	 * 不构造任何车辆权限/车辆记录，确保车辆分支不参与断言（其逻辑本次不改动）。
	 */
	private RepeatAuthHarness setUpRepeatAuthHarness(Long oldBatchId) throws Exception {
		SmtAdmittanceApplyMapper mapper = Mockito.mock(SmtAdmittanceApplyMapper.class);
		SmtAdmittanceFellowService fellowService = Mockito.mock(SmtAdmittanceFellowService.class);
		SmtAdmittanceVehicleService vehicleService = Mockito.mock(SmtAdmittanceVehicleService.class);
		SmtAdmittanceAreaTypeAuthService areaTypeAuthService = Mockito.mock(SmtAdmittanceAreaTypeAuthService.class);
		SmtDeviceAuthorityRelationService relationService = Mockito.mock(SmtDeviceAuthorityRelationService.class);
		SmtDeviceTaskService taskService = Mockito.mock(SmtDeviceTaskService.class);
		SmtIscDeviceTaskService iscTaskService = Mockito.mock(SmtIscDeviceTaskService.class);

		SmtAdmittanceApplyServiceImpl service = Mockito.spy(new SmtAdmittanceApplyServiceImpl());
		setField(service, "baseMapper", mapper);
		setField(service, "smtAdmittanceFellowService", fellowService);
		setField(service, "smtAdmittanceVehicleService", vehicleService);
		setField(service, "smtAdmittanceAreaTypeAuthService", areaTypeAuthService);
		setField(service, "smtDeviceAuthorityRelationService", relationService);
		setField(service, "smtDeviceTaskService", taskService);
		setField(service, "smtIscDeviceTaskService", iscTaskService);
		setField(service, "putOffsetHour", 0);
		setField(service, "transactionTemplate", newTransactionTemplate());

		SmtAdmittanceApply apply = pendingPersonAdmittanceApply(5001L, "oa-repeat-auth");
		apply.setStatus(VisitorStatusEnum.Status_0.getCode());
		apply.setIscSubmitBatch(oldBatchId);
		Mockito.when(mapper.selectById(apply.getId())).thenReturn(apply);

		SmtAdmittanceFellow fellow = new SmtAdmittanceFellow();
		fellow.setId(9501L);
		fellow.setFellowName("李四");
		fellow.setFellowPhotoId("photo-9501");
		fellow.setCertNo("cert-9501");
		Mockito.when(fellowService.getByApplyId(apply.getId())).thenReturn(Collections.singletonList(fellow));
		// 车辆分支不改动：本用例不构造车辆记录，车辆分支应因 vehicles 为空直接跳过
		Mockito.when(vehicleService.getByApplyId(apply.getId())).thenReturn(Collections.emptyList());

		SmtAdmittanceAreaTypeAuth auth = SmtAdmittanceAreaTypeAuth.builder().authId(9501).build();
		Mockito.when(areaTypeAuthService.getAuthByType(apply.getAreaType(), DeviceTypeEnum.DEVICE_TYPE_1.getCode(), apply.getParkId()))
				.thenReturn(Collections.singletonList(auth));
		Mockito.when(areaTypeAuthService.getAuthByType(apply.getAreaType(), DeviceTypeEnum.DEVICE_TYPE_3.getCode(), apply.getParkId()))
				.thenReturn(Collections.emptyList());
		SmtDeviceAuthorityRelation relation = new SmtDeviceAuthorityRelation();
		relation.setDeviceId("isc-device-repeat-1");
		Mockito.when(relationService.getRelationByAuthId(Mockito.anyList())).thenReturn(Collections.singletonList(relation));

		Mockito.when(taskService.saveTask(Mockito.any(DeviceTaskVO.class))).thenReturn("600001");
		Mockito.when(mapper.updateById(Mockito.any())).thenReturn(1);
		Mockito.when(mapper.update(Mockito.any(), Mockito.any())).thenReturn(1);
		Mockito.when(iscTaskService.update(Mockito.any(), Mockito.any())).thenReturn(true);

		RepeatAuthHarness harness = new RepeatAuthHarness();
		harness.service = service;
		harness.mapper = mapper;
		harness.taskService = taskService;
		harness.iscTaskService = iscTaskService;
		harness.apply = apply;
		return harness;
	}

	@Test
	public void repeatAuth_cancelsOldBatchAndCreatesNew() throws Exception {
		Long oldBatchId = 700001L;
		RepeatAuthHarness harness = setUpRepeatAuthHarness(oldBatchId);

		Boolean result = harness.service.repeatVisitorDeviceAuth(harness.apply.getId());

		Assert.assertTrue(result);
		// 旧批次非终态任务应被批量置为 CANCEL：一条 UPDATE，条件覆盖 applyId+batchId+非终态
		ArgumentCaptor<LambdaUpdateWrapper> cancelCaptor = ArgumentCaptor.forClass(LambdaUpdateWrapper.class);
		Mockito.verify(harness.iscTaskService).update(Mockito.isNull(), cancelCaptor.capture());
		String cancelSql = cancelCaptor.getValue().getSqlSegment().toLowerCase(Locale.ROOT);
		Assert.assertTrue("取消旧批次应限定 apply_id", cancelSql.contains("apply_id"));
		Assert.assertTrue("取消旧批次应限定 batch_id", cancelSql.contains("batch_id"));
		Assert.assertTrue("取消旧批次应限定非终态（排除成功/失败/取消）",
				cancelSql.contains("status") && (cancelSql.contains("in (") || cancelSql.contains("<>") || cancelSql.contains("!=")));
		Assert.assertTrue("取消旧批次应限定 deviceType=CARD，不影响车辆任务", cancelSql.contains("device_type"));
		Assert.assertTrue(cancelCaptor.getValue().getParamNameValuePairs().values().stream()
				.anyMatch(oldBatchId::equals));

		// 新批次任务生成：saveTask 被调用且带新的 batchId
		ArgumentCaptor<DeviceTaskVO> taskCaptor = ArgumentCaptor.forClass(DeviceTaskVO.class);
		Mockito.verify(harness.taskService).saveTask(taskCaptor.capture());
		Long newBatchId = taskCaptor.getValue().getBatchId();
		Assert.assertNotNull("重发应写入新批次号", newBatchId);
		Assert.assertNotEquals("新批次号不应与旧批次号相同", oldBatchId, newBatchId);
		Assert.assertEquals(harness.apply.getId(), taskCaptor.getValue().getApplyId());

		// isc_submit_batch 应更新为新批次号
		ArgumentCaptor<LambdaUpdateWrapper> submitCaptor = ArgumentCaptor.forClass(LambdaUpdateWrapper.class);
		Mockito.verify(harness.mapper).update(Mockito.isNull(), submitCaptor.capture());
		Assert.assertTrue("isc_submit_batch 更新应写入新批次号",
				submitCaptor.getValue().getParamNameValuePairs().values().stream()
						.anyMatch(newBatchId::equals));

		Assert.assertEquals("重发后应回到已下发(4)", DeviceDownStatusEnum.ALRAEDY.getCode(), harness.apply.getDeviceStatus());
	}

	@Test
	public void repeatAuth_oldTerminalTasksUntouched() throws Exception {
		// 旧批次为 NULL（历史单从未有批次）：跳过取消，直接建新批次
		RepeatAuthHarness harness = setUpRepeatAuthHarness(null);

		Boolean result = harness.service.repeatVisitorDeviceAuth(harness.apply.getId());

		Assert.assertTrue(result);
		// 无旧批次可取消，不应对 SmtIscDeviceTask 发起任何 UPDATE
		Mockito.verify(harness.iscTaskService, Mockito.never()).update(Mockito.any(), Mockito.any());
		// 新批次任务仍应正常生成
		Mockito.verify(harness.taskService).saveTask(Mockito.any(DeviceTaskVO.class));
	}

	@Test
	public void claimFailedPostApprovalHandling_excludesAppliesWithSubmittedBatch() throws Exception {
		// 加固（Task 4 评审遗留）：乐观锁 UpdateWrapper 需与两个补偿分页查询谓词对称，
		// 追加 isc_submit_batch IS NULL，防止 TOCTOU 窗口内认领到已提交批次的单
		SmtAdmittanceApplyMapper mapper = Mockito.mock(SmtAdmittanceApplyMapper.class);
		SmtAdmittanceApplyServiceImpl service = new SmtAdmittanceApplyServiceImpl();
		setField(service, "baseMapper", mapper);
		Mockito.when(mapper.update(Mockito.any(), Mockito.any())).thenReturn(1);
		SmtAdmittanceApply apply = failedPostApprovalAdmittanceApply(2500L);

		Method claimFailedPostApprovalHandling = SmtAdmittanceApplyServiceImpl.class
				.getDeclaredMethod("claimFailedPostApprovalHandling", SmtAdmittanceApply.class);
		claimFailedPostApprovalHandling.setAccessible(true);
		claimFailedPostApprovalHandling.invoke(service, apply);

		ArgumentCaptor<LambdaUpdateWrapper> updateCaptor = ArgumentCaptor.forClass(LambdaUpdateWrapper.class);
		Mockito.verify(mapper).update(Mockito.any(), updateCaptor.capture());
		String sqlSegment = updateCaptor.getValue().getSqlSegment().toLowerCase(Locale.ROOT);
		Assert.assertTrue("乐观锁认领条件应与补偿分页查询对称，排除已提交批次的单：" + sqlSegment,
				sqlSegment.contains("isc_submit_batch is null"));
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

	@Test
	public void updateOaStatusTaskQueriesLatestPendingApplyPage() throws Exception {
		SmtAdmittanceApplyMapper mapper = Mockito.mock(SmtAdmittanceApplyMapper.class);
		SmtAdmittanceApplyServiceImpl service = new SmtAdmittanceApplyServiceImpl();
		setField(service, "baseMapper", mapper);
		Mockito.when(mapper.selectPage(Mockito.any(Page.class), Mockito.any()))
				.thenReturn(emptyAdmittancePage(), emptyAdmittancePage(), emptyAdmittancePage());

		service.updateOaStatusTask();

		ArgumentCaptor<Page> pageCaptor = ArgumentCaptor.forClass(Page.class);
		ArgumentCaptor<LambdaQueryWrapper> queryCaptor = ArgumentCaptor.forClass(LambdaQueryWrapper.class);
		Mockito.verify(mapper, Mockito.times(3)).selectPage(pageCaptor.capture(), queryCaptor.capture());
		Assert.assertEquals(1L, pageCaptor.getAllValues().get(0).getCurrent());
		Assert.assertEquals(50L, pageCaptor.getAllValues().get(0).getSize());
		String latestSqlSegment = queryCaptor.getAllValues().get(0).getSqlSegment().toLowerCase(Locale.ROOT);
		Assert.assertTrue(latestSqlSegment.contains("status"));
		Assert.assertTrue(latestSqlSegment.contains("process_id is not null"));
		Assert.assertTrue(latestSqlSegment.contains("end_time"));
		Assert.assertTrue(latestSqlSegment.contains("order by"));
		Assert.assertTrue(latestSqlSegment.contains("create_time desc"));
		Assert.assertTrue(latestSqlSegment.contains("id desc"));
		assertQueryHasParam(queryCaptor.getAllValues().get(0), VisitorStatusEnum.Status_2.getCode());
		String cursorSqlSegment = queryCaptor.getAllValues().get(1).getSqlSegment().toLowerCase(Locale.ROOT);
		Assert.assertTrue(cursorSqlSegment.contains("id asc"));
		String retrySqlSegment = queryCaptor.getAllValues().get(2).getSqlSegment().toLowerCase(Locale.ROOT);
		Assert.assertTrue(retrySqlSegment.contains("device_status"));
		assertQueryHasParam(queryCaptor.getAllValues().get(2), DeviceDownStatusEnum.FAIL.getCode());
		assertQueryHasParam(queryCaptor.getAllValues().get(2), DeviceDownStatusEnum.IN_WORK.getCode());
	}

	@Test
	public void updateOaStatusTaskContinuesAfterOaQueryErrorAndApprovesNextApply() throws Exception {
		SmtAdmittanceApplyMapper mapper = Mockito.mock(SmtAdmittanceApplyMapper.class);
		IOAWorkflowService oaWorkflowService = Mockito.mock(IOAWorkflowService.class);
		SmtAdmittanceApplyServiceImpl service = Mockito.spy(new SmtAdmittanceApplyServiceImpl());
		setField(service, "baseMapper", mapper);
		setField(service, "oaWorkflowService", oaWorkflowService);
		SmtAdmittanceApply timeoutApply = pendingAdmittanceApply(2001L, "oa-timeout");
		SmtAdmittanceApply approvedApply = pendingAdmittanceApply(2002L, "oa-approved");
		Page<SmtAdmittanceApply> page = new Page<>(1, 50);
		page.setRecords(Arrays.asList(timeoutApply, approvedApply));
		Mockito.when(mapper.selectPage(Mockito.any(Page.class), Mockito.any()))
				.thenReturn(page, emptyAdmittancePage(), emptyAdmittancePage());
		Mockito.when(mapper.update(Mockito.isNull(), Mockito.any())).thenReturn(1);
		Mockito.when(oaWorkflowService.query("oa-timeout")).thenThrow(new RuntimeException("oa timeout"));
		Mockito.when(oaWorkflowService.query("oa-approved")).thenReturn(workflowLog(OaFinalStatusEnum.CAUSE_3));
		Mockito.doNothing().when(service).updateStatus(Mockito.any(SmtAdmittanceApply.class));

		service.updateOaStatusTask();

		Mockito.verify(oaWorkflowService).query("oa-timeout");
		Mockito.verify(oaWorkflowService).query("oa-approved");
		ArgumentCaptor<SmtAdmittanceApply> applyCaptor = ArgumentCaptor.forClass(SmtAdmittanceApply.class);
		Mockito.verify(service).updateStatus(applyCaptor.capture());
		Assert.assertEquals(Long.valueOf(2002L), applyCaptor.getValue().getId());
		Assert.assertEquals(VisitorStatusEnum.Status_0.getCode(), applyCaptor.getValue().getStatus());
		Assert.assertFalse(StrUtil.isBlank(applyCaptor.getValue().getSmsCode()));
	}

	@Test
	public void updateOaStatusTaskSkipsFinalStatusWhenPendingRowWasClaimedByAnotherRun() throws Exception {
		SmtAdmittanceApplyMapper mapper = Mockito.mock(SmtAdmittanceApplyMapper.class);
		IOAWorkflowService oaWorkflowService = Mockito.mock(IOAWorkflowService.class);
		SmtAdmittanceApplyServiceImpl service = Mockito.spy(new SmtAdmittanceApplyServiceImpl());
		setField(service, "baseMapper", mapper);
		setField(service, "oaWorkflowService", oaWorkflowService);
		SmtAdmittanceApply approvedApply = pendingAdmittanceApply(2003L, "oa-approved");
		Page<SmtAdmittanceApply> page = new Page<>(1, 50);
		page.setRecords(Collections.singletonList(approvedApply));
		Mockito.when(mapper.selectPage(Mockito.any(Page.class), Mockito.any()))
				.thenReturn(page, emptyAdmittancePage(), emptyAdmittancePage());
		Mockito.when(mapper.update(Mockito.isNull(), Mockito.any())).thenReturn(0);
		Mockito.when(oaWorkflowService.query("oa-approved")).thenReturn(workflowLog(OaFinalStatusEnum.CAUSE_3));
		Mockito.doNothing().when(service).updateStatus(Mockito.any(SmtAdmittanceApply.class));

		service.updateOaStatusTask();

		Mockito.verify(service, Mockito.never()).updateStatus(Mockito.any(SmtAdmittanceApply.class));
	}

	@Test
	public void updateOaStatusTaskContinuesAfterPostApprovalHandlingError() throws Exception {
		SmtAdmittanceApplyMapper mapper = Mockito.mock(SmtAdmittanceApplyMapper.class);
		IOAWorkflowService oaWorkflowService = Mockito.mock(IOAWorkflowService.class);
		SmtAdmittanceApplyServiceImpl service = Mockito.spy(new SmtAdmittanceApplyServiceImpl());
		setField(service, "baseMapper", mapper);
		setField(service, "oaWorkflowService", oaWorkflowService);
		SmtAdmittanceApply firstApply = pendingAdmittanceApply(2004L, "oa-approved-1");
		SmtAdmittanceApply secondApply = pendingAdmittanceApply(2005L, "oa-approved-2");
		Page<SmtAdmittanceApply> page = new Page<>(1, 50);
		page.setRecords(Arrays.asList(firstApply, secondApply));
		Mockito.when(mapper.selectPage(Mockito.any(Page.class), Mockito.any()))
				.thenReturn(page, emptyAdmittancePage(), emptyAdmittancePage());
		Mockito.when(mapper.update(Mockito.isNull(), Mockito.any())).thenReturn(1);
		Mockito.when(oaWorkflowService.query(Mockito.anyString())).thenReturn(workflowLog(OaFinalStatusEnum.CAUSE_3));
		Mockito.doThrow(new RuntimeException("device task failed"))
				.doNothing()
				.when(service).updateStatus(Mockito.any(SmtAdmittanceApply.class));

		service.updateOaStatusTask();

		Mockito.verify(service, Mockito.times(2)).updateStatus(Mockito.any(SmtAdmittanceApply.class));
	}

	@Test
	public void updateOaStatusTaskProcessesCursorPageWhenLatestPageIsStillPending() throws Exception {
		SmtAdmittanceApplyMapper mapper = Mockito.mock(SmtAdmittanceApplyMapper.class);
		IOAWorkflowService oaWorkflowService = Mockito.mock(IOAWorkflowService.class);
		SmtAdmittanceApplyServiceImpl service = Mockito.spy(new SmtAdmittanceApplyServiceImpl());
		setField(service, "baseMapper", mapper);
		setField(service, "oaWorkflowService", oaWorkflowService);
		SmtAdmittanceApply latestPendingApply = pendingAdmittanceApply(2006L, "oa-still-pending");
		SmtAdmittanceApply cursorApprovedApply = pendingAdmittanceApply(2007L, "oa-cursor-approved");
		Page<SmtAdmittanceApply> latestPage = new Page<>(1, 50);
		latestPage.setRecords(Collections.singletonList(latestPendingApply));
		Page<SmtAdmittanceApply> cursorPage = new Page<>(1, 50);
		cursorPage.setRecords(Collections.singletonList(cursorApprovedApply));
		Mockito.when(mapper.selectPage(Mockito.any(Page.class), Mockito.any()))
				.thenReturn(latestPage, cursorPage, emptyAdmittancePage(), emptyAdmittancePage());
		Mockito.when(mapper.update(Mockito.isNull(), Mockito.any())).thenReturn(1);
		Mockito.when(oaWorkflowService.query("oa-still-pending")).thenReturn(workflowLog("2"));
		Mockito.when(oaWorkflowService.query("oa-cursor-approved")).thenReturn(workflowLog(OaFinalStatusEnum.CAUSE_3));
		Mockito.doNothing().when(service).updateStatus(Mockito.any(SmtAdmittanceApply.class));

		service.updateOaStatusTask();

		ArgumentCaptor<SmtAdmittanceApply> applyCaptor = ArgumentCaptor.forClass(SmtAdmittanceApply.class);
		Mockito.verify(service).updateStatus(applyCaptor.capture());
		Assert.assertEquals(Long.valueOf(2007L), applyCaptor.getValue().getId());
	}

	@Test
	public void updateOaStatusTaskProcessesMiddleCursorPageWhenBothEdgesAreStillPending() throws Exception {
		SmtAdmittanceApplyMapper mapper = Mockito.mock(SmtAdmittanceApplyMapper.class);
		IOAWorkflowService oaWorkflowService = Mockito.mock(IOAWorkflowService.class);
		SmtAdmittanceApplyServiceImpl service = Mockito.spy(new SmtAdmittanceApplyServiceImpl());
		setField(service, "baseMapper", mapper);
		setField(service, "oaWorkflowService", oaWorkflowService);
		SmtAdmittanceApply latestPendingApply = pendingAdmittanceApply(2012L, "oa-new-pending");
		SmtAdmittanceApply middleApprovedApply = pendingAdmittanceApply(2013L, "oa-middle-approved");
		SmtAdmittanceApply oldestPendingApply = pendingAdmittanceApply(2014L, "oa-old-pending");
		Page<SmtAdmittanceApply> latestPage = new Page<>(1, 50);
		latestPage.setRecords(Collections.singletonList(latestPendingApply));
		Page<SmtAdmittanceApply> middlePage = new Page<>(1, 50);
		middlePage.setRecords(Collections.singletonList(middleApprovedApply));
		Page<SmtAdmittanceApply> oldestPage = new Page<>(1, 50);
		oldestPage.setRecords(Collections.singletonList(oldestPendingApply));
		Mockito.when(mapper.selectPage(Mockito.any(Page.class), Mockito.any()))
				.thenReturn(latestPage, oldestPage, middlePage, emptyAdmittancePage());
		Mockito.when(mapper.update(Mockito.isNull(), Mockito.any())).thenReturn(1);
		Mockito.when(oaWorkflowService.query("oa-new-pending")).thenReturn(workflowLog("2"));
		Mockito.when(oaWorkflowService.query("oa-old-pending")).thenReturn(workflowLog("2"));
		Mockito.when(oaWorkflowService.query("oa-middle-approved")).thenReturn(workflowLog(OaFinalStatusEnum.CAUSE_3));
		Mockito.doNothing().when(service).updateStatus(Mockito.any(SmtAdmittanceApply.class));

		service.updateOaStatusTask();

		Mockito.verify(oaWorkflowService).query("oa-middle-approved");
	}

	@Test
	public void updateOaStatusTaskAdvancesCursorAcrossRuns() throws Exception {
		SmtAdmittanceApplyMapper mapper = Mockito.mock(SmtAdmittanceApplyMapper.class);
		IOAWorkflowService oaWorkflowService = Mockito.mock(IOAWorkflowService.class);
		SmtAdmittanceApplyServiceImpl service = new SmtAdmittanceApplyServiceImpl();
		setField(service, "baseMapper", mapper);
		setField(service, "oaWorkflowService", oaWorkflowService);
		Page<SmtAdmittanceApply> oldestPage = new Page<>(1, 50);
		oldestPage.setRecords(Collections.singletonList(pendingAdmittanceApply(100L, "oa-old-pending")));
		Page<SmtAdmittanceApply> firstCursorPage = new Page<>(1, 50);
		firstCursorPage.setRecords(Collections.singletonList(pendingAdmittanceApply(200L, "oa-first-cursor-pending")));
		Page<SmtAdmittanceApply> secondCursorPage = new Page<>(1, 50);
		secondCursorPage.setRecords(Collections.singletonList(pendingAdmittanceApply(300L, "oa-second-cursor-pending")));
		Mockito.when(mapper.selectPage(Mockito.any(Page.class), Mockito.any()))
				.thenReturn(emptyAdmittancePage(), oldestPage, firstCursorPage, emptyAdmittancePage(),
						emptyAdmittancePage(), oldestPage, secondCursorPage, emptyAdmittancePage());
		Mockito.when(oaWorkflowService.query(Mockito.anyString())).thenReturn(workflowLog("2"));

		service.updateOaStatusTask();
		service.updateOaStatusTask();

		ArgumentCaptor<LambdaQueryWrapper> queryCaptor = ArgumentCaptor.forClass(LambdaQueryWrapper.class);
		Mockito.verify(mapper, Mockito.atLeast(8)).selectPage(Mockito.any(Page.class), queryCaptor.capture());
		Assert.assertTrue(queryCaptor.getAllValues().stream()
				.anyMatch(query -> query.getSqlSegment().toLowerCase(Locale.ROOT).contains("id >")
							&& queryHasParam(query, 200L)));
	}

	@Test
	public void updateOaStatusTaskRechecksRecentlyScannedPendingApplyOnNextRun() throws Exception {
		SmtAdmittanceApplyMapper mapper = Mockito.mock(SmtAdmittanceApplyMapper.class);
		IOAWorkflowService oaWorkflowService = Mockito.mock(IOAWorkflowService.class);
		SmtAdmittanceApplyServiceImpl service = Mockito.spy(new SmtAdmittanceApplyServiceImpl());
		setField(service, "baseMapper", mapper);
		setField(service, "oaWorkflowService", oaWorkflowService);
		SmtAdmittanceApply oldestPendingApply = pendingAdmittanceApply(2100L, "oa-old-still-pending");
		SmtAdmittanceApply recentlyScannedApply = pendingAdmittanceApply(2200L, "oa-recently-scanned");
		Page<SmtAdmittanceApply> oldestPage = new Page<>(1, 50);
		oldestPage.setRecords(Collections.singletonList(oldestPendingApply));
		Page<SmtAdmittanceApply> cursorPage = new Page<>(1, 50);
		cursorPage.setRecords(Collections.singletonList(recentlyScannedApply));
		Page<SmtAdmittanceApply> recheckPage = new Page<>(1, 50);
		recheckPage.setRecords(Collections.singletonList(recentlyScannedApply));
		Mockito.when(mapper.selectPage(Mockito.any(Page.class), Mockito.any()))
				.thenReturn(emptyAdmittancePage(), oldestPage, cursorPage, emptyAdmittancePage(),
						emptyAdmittancePage(), emptyAdmittancePage(), oldestPage, recheckPage,
						emptyAdmittancePage(), emptyAdmittancePage(), emptyAdmittancePage());
		Mockito.when(mapper.update(Mockito.isNull(), Mockito.any())).thenReturn(1);
		Mockito.when(oaWorkflowService.query("oa-old-still-pending")).thenReturn(workflowLog("2"));
		Mockito.when(oaWorkflowService.query("oa-recently-scanned"))
				.thenReturn(workflowLog("2"))
				.thenReturn(workflowLog(OaFinalStatusEnum.CAUSE_3));
		Mockito.doNothing().when(service).updateStatus(Mockito.any(SmtAdmittanceApply.class));

		service.updateOaStatusTask();
		service.updateOaStatusTask();

		ArgumentCaptor<SmtAdmittanceApply> applyCaptor = ArgumentCaptor.forClass(SmtAdmittanceApply.class);
		Mockito.verify(service).updateStatus(applyCaptor.capture());
		Assert.assertEquals(Long.valueOf(2200L), applyCaptor.getValue().getId());
	}

	@Test
	public void updateOaStatusTaskRechecksPendingApplyAcrossPlatformInstancesWithRedis() throws Exception {
		Map<String, String> redisValues = new HashMap<>();
		Set<String> redisRecheckIds = new LinkedHashSet<>();
		StringRedisTemplate redisTemplate = sharedRedisTemplate(redisValues, redisRecheckIds);
		SmtAdmittanceApplyMapper firstMapper = Mockito.mock(SmtAdmittanceApplyMapper.class);
		SmtAdmittanceApplyMapper secondMapper = Mockito.mock(SmtAdmittanceApplyMapper.class);
		IOAWorkflowService firstOaWorkflowService = Mockito.mock(IOAWorkflowService.class);
		IOAWorkflowService secondOaWorkflowService = Mockito.mock(IOAWorkflowService.class);
		SmtAdmittanceApplyServiceImpl firstService = Mockito.spy(new SmtAdmittanceApplyServiceImpl());
		SmtAdmittanceApplyServiceImpl secondService = Mockito.spy(new SmtAdmittanceApplyServiceImpl());
		setField(firstService, "baseMapper", firstMapper);
		setField(firstService, "oaWorkflowService", firstOaWorkflowService);
		setField(firstService, "stringRedisTemplate", redisTemplate);
		setField(secondService, "baseMapper", secondMapper);
		setField(secondService, "oaWorkflowService", secondOaWorkflowService);
		setField(secondService, "stringRedisTemplate", redisTemplate);
		SmtAdmittanceApply recentlyScannedApply = pendingAdmittanceApply(2210L, "oa-shared-recheck");
		Page<SmtAdmittanceApply> firstLatestPage = new Page<>(1, 50);
		firstLatestPage.setRecords(Collections.singletonList(recentlyScannedApply));
		Page<SmtAdmittanceApply> secondRecheckPage = new Page<>(1, 50);
		secondRecheckPage.setRecords(Collections.singletonList(recentlyScannedApply));
		Mockito.when(firstMapper.selectPage(Mockito.any(Page.class), Mockito.any()))
				.thenReturn(firstLatestPage, emptyAdmittancePage(), emptyAdmittancePage());
		Mockito.when(secondMapper.selectPage(Mockito.any(Page.class), Mockito.any()))
				.thenReturn(emptyAdmittancePage(), emptyAdmittancePage(), secondRecheckPage, emptyAdmittancePage());
		Mockito.when(secondMapper.update(Mockito.isNull(), Mockito.any())).thenReturn(1);
		Mockito.when(firstOaWorkflowService.query("oa-shared-recheck")).thenReturn(workflowLog("2"));
		Mockito.when(secondOaWorkflowService.query("oa-shared-recheck")).thenReturn(workflowLog(OaFinalStatusEnum.CAUSE_3));
		Mockito.doNothing().when(secondService).updateStatus(Mockito.any(SmtAdmittanceApply.class));

		firstService.updateOaStatusTask();
		secondService.updateOaStatusTask();

		ArgumentCaptor<SmtAdmittanceApply> applyCaptor = ArgumentCaptor.forClass(SmtAdmittanceApply.class);
		Mockito.verify(secondService).updateStatus(applyCaptor.capture());
		Assert.assertEquals(Long.valueOf(2210L), applyCaptor.getValue().getId());
	}

	@Test
	public void updateOaStatusTaskRetriesFailedPostApprovalHandling() throws Exception {
		SmtAdmittanceApplyMapper mapper = Mockito.mock(SmtAdmittanceApplyMapper.class);
		SmtAdmittanceApplyServiceImpl service = Mockito.spy(new SmtAdmittanceApplyServiceImpl());
		setField(service, "baseMapper", mapper);
		SmtAdmittanceApply failedApply = failedPostApprovalAdmittanceApply(2008L);
		Page<SmtAdmittanceApply> retryPage = new Page<>(1, 50);
		retryPage.setRecords(Collections.singletonList(failedApply));
		Mockito.when(mapper.selectPage(Mockito.any(Page.class), Mockito.any()))
				.thenReturn(emptyAdmittancePage(), emptyAdmittancePage(), retryPage);
		Mockito.when(mapper.update(Mockito.isNull(), Mockito.any())).thenReturn(1);
		Mockito.doNothing().when(service).updateStatus(Mockito.any(SmtAdmittanceApply.class));

		service.updateOaStatusTask();

		ArgumentCaptor<SmtAdmittanceApply> applyCaptor = ArgumentCaptor.forClass(SmtAdmittanceApply.class);
		Mockito.verify(service).updateStatus(applyCaptor.capture());
		Assert.assertEquals(Long.valueOf(2008L), applyCaptor.getValue().getId());
	}

	@Test
	public void compensation_skipsAppliesWithSubmittedBatch() throws Exception {
		// 聚合产生的真失败单必有批次号，只走人工重新下发（spec §3.4 补偿边界）
		// 本用例验证补偿分页查询显式排除已提交批次（isc_submit_batch 非空）的单据，
		// 避免把“已提交但仍待终态确认”的单误判为需要补偿重试的失败单。
		SmtAdmittanceApplyMapper mapper = Mockito.mock(SmtAdmittanceApplyMapper.class);
		SmtAdmittanceApplyServiceImpl service = Mockito.spy(new SmtAdmittanceApplyServiceImpl());
		setField(service, "baseMapper", mapper);
		Mockito.when(mapper.selectPage(Mockito.any(Page.class), Mockito.any())).thenReturn(emptyAdmittancePage());

		Method failedPostApprovalPage = SmtAdmittanceApplyServiceImpl.class
				.getDeclaredMethod("failedPostApprovalPage");
		failedPostApprovalPage.setAccessible(true);
		Method failedPostApprovalCursorPage = SmtAdmittanceApplyServiceImpl.class
				.getDeclaredMethod("failedPostApprovalCursorPage");
		failedPostApprovalCursorPage.setAccessible(true);

		failedPostApprovalPage.invoke(service);
		failedPostApprovalCursorPage.invoke(service);

		ArgumentCaptor<LambdaQueryWrapper> queryCaptor = ArgumentCaptor.forClass(LambdaQueryWrapper.class);
		Mockito.verify(mapper, Mockito.times(2)).selectPage(Mockito.any(Page.class), queryCaptor.capture());
		for (LambdaQueryWrapper queryWrapper : queryCaptor.getAllValues()) {
			String sqlSegment = queryWrapper.getSqlSegment().toLowerCase(Locale.ROOT);
			Assert.assertTrue("补偿分页查询必须排除已提交批次的单据（isc_submit_batch IS NULL）：" + sqlSegment,
					sqlSegment.contains("isc_submit_batch is null"));
		}
	}

	@Test
	public void retryFailedPostApprovalHandlingWalksCursorWhenOldestFailedApplyKeepsFailing() throws Exception {
		SmtAdmittanceApplyMapper mapper = Mockito.mock(SmtAdmittanceApplyMapper.class);
		SmtAdmittanceApplyServiceImpl service = Mockito.spy(new SmtAdmittanceApplyServiceImpl());
		setField(service, "baseMapper", mapper);
		SmtAdmittanceApply oldestFailedApply = failedPostApprovalAdmittanceApply(2300L);
		SmtAdmittanceApply laterFailedApply = failedPostApprovalAdmittanceApply(2400L);
		Page<SmtAdmittanceApply> oldestPage = new Page<>(1, 50);
		oldestPage.setRecords(Collections.singletonList(oldestFailedApply));
		Page<SmtAdmittanceApply> laterPage = new Page<>(1, 50);
		laterPage.setRecords(Collections.singletonList(laterFailedApply));
		Mockito.when(mapper.selectPage(Mockito.any(Page.class), Mockito.any()))
				.thenReturn(oldestPage, oldestPage, oldestPage, laterPage);
		Mockito.when(mapper.update(Mockito.isNull(), Mockito.any())).thenReturn(1);
		Mockito.doThrow(new RuntimeException("old failed apply is still blocked"))
				.doThrow(new RuntimeException("old failed apply is still blocked"))
				.doNothing()
				.when(service).updateStatus(Mockito.any(SmtAdmittanceApply.class));
		Method retryFailedPostApprovalHandling = SmtAdmittanceApplyServiceImpl.class
				.getDeclaredMethod("retryFailedPostApprovalHandling");
		retryFailedPostApprovalHandling.setAccessible(true);

		retryFailedPostApprovalHandling.invoke(service);
		retryFailedPostApprovalHandling.invoke(service);

		ArgumentCaptor<SmtAdmittanceApply> applyCaptor = ArgumentCaptor.forClass(SmtAdmittanceApply.class);
		Mockito.verify(service, Mockito.times(3)).updateStatus(applyCaptor.capture());
		Assert.assertTrue(applyCaptor.getAllValues().stream()
				.anyMatch(apply -> Long.valueOf(2400L).equals(apply.getId())));
	}

	@Test
	public void retryFailedPostApprovalHandlingSharesCursorAcrossPlatformInstancesWithRedis() throws Exception {
		Map<String, String> redisValues = new HashMap<>();
		Set<String> redisRecheckIds = new LinkedHashSet<>();
		StringRedisTemplate redisTemplate = sharedRedisTemplate(redisValues, redisRecheckIds);
		SmtAdmittanceApplyMapper firstMapper = Mockito.mock(SmtAdmittanceApplyMapper.class);
		SmtAdmittanceApplyMapper secondMapper = Mockito.mock(SmtAdmittanceApplyMapper.class);
		SmtAdmittanceApplyServiceImpl firstService = Mockito.spy(new SmtAdmittanceApplyServiceImpl());
		SmtAdmittanceApplyServiceImpl secondService = Mockito.spy(new SmtAdmittanceApplyServiceImpl());
		setField(firstService, "baseMapper", firstMapper);
		setField(firstService, "stringRedisTemplate", redisTemplate);
		setField(secondService, "baseMapper", secondMapper);
		setField(secondService, "stringRedisTemplate", redisTemplate);
		SmtAdmittanceApply oldestFailedApply = failedPostApprovalAdmittanceApply(2310L);
		SmtAdmittanceApply laterFailedApply = failedPostApprovalAdmittanceApply(2410L);
		Page<SmtAdmittanceApply> oldestPage = new Page<>(1, 50);
		oldestPage.setRecords(Collections.singletonList(oldestFailedApply));
		Page<SmtAdmittanceApply> laterPage = new Page<>(1, 50);
		laterPage.setRecords(Collections.singletonList(laterFailedApply));
		Mockito.when(firstMapper.selectPage(Mockito.any(Page.class), Mockito.any()))
				.thenReturn(oldestPage, oldestPage);
		Mockito.when(secondMapper.selectPage(Mockito.any(Page.class), Mockito.any()))
				.thenReturn(oldestPage, laterPage);
		Mockito.when(firstMapper.update(Mockito.isNull(), Mockito.any())).thenReturn(1);
		Mockito.when(secondMapper.update(Mockito.isNull(), Mockito.any())).thenReturn(1);
		Mockito.doThrow(new RuntimeException("old failed apply is still blocked"))
				.when(firstService).updateStatus(Mockito.any(SmtAdmittanceApply.class));
		Mockito.doThrow(new RuntimeException("old failed apply is still blocked"))
				.doNothing()
				.when(secondService).updateStatus(Mockito.any(SmtAdmittanceApply.class));
		Method retryFailedPostApprovalHandling = SmtAdmittanceApplyServiceImpl.class
				.getDeclaredMethod("retryFailedPostApprovalHandling");
		retryFailedPostApprovalHandling.setAccessible(true);

		retryFailedPostApprovalHandling.invoke(firstService);
		retryFailedPostApprovalHandling.invoke(secondService);

		ArgumentCaptor<SmtAdmittanceApply> applyCaptor = ArgumentCaptor.forClass(SmtAdmittanceApply.class);
		Mockito.verify(secondService, Mockito.times(2)).updateStatus(applyCaptor.capture());
		Assert.assertTrue(applyCaptor.getAllValues().stream()
				.anyMatch(apply -> Long.valueOf(2410L).equals(apply.getId())));
		ArgumentCaptor<LambdaQueryWrapper> queryCaptor = ArgumentCaptor.forClass(LambdaQueryWrapper.class);
		Mockito.verify(secondMapper, Mockito.atLeast(2)).selectPage(Mockito.any(Page.class), queryCaptor.capture());
		Assert.assertTrue(queryCaptor.getAllValues().stream()
				.anyMatch(query -> query.getSqlSegment().toLowerCase(Locale.ROOT).contains("id >")
						&& queryHasParam(query, 2310L)));
	}

	@Test
	public void updateOaStatusTaskRetriesInWorkPostApprovalHandling() throws Exception {
		SmtAdmittanceApplyMapper mapper = Mockito.mock(SmtAdmittanceApplyMapper.class);
		SmtAdmittanceApplyServiceImpl service = Mockito.spy(new SmtAdmittanceApplyServiceImpl());
		setField(service, "baseMapper", mapper);
		SmtAdmittanceApply inWorkApply = inWorkPostApprovalAdmittanceApply(2011L);
		Page<SmtAdmittanceApply> retryPage = new Page<>(1, 50);
		retryPage.setRecords(Collections.singletonList(inWorkApply));
		Mockito.when(mapper.selectPage(Mockito.any(Page.class), Mockito.any()))
				.thenReturn(emptyAdmittancePage(), emptyAdmittancePage(), retryPage);
		Mockito.when(mapper.update(Mockito.isNull(), Mockito.any())).thenReturn(1);
		Mockito.doNothing().when(service).updateStatus(Mockito.any(SmtAdmittanceApply.class));

		service.updateOaStatusTask();

		ArgumentCaptor<SmtAdmittanceApply> applyCaptor = ArgumentCaptor.forClass(SmtAdmittanceApply.class);
		Mockito.verify(service).updateStatus(applyCaptor.capture());
		Assert.assertEquals(Long.valueOf(2011L), applyCaptor.getValue().getId());
		Assert.assertTrue(hasUpdateParam(mapper, DeviceDownStatusEnum.IN_WORK.getCode()));
	}

	@Test
	public void updateOaStatusTaskClaimsApprovedPersonAsInWorkBeforePostApprovalHandling() throws Exception {
		SmtAdmittanceApplyMapper mapper = Mockito.mock(SmtAdmittanceApplyMapper.class);
		IOAWorkflowService oaWorkflowService = Mockito.mock(IOAWorkflowService.class);
		SmtAdmittanceApplyServiceImpl service = Mockito.spy(new SmtAdmittanceApplyServiceImpl());
		setField(service, "baseMapper", mapper);
		setField(service, "oaWorkflowService", oaWorkflowService);
		SmtAdmittanceApply approvedApply = pendingPersonAdmittanceApply(2018L, "oa-approved-in-work");
		Page<SmtAdmittanceApply> page = new Page<>(1, 50);
		page.setRecords(Collections.singletonList(approvedApply));
		Mockito.when(mapper.selectPage(Mockito.any(Page.class), Mockito.any()))
				.thenReturn(page, emptyAdmittancePage(), emptyAdmittancePage());
		Mockito.when(mapper.update(Mockito.isNull(), Mockito.any())).thenReturn(1);
		Mockito.when(oaWorkflowService.query("oa-approved-in-work")).thenReturn(workflowLog(OaFinalStatusEnum.CAUSE_3));
		Mockito.doNothing().when(service).updateStatus(Mockito.any(SmtAdmittanceApply.class));

		service.updateOaStatusTask();

		Assert.assertTrue(hasUpdateParam(mapper, DeviceDownStatusEnum.IN_WORK.getCode()));
		Assert.assertFalse(hasUpdateParam(mapper, DeviceDownStatusEnum.ALRAEDY.getCode()));
		ArgumentCaptor<SmtAdmittanceApply> applyCaptor = ArgumentCaptor.forClass(SmtAdmittanceApply.class);
		Mockito.verify(service).updateStatus(applyCaptor.capture());
		Assert.assertEquals(DeviceDownStatusEnum.IN_WORK.getCode(), applyCaptor.getValue().getDeviceStatus());
	}

	@Test
	public void updateOaStatusTaskSkipsPostApprovalRetryWhenRedisLockIsHeld() throws Exception {
		SmtAdmittanceApplyMapper mapper = Mockito.mock(SmtAdmittanceApplyMapper.class);
		StringRedisTemplate redisTemplate = Mockito.mock(StringRedisTemplate.class);
		ValueOperations<String, String> valueOperations = Mockito.mock(ValueOperations.class);
		SmtAdmittanceApplyServiceImpl service = Mockito.spy(new SmtAdmittanceApplyServiceImpl());
		setField(service, "baseMapper", mapper);
		setField(service, "stringRedisTemplate", redisTemplate);
		SmtAdmittanceApply inWorkApply = inWorkPostApprovalAdmittanceApply(2016L);
		Page<SmtAdmittanceApply> retryPage = new Page<>(1, 50);
		retryPage.setRecords(Collections.singletonList(inWorkApply));
		Mockito.when(mapper.selectPage(Mockito.any(Page.class), Mockito.any()))
				.thenReturn(emptyAdmittancePage(), emptyAdmittancePage(), retryPage);
		Mockito.when(redisTemplate.opsForValue()).thenReturn(valueOperations);
		Mockito.when(valueOperations.setIfAbsent(Mockito.anyString(), Mockito.anyString(), Mockito.anyLong(), Mockito.eq(TimeUnit.MINUTES)))
				.thenReturn(Boolean.FALSE);

		service.updateOaStatusTask();

		Mockito.verify(service, Mockito.never()).updateStatus(Mockito.any(SmtAdmittanceApply.class));
	}

	@Test
	public void updateOaStatusTaskReleasesPostApprovalRetryLockWithAtomicScript() throws Exception {
		SmtAdmittanceApplyMapper mapper = Mockito.mock(SmtAdmittanceApplyMapper.class);
		StringRedisTemplate redisTemplate = Mockito.mock(StringRedisTemplate.class);
		ValueOperations<String, String> valueOperations = Mockito.mock(ValueOperations.class);
		SmtAdmittanceApplyServiceImpl service = Mockito.spy(new SmtAdmittanceApplyServiceImpl());
		setField(service, "baseMapper", mapper);
		setField(service, "stringRedisTemplate", redisTemplate);
		SmtAdmittanceApply inWorkApply = inWorkPostApprovalAdmittanceApply(2017L);
		Page<SmtAdmittanceApply> retryPage = new Page<>(1, 50);
		retryPage.setRecords(Collections.singletonList(inWorkApply));
		Mockito.when(mapper.selectPage(Mockito.any(Page.class), Mockito.any()))
				.thenReturn(emptyAdmittancePage(), emptyAdmittancePage(), retryPage);
		Mockito.when(mapper.update(Mockito.isNull(), Mockito.any())).thenReturn(1);
		Mockito.when(redisTemplate.opsForValue()).thenReturn(valueOperations);
		Mockito.when(valueOperations.setIfAbsent(Mockito.anyString(), Mockito.anyString(), Mockito.anyLong(), Mockito.eq(TimeUnit.MINUTES)))
				.thenReturn(Boolean.TRUE);
		Mockito.doNothing().when(service).updateStatus(Mockito.any(SmtAdmittanceApply.class));

		service.updateOaStatusTask();

		Mockito.verify(service).updateStatus(Mockito.any(SmtAdmittanceApply.class));
		Mockito.verify(redisTemplate).execute(Mockito.any(DefaultRedisScript.class),
				Mockito.anyList(), Mockito.anyString());
		Mockito.verify(redisTemplate, Mockito.never()).delete(Mockito.anyString());
	}

	@Test
	public void updateOaStatusTaskContinuesWhenPostApprovalRetryLockReleaseFails() throws Exception {
		SmtAdmittanceApplyMapper mapper = Mockito.mock(SmtAdmittanceApplyMapper.class);
		StringRedisTemplate redisTemplate = Mockito.mock(StringRedisTemplate.class);
		ValueOperations<String, String> valueOperations = Mockito.mock(ValueOperations.class);
		SmtAdmittanceApplyServiceImpl service = Mockito.spy(new SmtAdmittanceApplyServiceImpl());
		setField(service, "baseMapper", mapper);
		setField(service, "stringRedisTemplate", redisTemplate);
		SmtAdmittanceApply inWorkApply = inWorkPostApprovalAdmittanceApply(2018L);
		Page<SmtAdmittanceApply> retryPage = new Page<>(1, 50);
		retryPage.setRecords(Collections.singletonList(inWorkApply));
		Mockito.when(mapper.selectPage(Mockito.any(Page.class), Mockito.any()))
				.thenReturn(emptyAdmittancePage(), emptyAdmittancePage(), retryPage);
		Mockito.when(mapper.update(Mockito.isNull(), Mockito.any())).thenReturn(1);
		Mockito.when(redisTemplate.opsForValue()).thenReturn(valueOperations);
		Mockito.when(valueOperations.setIfAbsent(Mockito.anyString(), Mockito.anyString(), Mockito.anyLong(), Mockito.eq(TimeUnit.MINUTES)))
				.thenReturn(Boolean.TRUE);
		Mockito.when(redisTemplate.execute(Mockito.any(DefaultRedisScript.class), Mockito.anyList(), Mockito.anyString()))
				.thenThrow(new RuntimeException("redis down"));
		Mockito.doNothing().when(service).updateStatus(Mockito.any(SmtAdmittanceApply.class));

		service.updateOaStatusTask();

		Mockito.verify(service).updateStatus(Mockito.any(SmtAdmittanceApply.class));
		Mockito.verify(redisTemplate, Mockito.times(3)).execute(Mockito.any(DefaultRedisScript.class),
				Mockito.anyList(), Mockito.anyString());
	}

	@Test
	public void updateOaStatusTaskContinuesNextPostApprovalRetryWhenRedisLockAcquireFails() throws Exception {
		SmtAdmittanceApplyMapper mapper = Mockito.mock(SmtAdmittanceApplyMapper.class);
		StringRedisTemplate redisTemplate = Mockito.mock(StringRedisTemplate.class);
		ValueOperations<String, String> valueOperations = Mockito.mock(ValueOperations.class);
		SmtAdmittanceApplyServiceImpl service = Mockito.spy(new SmtAdmittanceApplyServiceImpl());
		setField(service, "baseMapper", mapper);
		setField(service, "stringRedisTemplate", redisTemplate);
		SmtAdmittanceApply firstApply = inWorkPostApprovalAdmittanceApply(2019L);
		SmtAdmittanceApply secondApply = inWorkPostApprovalAdmittanceApply(2020L);
		Page<SmtAdmittanceApply> retryPage = new Page<>(1, 50);
		retryPage.setRecords(Arrays.asList(firstApply, secondApply));
		Mockito.when(mapper.selectPage(Mockito.any(Page.class), Mockito.any()))
				.thenReturn(emptyAdmittancePage(), emptyAdmittancePage(), retryPage);
		Mockito.when(mapper.update(Mockito.isNull(), Mockito.any())).thenReturn(1);
		Mockito.when(redisTemplate.opsForValue()).thenReturn(valueOperations);
		Mockito.when(valueOperations.setIfAbsent(Mockito.anyString(), Mockito.anyString(), Mockito.anyLong(), Mockito.eq(TimeUnit.MINUTES)))
				.thenThrow(new RuntimeException("redis down"))
				.thenReturn(Boolean.TRUE);
		Mockito.doNothing().when(service).updateStatus(Mockito.any(SmtAdmittanceApply.class));

		service.updateOaStatusTask();

		ArgumentCaptor<SmtAdmittanceApply> applyCaptor = ArgumentCaptor.forClass(SmtAdmittanceApply.class);
		Mockito.verify(service).updateStatus(applyCaptor.capture());
		Assert.assertEquals(Long.valueOf(2020L), applyCaptor.getValue().getId());
	}

	@Test
	public void updateOaStatusTaskMarksFailedWhenDeviceTaskReturnsError() throws Exception {
		SmtAdmittanceApplyMapper mapper = Mockito.mock(SmtAdmittanceApplyMapper.class);
		IOAWorkflowService oaWorkflowService = Mockito.mock(IOAWorkflowService.class);
		SmtDeviceTaskService taskService = Mockito.mock(SmtDeviceTaskService.class);
		SmtAdmittanceAreaTypeAuthService areaTypeAuthService = Mockito.mock(SmtAdmittanceAreaTypeAuthService.class);
		SmtDeviceAuthorityRelationService relationService = Mockito.mock(SmtDeviceAuthorityRelationService.class);
		SmtAdmittanceFellowService fellowService = Mockito.mock(SmtAdmittanceFellowService.class);
		SmtAdmittanceApplyServiceImpl service = Mockito.spy(new SmtAdmittanceApplyServiceImpl());
		setField(service, "baseMapper", mapper);
		setField(service, "oaWorkflowService", oaWorkflowService);
		setField(service, "smtDeviceTaskService", taskService);
		setField(service, "smtAdmittanceAreaTypeAuthService", areaTypeAuthService);
		setField(service, "smtDeviceAuthorityRelationService", relationService);
		setField(service, "smtAdmittanceFellowService", fellowService);
		setField(service, "putOffsetHour", 0);
		setField(service, "transactionTemplate", newTransactionTemplate());
		SmtAdmittanceApply approvedApply = pendingPersonAdmittanceApply(2009L, "oa-device-task-error");
		Page<SmtAdmittanceApply> page = new Page<>(1, 50);
		page.setRecords(Collections.singletonList(approvedApply));
		SmtAdmittanceAreaTypeAuth personAuth = SmtAdmittanceAreaTypeAuth.builder().authId(9101).build();
		SmtDeviceAuthorityRelation relation = new SmtDeviceAuthorityRelation();
		relation.setDeviceId("missing-device");
		SmtAdmittanceFellow fellow = new SmtAdmittanceFellow();
		fellow.setId(9102L);
		fellow.setFellowName("同行人");
		fellow.setFellowPhotoId("photo-9102");
		fellow.setCertNo("cert-9102");
		Mockito.when(mapper.selectPage(Mockito.any(Page.class), Mockito.any()))
				.thenReturn(page, emptyAdmittancePage(), emptyAdmittancePage());
		Mockito.when(mapper.update(Mockito.isNull(), Mockito.any())).thenReturn(1);
		Mockito.when(mapper.updateById(Mockito.any())).thenReturn(1);
		Mockito.when(oaWorkflowService.query("oa-device-task-error")).thenReturn(workflowLog(OaFinalStatusEnum.CAUSE_3));
		Mockito.when(areaTypeAuthService.getAuthByType(Mockito.eq("area-1"), Mockito.eq(DeviceTypeEnum.DEVICE_TYPE_1.getCode()), Mockito.eq(1001)))
				.thenReturn(Collections.singletonList(personAuth));
		Mockito.when(areaTypeAuthService.getAuthByType(Mockito.eq("area-1"), Mockito.eq(DeviceTypeEnum.DEVICE_TYPE_3.getCode()), Mockito.eq(1001)))
				.thenReturn(Collections.emptyList());
		Mockito.when(relationService.getRelationByAuthId(Mockito.anyList())).thenReturn(Collections.singletonList(relation));
		Mockito.when(fellowService.getByApplyId(approvedApply.getId())).thenReturn(Collections.singletonList(fellow));
		Mockito.when(taskService.saveTask(Mockito.any(DeviceTaskVO.class))).thenReturn("设备missing-device不存在");
		Mockito.doReturn(Boolean.TRUE).when(service).smbPutPhoto(approvedApply.getId());

		service.updateOaStatusTask();

		Assert.assertTrue(hasUpdateParam(mapper, DeviceDownStatusEnum.FAIL.getCode()));
	}

	@Test
	public void updateOaStatusTaskDoesNotMarkFailedWhenPhotoUploadReturnsFalse() throws Exception {
		// Task 3 解耦后：照片推送失败属于过渡期尽力而为行为，不再触发 syncOaStatus 的兜底 FAIL 标记
		SmtAdmittanceApplyMapper mapper = Mockito.mock(SmtAdmittanceApplyMapper.class);
		IOAWorkflowService oaWorkflowService = Mockito.mock(IOAWorkflowService.class);
		SmtAdmittanceAreaTypeAuthService areaTypeAuthService = Mockito.mock(SmtAdmittanceAreaTypeAuthService.class);
		SmtAdmittanceApplyServiceImpl service = Mockito.spy(new SmtAdmittanceApplyServiceImpl());
		setField(service, "baseMapper", mapper);
		setField(service, "oaWorkflowService", oaWorkflowService);
		setField(service, "smtAdmittanceAreaTypeAuthService", areaTypeAuthService);
		setField(service, "putOffsetHour", 0);
		setField(service, "transactionTemplate", newTransactionTemplate());
		setField(service, "photoPushEnabled", Boolean.TRUE);
		SmtAdmittanceApply approvedApply = pendingPersonAdmittanceApply(2010L, "oa-photo-error");
		Page<SmtAdmittanceApply> page = new Page<>(1, 50);
		page.setRecords(Collections.singletonList(approvedApply));
		Mockito.when(mapper.selectPage(Mockito.any(Page.class), Mockito.any()))
				.thenReturn(page, emptyAdmittancePage(), emptyAdmittancePage());
		Mockito.when(mapper.update(Mockito.isNull(), Mockito.any())).thenReturn(1);
		Mockito.when(mapper.updateById(Mockito.any())).thenReturn(1);
		Mockito.when(oaWorkflowService.query("oa-photo-error")).thenReturn(workflowLog(OaFinalStatusEnum.CAUSE_3));
		Mockito.when(areaTypeAuthService.getAuthByType(Mockito.anyString(), Mockito.anyInt(), Mockito.anyInt()))
				.thenReturn(Collections.emptyList());
		Mockito.doReturn(Boolean.FALSE).when(service).smbPutPhoto(approvedApply.getId());

		service.updateOaStatusTask();

		Assert.assertFalse(hasUpdateParam(mapper, DeviceDownStatusEnum.FAIL.getCode()));
		ArgumentCaptor<SmtAdmittanceApply> updateByIdCaptor = ArgumentCaptor.forClass(SmtAdmittanceApply.class);
		Mockito.verify(mapper, Mockito.atLeastOnce()).updateById(updateByIdCaptor.capture());
		Assert.assertTrue("推送失败不影响下发状态，应仍为已下发(4)",
				updateByIdCaptor.getAllValues().stream()
						.anyMatch(a -> DeviceDownStatusEnum.ALRAEDY.getCode().equals(a.getDeviceStatus())));
	}

	private boolean hasUpdateParam(SmtAdmittanceApplyMapper mapper, Object expected) {
		ArgumentCaptor<LambdaUpdateWrapper> updateCaptor = ArgumentCaptor.forClass(LambdaUpdateWrapper.class);
		Mockito.verify(mapper, Mockito.atLeastOnce()).update(Mockito.isNull(), updateCaptor.capture());
		for (LambdaUpdateWrapper wrapper : updateCaptor.getAllValues()) {
			wrapper.getSqlSet();
			if (wrapper.getParamNameValuePairs().values().stream()
					.anyMatch(value -> String.valueOf(expected).equals(String.valueOf(value)))) {
				return true;
			}
		}
		return false;
	}

	private void assertQueryHasParam(LambdaQueryWrapper queryWrapper, Object expected) {
		queryWrapper.getSqlSegment();
		Assert.assertTrue(queryHasParam(queryWrapper, expected));
	}

	private boolean queryHasParam(LambdaQueryWrapper queryWrapper, Object expected) {
		queryWrapper.getSqlSegment();
		return queryWrapper.getParamNameValuePairs().values().stream()
				.anyMatch(value -> queryParamMatches(value, expected));
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

	private SmtAdmittanceApply pendingAdmittanceApply(Long id, String processId) {
		SmtAdmittanceApply apply = new SmtAdmittanceApply();
		apply.setId(id);
		apply.setProcessId(processId);
		apply.setStatus(VisitorStatusEnum.Status_2.getCode());
		apply.setEndTime(LocalDateTime.now().plusDays(1));
		return apply;
	}

	private SmtAdmittanceApply pendingPersonAdmittanceApply(Long id, String processId) {
		SmtAdmittanceApply apply = pendingAdmittanceApply(id, processId);
		apply.setApplyType(AdmittanceTypeEnum.PERSON.getCode());
		apply.setAreaType("area-1");
		apply.setParkId(1001);
		apply.setStartTime(LocalDateTime.now().plusHours(1));
		apply.setEndTime(LocalDateTime.now().plusHours(3));
		return apply;
	}

	private SmtAdmittanceApply failedPostApprovalAdmittanceApply(Long id) {
		SmtAdmittanceApply apply = new SmtAdmittanceApply();
		apply.setId(id);
		apply.setProcessId("oa-approved-failed");
		apply.setStatus(VisitorStatusEnum.Status_0.getCode());
		apply.setDeviceStatus(DeviceDownStatusEnum.FAIL.getCode());
		apply.setApplyType(AdmittanceTypeEnum.PERSON.getCode());
		apply.setSmsCode("123456");
		apply.setEndTime(LocalDateTime.now().plusDays(1));
		return apply;
	}

	private SmtAdmittanceApply inWorkPostApprovalAdmittanceApply(Long id) {
		SmtAdmittanceApply apply = failedPostApprovalAdmittanceApply(id);
		apply.setDeviceStatus(DeviceDownStatusEnum.IN_WORK.getCode());
		return apply;
	}

	private Page<SmtAdmittanceApply> emptyAdmittancePage() {
		Page<SmtAdmittanceApply> page = new Page<>(1, 50);
		page.setRecords(Collections.emptyList());
		return page;
	}

	private WorkFlowLogDTO workflowLog(OaFinalStatusEnum finalStatus) {
		return workflowLog(finalStatus.getCode().toString());
	}

	private WorkFlowLogDTO workflowLog(String currentNodeType) {
		WorkFlowLogDataDTO logData = new WorkFlowLogDataDTO();
		logData.setCURRENTNODETYPE(currentNodeType);
		WorkFlowLogDTO log = new WorkFlowLogDTO();
		log.setType(WorkFlowLogConstants.SUCCESS);
		log.setResultdata(Collections.singletonList(logData));
		return log;
	}

	private StringRedisTemplate sharedRedisTemplate(Map<String, String> values, Set<String> recheckIds) {
		StringRedisTemplate redisTemplate = Mockito.mock(StringRedisTemplate.class);
		ValueOperations<String, String> valueOperations = Mockito.mock(ValueOperations.class);
		ZSetOperations<String, String> zSetOperations = Mockito.mock(ZSetOperations.class);
		Mockito.when(redisTemplate.opsForValue()).thenReturn(valueOperations);
		Mockito.when(redisTemplate.opsForZSet()).thenReturn(zSetOperations);
		Mockito.when(valueOperations.get(Mockito.anyString())).thenAnswer(invocation ->
				values.get(invocation.getArgument(0)));
		Mockito.doAnswer(invocation -> {
			values.put(invocation.getArgument(0), invocation.getArgument(1));
			return null;
		}).when(valueOperations).set(Mockito.anyString(), Mockito.anyString(), Mockito.anyLong(), Mockito.any(TimeUnit.class));
		Mockito.when(valueOperations.setIfAbsent(Mockito.anyString(), Mockito.anyString(), Mockito.anyLong(), Mockito.any(TimeUnit.class)))
				.thenReturn(Boolean.TRUE);
		Mockito.when(zSetOperations.add(Mockito.anyString(), Mockito.anyString(), Mockito.anyDouble())).thenAnswer(invocation -> {
			recheckIds.add(invocation.getArgument(1));
			return Boolean.TRUE;
		});
		Mockito.when(zSetOperations.rangeByScore(Mockito.anyString(), Mockito.anyDouble(), Mockito.anyDouble(),
				Mockito.anyLong(), Mockito.anyLong())).thenAnswer(invocation ->
				new LinkedHashSet<>(recheckIds));
		Mockito.when(zSetOperations.zCard(Mockito.anyString())).thenAnswer(invocation -> (long) recheckIds.size());
		Mockito.when(zSetOperations.removeRangeByScore(Mockito.anyString(), Mockito.anyDouble(), Mockito.anyDouble())).thenReturn(0L);
		Mockito.when(zSetOperations.removeRange(Mockito.anyString(), Mockito.anyLong(), Mockito.anyLong())).thenReturn(0L);
		Mockito.when(zSetOperations.remove(Mockito.anyString(), Mockito.any())).thenAnswer(invocation -> {
			Object[] arguments = invocation.getArguments();
			long removedCount = 0L;
			for (int index = 1; index < arguments.length; index++) {
				if (recheckIds.remove(String.valueOf(arguments[index]))) {
					removedCount++;
				}
			}
			return removedCount;
		});
		Mockito.when(redisTemplate.expire(Mockito.anyString(), Mockito.anyLong(), Mockito.any(TimeUnit.class))).thenReturn(Boolean.TRUE);
		return redisTemplate;
	}

	/**
	 * 构造以 mock PlatformTransactionManager 为底的 TransactionTemplate：
	 * submitIscBatch 用编程式事务包裹建任务 + 写批次号，单测无真实 DataSource，
	 * 用 mock 事务管理器令 execute 直接同步执行回调（不做真实提交/回滚）。
	 */
	private TransactionTemplate newTransactionTemplate() {
		PlatformTransactionManager transactionManager = Mockito.mock(PlatformTransactionManager.class);
		TransactionStatus transactionStatus = Mockito.mock(TransactionStatus.class);
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
