package com.tce.smart.platform.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tce.smart.common.core.constant.enums.SmtVisitorEnum;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.data.api.vo.msg.SendSmsVo;
import com.tce.smart.platform.core.dto.WorkFlowLogDTO;
import com.tce.smart.platform.core.dto.WorkFlowLogDataDTO;
import com.tce.smart.platform.core.entity.SmtVisitor;
import com.tce.smart.platform.core.mapper.SmtVisitorMapper;
import com.tce.smart.platform.core.service.SmtDeviceTaskService;
import com.tce.smart.platform.service.SmtDeviceAuthorityRelationService;
import com.tce.smart.platform.service.SmtFellowVisitorService;
import com.tce.smart.platform.service.IOAWorkflowService;
import com.tce.smart.tool.constant.WorkFlowLogConstants;
import com.tce.smart.tool.enums.OaFinalStatusEnum;
import com.tce.smart.tool.enums.VisitorStatusEnum;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.ZSetOperations;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@SuppressWarnings({"rawtypes", "unchecked"})
public class SmtVisitorServiceImplTest {

	@BeforeClass
	public static void initMybatisPlusLambdaCache() {
		TableInfoHelper.initTableInfo(new MapperBuilderAssistant(new MybatisConfiguration(), ""), SmtVisitor.class);
	}

	@Test
	public void updateOaStatusTaskQueriesLatestPendingVisitorPage() throws Exception {
		SmtVisitorMapper mapper = Mockito.mock(SmtVisitorMapper.class);
		SmtVisitorServiceImpl service = new SmtVisitorServiceImpl();
		setField(service, "baseMapper", mapper);
		Mockito.when(mapper.selectPage(Mockito.any(Page.class), Mockito.any()))
				.thenReturn(emptyVisitorPage(), emptyVisitorPage());

		service.updateOaStatusTask();

		ArgumentCaptor<Page> pageCaptor = ArgumentCaptor.forClass(Page.class);
		ArgumentCaptor<LambdaQueryWrapper> queryCaptor = ArgumentCaptor.forClass(LambdaQueryWrapper.class);
		Mockito.verify(mapper, Mockito.times(2)).selectPage(pageCaptor.capture(), queryCaptor.capture());
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
	}

	@Test
	public void updateOaStatusTaskContinuesAfterOaQueryErrorAndApprovesNextVisitor() throws Exception {
		SmtVisitorMapper mapper = Mockito.mock(SmtVisitorMapper.class);
		IOAWorkflowService oaWorkflowService = Mockito.mock(IOAWorkflowService.class);
		SmtVisitorServiceImpl service = Mockito.spy(new SmtVisitorServiceImpl());
		setField(service, "baseMapper", mapper);
		setField(service, "oaWorkflowService", oaWorkflowService);
		SmtVisitor timeoutVisitor = pendingVisitor(3001L, "oa-timeout");
		SmtVisitor approvedVisitor = pendingVisitor(3002L, "oa-approved");
		Page<SmtVisitor> page = new Page<>(1, 50);
		page.setRecords(Arrays.asList(timeoutVisitor, approvedVisitor));
		Mockito.when(mapper.selectPage(Mockito.any(Page.class), Mockito.any())).thenReturn(page, emptyVisitorPage());
		Mockito.when(mapper.update(Mockito.isNull(), Mockito.any())).thenReturn(1);
		Mockito.when(oaWorkflowService.query("oa-timeout")).thenThrow(new RuntimeException("oa timeout"));
		Mockito.when(oaWorkflowService.query("oa-approved")).thenReturn(workflowLog(OaFinalStatusEnum.CAUSE_3));
		Mockito.doReturn(Boolean.TRUE).when(service).updateHfStatus(Mockito.any(SmtVisitor.class));

		service.updateOaStatusTask();

		Mockito.verify(oaWorkflowService).query("oa-timeout");
		Mockito.verify(oaWorkflowService).query("oa-approved");
		ArgumentCaptor<SmtVisitor> visitorCaptor = ArgumentCaptor.forClass(SmtVisitor.class);
		Mockito.verify(service).updateHfStatus(visitorCaptor.capture());
		Assert.assertEquals(Long.valueOf(3002L), visitorCaptor.getValue().getId());
		Assert.assertEquals(VisitorStatusEnum.Status_0.getCode(), visitorCaptor.getValue().getStatus());
		Assert.assertFalse(StrUtil.isBlank(visitorCaptor.getValue().getSmsCode()));
	}

	@Test
	public void updateOaStatusTaskSkipsVisitorWhenPendingRowWasClaimedByAnotherRun() throws Exception {
		SmtVisitorMapper mapper = Mockito.mock(SmtVisitorMapper.class);
		IOAWorkflowService oaWorkflowService = Mockito.mock(IOAWorkflowService.class);
		SmtVisitorServiceImpl service = Mockito.spy(new SmtVisitorServiceImpl());
		setField(service, "baseMapper", mapper);
		setField(service, "oaWorkflowService", oaWorkflowService);
		SmtVisitor approvedVisitor = pendingVisitor(3003L, "oa-approved");
		Page<SmtVisitor> page = new Page<>(1, 50);
		page.setRecords(Collections.singletonList(approvedVisitor));
		Mockito.when(mapper.selectPage(Mockito.any(Page.class), Mockito.any())).thenReturn(page, emptyVisitorPage());
		Mockito.when(mapper.update(Mockito.isNull(), Mockito.any())).thenReturn(0);
		Mockito.when(oaWorkflowService.query("oa-approved")).thenReturn(workflowLog(OaFinalStatusEnum.CAUSE_3));
		Mockito.doReturn(Boolean.TRUE).when(service).updateHfStatus(Mockito.any(SmtVisitor.class));

		service.updateOaStatusTask();

		Mockito.verify(service, Mockito.never()).updateHfStatus(Mockito.any(SmtVisitor.class));
	}

	@Test
	public void updateOaStatusTaskContinuesAfterPostApprovalHandlingError() throws Exception {
		SmtVisitorMapper mapper = Mockito.mock(SmtVisitorMapper.class);
		IOAWorkflowService oaWorkflowService = Mockito.mock(IOAWorkflowService.class);
		SmtVisitorServiceImpl service = Mockito.spy(new SmtVisitorServiceImpl());
		setField(service, "baseMapper", mapper);
		setField(service, "oaWorkflowService", oaWorkflowService);
		SmtVisitor firstVisitor = pendingVisitor(3004L, "oa-approved-1");
		SmtVisitor secondVisitor = pendingVisitor(3005L, "oa-approved-2");
		Page<SmtVisitor> page = new Page<>(1, 50);
		page.setRecords(Arrays.asList(firstVisitor, secondVisitor));
		Mockito.when(mapper.selectPage(Mockito.any(Page.class), Mockito.any())).thenReturn(page, emptyVisitorPage());
		Mockito.when(mapper.update(Mockito.isNull(), Mockito.any())).thenReturn(1);
		Mockito.when(oaWorkflowService.query(Mockito.anyString())).thenReturn(workflowLog(OaFinalStatusEnum.CAUSE_3));
		Mockito.doThrow(new RuntimeException("device task failed"))
				.doReturn(Boolean.TRUE)
				.when(service).updateHfStatus(Mockito.any(SmtVisitor.class));

		service.updateOaStatusTask();

		Mockito.verify(service, Mockito.times(2)).updateHfStatus(Mockito.any(SmtVisitor.class));
	}

	@Test
	public void updateOaStatusTaskRestoresApprovedVisitorToPendingWhenPostApprovalHandlingFails() throws Exception {
		SmtVisitorMapper mapper = Mockito.mock(SmtVisitorMapper.class);
		IOAWorkflowService oaWorkflowService = Mockito.mock(IOAWorkflowService.class);
		SmtVisitorServiceImpl service = Mockito.spy(new SmtVisitorServiceImpl());
		setField(service, "baseMapper", mapper);
		setField(service, "oaWorkflowService", oaWorkflowService);
		SmtVisitor approvedVisitor = pendingVisitor(3014L, "oa-approved-restore");
		Page<SmtVisitor> page = new Page<>(1, 50);
		page.setRecords(Collections.singletonList(approvedVisitor));
		Mockito.when(mapper.selectPage(Mockito.any(Page.class), Mockito.any())).thenReturn(page, emptyVisitorPage());
		Mockito.when(mapper.update(Mockito.isNull(), Mockito.any())).thenReturn(1);
		Mockito.when(oaWorkflowService.query("oa-approved-restore")).thenReturn(workflowLog(OaFinalStatusEnum.CAUSE_3));
		Mockito.doThrow(new RuntimeException("device task failed"))
				.when(service).updateHfStatus(Mockito.any(SmtVisitor.class));

		service.updateOaStatusTask();

		ArgumentCaptor<LambdaUpdateWrapper> updateCaptor = ArgumentCaptor.forClass(LambdaUpdateWrapper.class);
		Mockito.verify(mapper, Mockito.times(2)).update(Mockito.isNull(), updateCaptor.capture());
		Assert.assertTrue(updateHasParam(updateCaptor.getAllValues().get(0), VisitorStatusEnum.Status_0.getCode()));
		Assert.assertTrue(updateHasParam(updateCaptor.getAllValues().get(1), VisitorStatusEnum.Status_2.getCode()));
		Assert.assertFalse(StrUtil.isBlank(approvedVisitor.getSmsCode()));
	}

	@Test
	public void updateHfStatusKeepsApprovedVisitorSuccessfulWhenSmsNoticeFails() throws Exception {
		SmtVisitorMapper mapper = Mockito.mock(SmtVisitorMapper.class);
		SmtDeviceAuthorityRelationService relationService = Mockito.mock(SmtDeviceAuthorityRelationService.class);
		SmtFellowVisitorService fellowVisitorService = Mockito.mock(SmtFellowVisitorService.class);
		SmtVisitorServiceImpl service = new SmtVisitorServiceImpl() {
			@Override
			public Result<SendSmsVo> sendMessage(String number, String visitorName, String tempCode, String hostName,
											 String appointmentDate, String realityDate, String deviceName, String company,
											 String reportToName, String refuseDes, String smsCode, String swichCode,
											 Integer parkId) {
				throw new RuntimeException("sms gateway down");
			}
		};
		setField(service, "baseMapper", mapper);
		setField(service, "deviceAuthorityRelationService", relationService);
		setField(service, "smtFellowVisitorService", fellowVisitorService);
		setField(service, "putOffsetHour", 0);
		SmtVisitor approvedVisitor = pendingVisitor(3016L, "oa-approved-sms-failed");
		approvedVisitor.setStatus(SmtVisitorEnum.PASS_STATUS.getType());
		approvedVisitor.setParkId(1);
		approvedVisitor.setVisitorName("访客");
		approvedVisitor.setVisitorPhone("13800000000");
		approvedVisitor.setReceptionistName("被访人");
		approvedVisitor.setReceptionistPhone("13900000000");
		approvedVisitor.setCompany("裕同");
		approvedVisitor.setIsVehicle(SmtVisitorEnum.NOT_VEHICLE.getType());
		approvedVisitor.setStartTime(new Date(System.currentTimeMillis() + 60 * 60 * 1000L));
		approvedVisitor.setEndTime(new Date(System.currentTimeMillis() + 2 * 60 * 60 * 1000L));
		Mockito.when(mapper.updateById(Mockito.any(SmtVisitor.class))).thenReturn(1);
		Mockito.when(relationService.getRelationAuth(Mockito.any(), Mockito.any(), Mockito.any()))
				.thenReturn(Collections.emptyList());
		Mockito.when(fellowVisitorService.selectListByVisitorId(Mockito.any(SmtVisitor.class)))
				.thenReturn(Collections.emptyList());

		Boolean updated = service.updateHfStatus(approvedVisitor);

		Assert.assertTrue(updated);
		Assert.assertFalse(StrUtil.isBlank(approvedVisitor.getSmsCode()));
		Mockito.verify(mapper).updateById(Mockito.eq(approvedVisitor));
	}

	@Test
	public void addCardThrowsWhenDeviceTaskCreationFails() throws Exception {
		SmtDeviceTaskService taskService = Mockito.mock(SmtDeviceTaskService.class);
		SmtVisitorServiceImpl service = new SmtVisitorServiceImpl();
		setField(service, "smtDeviceTaskService", taskService);
		setField(service, "putOffsetHour", 0);
		SmtVisitor visitor = pendingVisitor(3013L, "oa-approved");
		visitor.setVisitorName("访客");
		visitor.setVisitorPhotoId("photo-3013");
		visitor.setCertNo("cert-3013");
		visitor.setStartTime(new Date(System.currentTimeMillis() + 60 * 60 * 1000L));
		Mockito.when(taskService.saveTask(Mockito.any())).thenReturn("设备不存在");
		Method addCard = SmtVisitorServiceImpl.class.getDeclaredMethod("addCard", SmtVisitor.class, String.class);
		addCard.setAccessible(true);

		try {
			addCard.invoke(service, visitor, "missing-device");
			Assert.fail("device task failure should throw");
		} catch (InvocationTargetException expected) {
			Assert.assertTrue(expected.getCause() instanceof IllegalStateException);
		}
	}

	@Test
	public void addCarCardTreatsUnsupportedIscVehicleTaskAsSkippedSuccess() throws Exception {
		SmtDeviceTaskService taskService = Mockito.mock(SmtDeviceTaskService.class);
		SmtVisitorServiceImpl service = new SmtVisitorServiceImpl();
		setField(service, "smtDeviceTaskService", taskService);
		setField(service, "putOffsetHour", 0);
		SmtVisitor visitor = pendingVisitor(3015L, "oa-approved-car");
		visitor.setVehiclePlate("粤B54321");
		visitor.setCertNo("cert-3015");
		visitor.setStartTime(new Date(System.currentTimeMillis() + 60 * 60 * 1000L));
		Mockito.when(taskService.saveTask(Mockito.any())).thenReturn("ISC车辆权限不支持下发");
		Method addCarCard = SmtVisitorServiceImpl.class.getDeclaredMethod("addCarCard", SmtVisitor.class, String.class);
		addCarCard.setAccessible(true);

		addCarCard.invoke(service, visitor, "isc-car-device");

		Mockito.verify(taskService).saveTask(Mockito.any());
	}

	@Test
	public void updateOaStatusTaskProcessesCursorPageWhenLatestPageIsStillPending() throws Exception {
		SmtVisitorMapper mapper = Mockito.mock(SmtVisitorMapper.class);
		IOAWorkflowService oaWorkflowService = Mockito.mock(IOAWorkflowService.class);
		SmtVisitorServiceImpl service = Mockito.spy(new SmtVisitorServiceImpl());
		setField(service, "baseMapper", mapper);
		setField(service, "oaWorkflowService", oaWorkflowService);
		SmtVisitor latestPendingVisitor = pendingVisitor(3006L, "oa-still-pending");
		SmtVisitor cursorApprovedVisitor = pendingVisitor(3007L, "oa-cursor-approved");
		Page<SmtVisitor> latestPage = new Page<>(1, 50);
		latestPage.setRecords(Collections.singletonList(latestPendingVisitor));
		Page<SmtVisitor> cursorPage = new Page<>(1, 50);
		cursorPage.setRecords(Collections.singletonList(cursorApprovedVisitor));
		Mockito.when(mapper.selectPage(Mockito.any(Page.class), Mockito.any())).thenReturn(latestPage, cursorPage, emptyVisitorPage());
		Mockito.when(mapper.update(Mockito.isNull(), Mockito.any())).thenReturn(1);
		Mockito.when(oaWorkflowService.query("oa-still-pending")).thenReturn(workflowLog("2"));
		Mockito.when(oaWorkflowService.query("oa-cursor-approved")).thenReturn(workflowLog(OaFinalStatusEnum.CAUSE_3));
		Mockito.doReturn(Boolean.TRUE).when(service).updateHfStatus(Mockito.any(SmtVisitor.class));

		service.updateOaStatusTask();

		ArgumentCaptor<SmtVisitor> visitorCaptor = ArgumentCaptor.forClass(SmtVisitor.class);
		Mockito.verify(service).updateHfStatus(visitorCaptor.capture());
		Assert.assertEquals(Long.valueOf(3007L), visitorCaptor.getValue().getId());
	}

	@Test
	public void updateOaStatusTaskProcessesMiddleCursorPageWhenBothEdgesAreStillPending() throws Exception {
		SmtVisitorMapper mapper = Mockito.mock(SmtVisitorMapper.class);
		IOAWorkflowService oaWorkflowService = Mockito.mock(IOAWorkflowService.class);
		SmtVisitorServiceImpl service = Mockito.spy(new SmtVisitorServiceImpl());
		setField(service, "baseMapper", mapper);
		setField(service, "oaWorkflowService", oaWorkflowService);
		SmtVisitor latestPendingVisitor = pendingVisitor(3008L, "oa-new-pending");
		SmtVisitor middleApprovedVisitor = pendingVisitor(3009L, "oa-middle-approved");
		SmtVisitor oldestPendingVisitor = pendingVisitor(3010L, "oa-old-pending");
		Page<SmtVisitor> latestPage = new Page<>(1, 50);
		latestPage.setRecords(Collections.singletonList(latestPendingVisitor));
		Page<SmtVisitor> oldestPage = new Page<>(1, 50);
		oldestPage.setRecords(Collections.singletonList(oldestPendingVisitor));
		Page<SmtVisitor> middlePage = new Page<>(1, 50);
		middlePage.setRecords(Collections.singletonList(middleApprovedVisitor));
		Mockito.when(mapper.selectPage(Mockito.any(Page.class), Mockito.any())).thenReturn(latestPage, oldestPage, middlePage);
		Mockito.when(mapper.update(Mockito.isNull(), Mockito.any())).thenReturn(1);
		Mockito.when(oaWorkflowService.query("oa-new-pending")).thenReturn(workflowLog("2"));
		Mockito.when(oaWorkflowService.query("oa-old-pending")).thenReturn(workflowLog("2"));
		Mockito.when(oaWorkflowService.query("oa-middle-approved")).thenReturn(workflowLog(OaFinalStatusEnum.CAUSE_3));
		Mockito.doReturn(Boolean.TRUE).when(service).updateHfStatus(Mockito.any(SmtVisitor.class));

		service.updateOaStatusTask();

		Mockito.verify(oaWorkflowService).query("oa-middle-approved");
	}

	@Test
	public void updateOaStatusTaskAdvancesCursorAcrossRuns() throws Exception {
		SmtVisitorMapper mapper = Mockito.mock(SmtVisitorMapper.class);
		IOAWorkflowService oaWorkflowService = Mockito.mock(IOAWorkflowService.class);
		SmtVisitorServiceImpl service = new SmtVisitorServiceImpl();
		setField(service, "baseMapper", mapper);
		setField(service, "oaWorkflowService", oaWorkflowService);
		Page<SmtVisitor> oldestPage = new Page<>(1, 50);
		oldestPage.setRecords(Collections.singletonList(pendingVisitor(100L, "oa-old-pending")));
		Page<SmtVisitor> firstCursorPage = new Page<>(1, 50);
		firstCursorPage.setRecords(Collections.singletonList(pendingVisitor(200L, "oa-first-cursor-pending")));
		Page<SmtVisitor> secondCursorPage = new Page<>(1, 50);
		secondCursorPage.setRecords(Collections.singletonList(pendingVisitor(300L, "oa-second-cursor-pending")));
		Mockito.when(mapper.selectPage(Mockito.any(Page.class), Mockito.any()))
				.thenReturn(emptyVisitorPage(), oldestPage, firstCursorPage,
						emptyVisitorPage(), oldestPage, secondCursorPage);
		Mockito.when(oaWorkflowService.query(Mockito.anyString())).thenReturn(workflowLog("2"));

		service.updateOaStatusTask();
		service.updateOaStatusTask();

		ArgumentCaptor<LambdaQueryWrapper> queryCaptor = ArgumentCaptor.forClass(LambdaQueryWrapper.class);
		Mockito.verify(mapper, Mockito.atLeast(6)).selectPage(Mockito.any(Page.class), queryCaptor.capture());
		Assert.assertTrue(queryCaptor.getAllValues().stream()
				.anyMatch(query -> query.getSqlSegment().toLowerCase(Locale.ROOT).contains("id >")
							&& queryHasParam(query, 200L)));
	}

	@Test
	public void updateOaStatusTaskRechecksRecentlyScannedPendingVisitorOnNextRun() throws Exception {
		SmtVisitorMapper mapper = Mockito.mock(SmtVisitorMapper.class);
		IOAWorkflowService oaWorkflowService = Mockito.mock(IOAWorkflowService.class);
		SmtVisitorServiceImpl service = Mockito.spy(new SmtVisitorServiceImpl());
		setField(service, "baseMapper", mapper);
		setField(service, "oaWorkflowService", oaWorkflowService);
		SmtVisitor oldestPendingVisitor = pendingVisitor(3100L, "oa-old-still-pending");
		SmtVisitor recentlyScannedVisitor = pendingVisitor(3200L, "oa-recently-scanned");
		Page<SmtVisitor> oldestPage = new Page<>(1, 50);
		oldestPage.setRecords(Collections.singletonList(oldestPendingVisitor));
		Page<SmtVisitor> cursorPage = new Page<>(1, 50);
		cursorPage.setRecords(Collections.singletonList(recentlyScannedVisitor));
		Page<SmtVisitor> recheckPage = new Page<>(1, 50);
		recheckPage.setRecords(Collections.singletonList(recentlyScannedVisitor));
		Mockito.when(mapper.selectPage(Mockito.any(Page.class), Mockito.any()))
				.thenReturn(emptyVisitorPage(), oldestPage, cursorPage,
						emptyVisitorPage(), oldestPage, recheckPage,
						emptyVisitorPage(), emptyVisitorPage());
		Mockito.when(mapper.update(Mockito.isNull(), Mockito.any())).thenReturn(1);
		Mockito.when(oaWorkflowService.query("oa-old-still-pending")).thenReturn(workflowLog("2"));
		Mockito.when(oaWorkflowService.query("oa-recently-scanned"))
				.thenReturn(workflowLog("2"))
				.thenReturn(workflowLog(OaFinalStatusEnum.CAUSE_3));
		Mockito.doReturn(Boolean.TRUE).when(service).updateHfStatus(Mockito.any(SmtVisitor.class));

		service.updateOaStatusTask();
		service.updateOaStatusTask();

		ArgumentCaptor<SmtVisitor> visitorCaptor = ArgumentCaptor.forClass(SmtVisitor.class);
		Mockito.verify(service).updateHfStatus(visitorCaptor.capture());
		Assert.assertEquals(Long.valueOf(3200L), visitorCaptor.getValue().getId());
	}

	@Test
	public void updateOaStatusTaskRechecksPendingVisitorAcrossPlatformInstancesWithRedis() throws Exception {
		Map<String, String> redisValues = new HashMap<>();
		Set<String> redisRecheckIds = new LinkedHashSet<>();
		StringRedisTemplate redisTemplate = sharedRedisTemplate(redisValues, redisRecheckIds);
		SmtVisitorMapper firstMapper = Mockito.mock(SmtVisitorMapper.class);
		SmtVisitorMapper secondMapper = Mockito.mock(SmtVisitorMapper.class);
		IOAWorkflowService firstOaWorkflowService = Mockito.mock(IOAWorkflowService.class);
		IOAWorkflowService secondOaWorkflowService = Mockito.mock(IOAWorkflowService.class);
		SmtVisitorServiceImpl firstService = Mockito.spy(new SmtVisitorServiceImpl());
		SmtVisitorServiceImpl secondService = Mockito.spy(new SmtVisitorServiceImpl());
		setField(firstService, "baseMapper", firstMapper);
		setField(firstService, "oaWorkflowService", firstOaWorkflowService);
		setField(firstService, "stringRedisTemplate", redisTemplate);
		setField(secondService, "baseMapper", secondMapper);
		setField(secondService, "oaWorkflowService", secondOaWorkflowService);
		setField(secondService, "stringRedisTemplate", redisTemplate);
		SmtVisitor recentlyScannedVisitor = pendingVisitor(3210L, "oa-shared-recheck");
		Page<SmtVisitor> firstLatestPage = new Page<>(1, 50);
		firstLatestPage.setRecords(Collections.singletonList(recentlyScannedVisitor));
		Page<SmtVisitor> secondRecheckPage = new Page<>(1, 50);
		secondRecheckPage.setRecords(Collections.singletonList(recentlyScannedVisitor));
		Mockito.when(firstMapper.selectPage(Mockito.any(Page.class), Mockito.any()))
				.thenReturn(firstLatestPage, emptyVisitorPage());
		Mockito.when(secondMapper.selectPage(Mockito.any(Page.class), Mockito.any()))
				.thenReturn(emptyVisitorPage(), emptyVisitorPage(), secondRecheckPage);
		Mockito.when(secondMapper.update(Mockito.isNull(), Mockito.any())).thenReturn(1);
		Mockito.when(firstOaWorkflowService.query("oa-shared-recheck")).thenReturn(workflowLog("2"));
		Mockito.when(secondOaWorkflowService.query("oa-shared-recheck")).thenReturn(workflowLog(OaFinalStatusEnum.CAUSE_3));
		Mockito.doReturn(Boolean.TRUE).when(secondService).updateHfStatus(Mockito.any(SmtVisitor.class));

		firstService.updateOaStatusTask();
		secondService.updateOaStatusTask();

		ArgumentCaptor<SmtVisitor> visitorCaptor = ArgumentCaptor.forClass(SmtVisitor.class);
		Mockito.verify(secondService).updateHfStatus(visitorCaptor.capture());
		Assert.assertEquals(Long.valueOf(3210L), visitorCaptor.getValue().getId());
	}

	private void assertQueryHasParam(LambdaQueryWrapper queryWrapper, Object expected) {
		queryWrapper.getSqlSegment();
		Assert.assertTrue(queryHasParam(queryWrapper, expected));
	}

	private boolean queryHasParam(LambdaQueryWrapper queryWrapper, Object expected) {
		queryWrapper.getSqlSegment();
		return queryWrapper.getParamNameValuePairs().values().stream()
				.anyMatch(value -> String.valueOf(expected).equals(String.valueOf(value)));
	}

	private boolean updateHasParam(LambdaUpdateWrapper updateWrapper, Object expected) {
		updateWrapper.getSqlSet();
		return updateWrapper.getParamNameValuePairs().values().stream()
				.anyMatch(value -> String.valueOf(expected).equals(String.valueOf(value)));
	}

	// ========== claim 式落 OA 终态入口（回调链路与拉取对账共用，见 claimAndApplyHfOaFinalStatus） ==========

	@Test
	public void claimAndApplyHfOaFinalStatus_claimLost_returnsFalse_andSkipsPostProcessing() throws Exception {
		SmtVisitorMapper mapper = Mockito.mock(SmtVisitorMapper.class);
		SmtVisitorServiceImpl service = Mockito.spy(new SmtVisitorServiceImpl());
		setField(service, "baseMapper", mapper);
		//CAS 条件更新未命中：该行已被拉取对账（或另一次回调）先行落终态
		Mockito.when(mapper.update(Mockito.isNull(), Mockito.any())).thenReturn(0);
		Mockito.doReturn(Boolean.TRUE).when(service).updateHfStatus(Mockito.any(SmtVisitor.class));

		boolean claimed = service.claimAndApplyHfOaFinalStatus(
				pendingVisitor(3101L, "oa-cb-claim-lost"), VisitorStatusEnum.Status_0.getCode());

		Assert.assertFalse(claimed);
		Mockito.verify(service, Mockito.never()).updateHfStatus(Mockito.any(SmtVisitor.class));
	}

	@Test
	public void claimAndApplyHfOaFinalStatus_claimWon_runsPostProcessing_andFillsSmsCode() throws Exception {
		SmtVisitorMapper mapper = Mockito.mock(SmtVisitorMapper.class);
		SmtVisitorServiceImpl service = Mockito.spy(new SmtVisitorServiceImpl());
		setField(service, "baseMapper", mapper);
		Mockito.when(mapper.update(Mockito.isNull(), Mockito.any())).thenReturn(1);
		Mockito.doReturn(Boolean.TRUE).when(service).updateHfStatus(Mockito.any(SmtVisitor.class));
		SmtVisitor visitor = pendingVisitor(3102L, "oa-cb-claim-won");

		boolean claimed = service.claimAndApplyHfOaFinalStatus(visitor, VisitorStatusEnum.Status_0.getCode());

		Assert.assertTrue(claimed);
		ArgumentCaptor<SmtVisitor> visitorCaptor = ArgumentCaptor.forClass(SmtVisitor.class);
		Mockito.verify(service).updateHfStatus(visitorCaptor.capture());
		Assert.assertEquals(Long.valueOf(3102L), visitorCaptor.getValue().getId());
		Assert.assertEquals(VisitorStatusEnum.Status_0.getCode(), visitorCaptor.getValue().getStatus());
		//审批通过路径必须在 claim 阶段生成短信验证码（与拉取对账行为一致）
		Assert.assertFalse(StrUtil.isBlank(visitorCaptor.getValue().getSmsCode()));
	}

	@Test
	public void claimAndApplyHfOaFinalStatus_postProcessingFails_restoresPendingStatus_andReturnsTrue() throws Exception {
		SmtVisitorMapper mapper = Mockito.mock(SmtVisitorMapper.class);
		SmtVisitorServiceImpl service = Mockito.spy(new SmtVisitorServiceImpl());
		setField(service, "baseMapper", mapper);
		Mockito.when(mapper.update(Mockito.isNull(), Mockito.any())).thenReturn(1);
		Mockito.doThrow(new RuntimeException("post approval failed"))
				.when(service).updateHfStatus(Mockito.any(SmtVisitor.class));
		SmtVisitor visitor = pendingVisitor(3103L, "oa-cb-post-fail");

		boolean claimed = service.claimAndApplyHfOaFinalStatus(visitor, VisitorStatusEnum.Status_0.getCode());

		//claim 已成功返回 true；后置失败回滚为待审核，交拉取对账重试（与 syncOaStatus 失败补偿一致）
		Assert.assertTrue(claimed);
		Assert.assertEquals(VisitorStatusEnum.Status_2.getCode(), visitor.getStatus());
		//mapper.update 两次：claim CAS 一次 + 状态回滚一次
		Mockito.verify(mapper, Mockito.times(2)).update(Mockito.isNull(), Mockito.any());
	}

	@Test
	public void claimAndApplyHfOaFinalStatus_rejectPostProcessingFails_restoresPendingStatus_andReturnsTrue() throws Exception {
		SmtVisitorMapper mapper = Mockito.mock(SmtVisitorMapper.class);
		SmtVisitorServiceImpl service = Mockito.spy(new SmtVisitorServiceImpl());
		setField(service, "baseMapper", mapper);
		Mockito.when(mapper.update(Mockito.isNull(), Mockito.any())).thenReturn(1);
		Mockito.doThrow(new RuntimeException("post rejection failed"))
				.when(service).updateHfStatus(Mockito.any(SmtVisitor.class));
		SmtVisitor visitor = pendingVisitor(3104L, "oa-cb-reject-post-fail");

		boolean claimed = service.claimAndApplyHfOaFinalStatus(visitor, VisitorStatusEnum.Status_1.getCode());

		//拒绝路径后置失败（拒绝短信等）也必须回滚为待审核，否则行停在 Status_1，对账只查 Status_2 永不重试
		Assert.assertTrue(claimed);
		Assert.assertEquals(VisitorStatusEnum.Status_2.getCode(), visitor.getStatus());
		//mapper.update 两次：claim CAS 一次 + 状态回滚一次
		Mockito.verify(mapper, Mockito.times(2)).update(Mockito.isNull(), Mockito.any());
	}

	@Test
	public void updateOaStatusTaskRestoresRejectedVisitorToPendingWhenPostRejectionHandlingFails() throws Exception {
		SmtVisitorMapper mapper = Mockito.mock(SmtVisitorMapper.class);
		IOAWorkflowService oaWorkflowService = Mockito.mock(IOAWorkflowService.class);
		SmtVisitorServiceImpl service = Mockito.spy(new SmtVisitorServiceImpl());
		setField(service, "baseMapper", mapper);
		setField(service, "oaWorkflowService", oaWorkflowService);
		SmtVisitor rejectedVisitor = pendingVisitor(3017L, "oa-rejected-restore");
		Page<SmtVisitor> page = new Page<>(1, 50);
		page.setRecords(Collections.singletonList(rejectedVisitor));
		Mockito.when(mapper.selectPage(Mockito.any(Page.class), Mockito.any())).thenReturn(page, emptyVisitorPage());
		Mockito.when(mapper.update(Mockito.isNull(), Mockito.any())).thenReturn(1);
		//OA 终态为拒绝（CAUSE_0 → Status_1）
		Mockito.when(oaWorkflowService.query("oa-rejected-restore")).thenReturn(workflowLog(OaFinalStatusEnum.CAUSE_0));
		Mockito.doThrow(new RuntimeException("reject sms failed"))
				.when(service).updateHfStatus(Mockito.any(SmtVisitor.class));

		service.updateOaStatusTask();

		//两次 update：claim 落 Status_1 一次 + 回滚 Status_2 一次，行重新回到对账可见范围
		ArgumentCaptor<LambdaUpdateWrapper> updateCaptor = ArgumentCaptor.forClass(LambdaUpdateWrapper.class);
		Mockito.verify(mapper, Mockito.times(2)).update(Mockito.isNull(), updateCaptor.capture());
		Assert.assertTrue(updateHasParam(updateCaptor.getAllValues().get(0), VisitorStatusEnum.Status_1.getCode()));
		Assert.assertTrue(updateHasParam(updateCaptor.getAllValues().get(1), VisitorStatusEnum.Status_2.getCode()));
	}

	private SmtVisitor pendingVisitor(Long id, String processId) {
		SmtVisitor visitor = new SmtVisitor();
		visitor.setId(id);
		visitor.setProcessId(processId);
		visitor.setStatus(VisitorStatusEnum.Status_2.getCode());
		visitor.setEndTime(new Date(System.currentTimeMillis() + 24 * 60 * 60 * 1000L));
		return visitor;
	}

	private Page<SmtVisitor> emptyVisitorPage() {
		Page<SmtVisitor> page = new Page<>(1, 50);
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
