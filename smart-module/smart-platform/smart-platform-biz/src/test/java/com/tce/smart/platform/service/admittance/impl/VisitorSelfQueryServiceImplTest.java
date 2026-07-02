package com.tce.smart.platform.service.admittance.impl;

import com.tce.smart.app.api.feign.RemoteAppSmsService;
import com.tce.smart.common.core.exception.SmartException;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.platform.api.dto.req.admittance.VisitorSelfQueryReqDTO;
import com.tce.smart.platform.api.dto.resp.admittance.VisitorApplyRecordDetailRespDTO;
import com.tce.smart.platform.api.dto.resp.admittance.VisitorApplyRecordRespDTO;
import com.tce.smart.platform.api.dto.resp.admittance.VisitorApprovalProgressRespDTO;
import com.tce.smart.platform.api.dto.resp.admittance.VisitorSelfQueryRespDTO;
import com.tce.smart.platform.core.entity.ApproveList;
import com.tce.smart.platform.core.entity.SmtPark;
import com.tce.smart.platform.core.entity.SmtApprovalNode;
import com.tce.smart.platform.core.entity.SmtStaff;
import com.tce.smart.platform.core.entity.admittance.SmtAdmittanceApply;
import com.tce.smart.platform.core.entity.admittance.SmtAdmittanceFellow;
import com.tce.smart.platform.core.entity.admittance.SmtAdmittanceVehicle;
import com.tce.smart.platform.core.mapper.SmtAdmittanceApplyMapper;
import com.tce.smart.platform.core.service.SmtApprovalNodeService;
import com.tce.smart.platform.core.vo.FlowVO;
import com.tce.smart.platform.service.ApproveListService;
import com.tce.smart.platform.service.SmtOutDormitoryStaffService;
import com.tce.smart.platform.service.SmtParkService;
import com.tce.smart.platform.service.SmtStaffService;
import com.tce.smart.platform.service.admittance.SmtAdmittanceFellowService;
import com.tce.smart.platform.service.admittance.SmtAdmittanceVehicleService;
import com.tce.smart.tool.enums.ApplicationEnum;
import com.tce.smart.tool.enums.DeviceDownStatusEnum;
import com.tce.smart.tool.enums.ApproveListStateEnum;
import com.tce.smart.tool.enums.VisitorStatusEnum;
import com.tce.smart.tool.constant.ApproveListTypeConstants;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

@SuppressWarnings({"rawtypes", "unchecked"})
public class VisitorSelfQueryServiceImplTest {

	@Test
	public void listMyApplyVerifiesSmsStoresOneDayTokenAndReturnsFullNames() throws Exception {
		SmtAdmittanceApplyMapper mapper = Mockito.mock(SmtAdmittanceApplyMapper.class);
		RemoteAppSmsService smsService = Mockito.mock(RemoteAppSmsService.class);
		StringRedisTemplate redisTemplate = Mockito.mock(StringRedisTemplate.class);
		ValueOperations<String, String> valueOperations = Mockito.mock(ValueOperations.class);
		SmtParkService parkService = Mockito.mock(SmtParkService.class);
		SmtAdmittanceFellowService fellowService = Mockito.mock(SmtAdmittanceFellowService.class);
		SmtAdmittanceVehicleService vehicleService = Mockito.mock(SmtAdmittanceVehicleService.class);
		VisitorSelfQueryServiceImpl service = newService(mapper, smsService, redisTemplate, valueOperations, parkService,
				fellowService, vehicleService);
		setField(service, "queryTokenSupplier", (Supplier<String>) () -> "tok-fixed");
		Mockito.when(smsService.verifySmsCode("13712341234", "123456")).thenReturn(Result.success(Boolean.TRUE));
		SmtPark park = new SmtPark();
		park.setParkName("裕同科技许昌园区");
		Mockito.when(parkService.getById(5000021)).thenReturn(park);
		SmtAdmittanceApply pending = apply(1001L, "李明", "13712341234", VisitorStatusEnum.Status_0.getCode());
		pending.setDeviceStatus(DeviceDownStatusEnum.IN_WORK.getCode());
		Mockito.when(mapper.selectList(Mockito.any())).thenReturn(Collections.singletonList(pending));
		Mockito.when(fellowService.getByApplyId(1001L)).thenReturn(Arrays.asList(fellow("李明", 1), fellow("赵六", 0)));
		Mockito.when(vehicleService.getByApplyId(1001L)).thenReturn(Collections.singletonList(vehicle("豫A12345")));
		VisitorSelfQueryReqDTO request = new VisitorSelfQueryReqDTO();
		request.setMobile("13712341234");
		request.setSmsCode("123456");

		VisitorSelfQueryRespDTO response = service.listMyApply(request, null);

		Assert.assertEquals("tok-fixed", response.getQueryToken());
		Assert.assertEquals("李明", response.getMaskedName());
		Assert.assertEquals("137****1234", response.getMaskedMobile());
		List<VisitorApplyRecordRespDTO> records = response.getRecords();
		Assert.assertEquals(1, records.size());
		Assert.assertEquals("1001", records.get(0).getApplyId());
		Assert.assertEquals("PASSED", records.get(0).getApplyStatus());
		Assert.assertEquals("王强", records.get(0).getReceptionistName());
		Assert.assertEquals("ISSUING", records.get(0).getDispatchStatus());
		Assert.assertEquals(Integer.valueOf(1), records.get(0).getFellowCount());
		Assert.assertEquals(Collections.singletonList("豫A12345"), records.get(0).getPlates());
		Mockito.verify(smsService).verifySmsCode("13712341234", "123456");
		Mockito.verify(valueOperations).set("smart:admittance:visitor-query:tok-fixed", "13712341234", 1L,
				TimeUnit.DAYS);
	}

	@Test
	public void listMyApplyRefreshesWithExistingTokenWithoutCheckingSmsAgain() throws Exception {
		SmtAdmittanceApplyMapper mapper = Mockito.mock(SmtAdmittanceApplyMapper.class);
		RemoteAppSmsService smsService = Mockito.mock(RemoteAppSmsService.class);
		StringRedisTemplate redisTemplate = Mockito.mock(StringRedisTemplate.class);
		ValueOperations<String, String> valueOperations = Mockito.mock(ValueOperations.class);
		SmtParkService parkService = Mockito.mock(SmtParkService.class);
		SmtAdmittanceFellowService fellowService = Mockito.mock(SmtAdmittanceFellowService.class);
		SmtAdmittanceVehicleService vehicleService = Mockito.mock(SmtAdmittanceVehicleService.class);
		VisitorSelfQueryServiceImpl service = newService(mapper, smsService, redisTemplate, valueOperations, parkService,
				fellowService, vehicleService);
		Mockito.when(valueOperations.get("smart:admittance:visitor-query:tok-existing")).thenReturn("13712341234");
		Mockito.when(mapper.selectList(Mockito.any())).thenReturn(Collections.singletonList(
				apply(1002L, "李明", "13712341234", VisitorStatusEnum.Status_0.getCode())));

		VisitorSelfQueryRespDTO response = service.listMyApply(new VisitorSelfQueryReqDTO(), "tok-existing");

		Assert.assertEquals("tok-existing", response.getQueryToken());
		Assert.assertEquals(1, response.getRecords().size());
		Mockito.verify(smsService, Mockito.never()).verifySmsCode(Mockito.anyString(), Mockito.anyString());
		Mockito.verify(valueOperations).get("smart:admittance:visitor-query:tok-existing");
	}

	@Test
	public void listMyApplyUsesOaCurrentNodeWhenPendingApplyHasProcessId() throws Exception {
		SmtAdmittanceApplyMapper mapper = Mockito.mock(SmtAdmittanceApplyMapper.class);
		RemoteAppSmsService smsService = Mockito.mock(RemoteAppSmsService.class);
		StringRedisTemplate redisTemplate = Mockito.mock(StringRedisTemplate.class);
		ValueOperations<String, String> valueOperations = Mockito.mock(ValueOperations.class);
		SmtParkService parkService = Mockito.mock(SmtParkService.class);
		SmtAdmittanceFellowService fellowService = Mockito.mock(SmtAdmittanceFellowService.class);
		SmtAdmittanceVehicleService vehicleService = Mockito.mock(SmtAdmittanceVehicleService.class);
		ApproveListService approveListService = Mockito.mock(ApproveListService.class);
		SmtOutDormitoryStaffService outDormitoryStaffService = Mockito.mock(SmtOutDormitoryStaffService.class);
		VisitorSelfQueryServiceImpl service = newService(mapper, smsService, redisTemplate, valueOperations, parkService,
				fellowService, vehicleService);
		setField(service, "approveListService", approveListService);
		setField(service, "smtOutDormitoryStaffService", outDormitoryStaffService);
		Mockito.when(valueOperations.get("smart:admittance:visitor-query:tok-existing")).thenReturn("13712341234");
		SmtPark park = new SmtPark();
		park.setParkName("裕同科技许昌园区");
		Mockito.when(parkService.getById(5000021)).thenReturn(park);
		SmtAdmittanceApply apply = apply(1010L, "李明", "13712341234", VisitorStatusEnum.Status_2.getCode());
		apply.setProcessId("OA-9004");
		Mockito.when(mapper.selectList(Mockito.any())).thenReturn(Collections.singletonList(apply));
		Mockito.when(fellowService.getByApplyId(1010L)).thenReturn(Collections.emptyList());
		Mockito.when(vehicleService.getByApplyId(1010L)).thenReturn(Collections.emptyList());
		Mockito.doAnswer(invocation -> {
			List<FlowVO> flowList = (List<FlowVO>) invocation.getArguments()[1];
			flowList.add(flow("提交申请", ApplicationEnum.RECORD_STATUS_2.getDesc(), "李明",
					LocalDateTime.of(2026, 6, 13, 9, 5), ""));
			flowList.add(flow("部门经理审批", ApplicationEnum.RECORD_STATUS_c.getDesc(), "张三", null, ""));
			return null;
		}).when(outDormitoryStaffService).getOAProcessFlow(Mockito.eq("OA-9004"), Mockito.anyList());

		VisitorSelfQueryRespDTO response = service.listMyApply(new VisitorSelfQueryReqDTO(), "tok-existing");

		Assert.assertEquals(1, response.getRecords().size());
		Assert.assertEquals("PENDING", response.getRecords().get(0).getApplyStatus());
		Assert.assertEquals("部门经理审批 张三 审批中", response.getRecords().get(0).getCurrentNode());
		Mockito.verify(outDormitoryStaffService).getOAProcessFlow(Mockito.eq("OA-9004"), Mockito.anyList());
		Mockito.verify(approveListService, Mockito.never()).list(Mockito.any());
	}

	@Test
	public void listMyApplyUsesLocalCurrentNodeWithApproverName() throws Exception {
		SmtAdmittanceApplyMapper mapper = Mockito.mock(SmtAdmittanceApplyMapper.class);
		RemoteAppSmsService smsService = Mockito.mock(RemoteAppSmsService.class);
		StringRedisTemplate redisTemplate = Mockito.mock(StringRedisTemplate.class);
		ValueOperations<String, String> valueOperations = Mockito.mock(ValueOperations.class);
		SmtParkService parkService = Mockito.mock(SmtParkService.class);
		SmtAdmittanceFellowService fellowService = Mockito.mock(SmtAdmittanceFellowService.class);
		SmtAdmittanceVehicleService vehicleService = Mockito.mock(SmtAdmittanceVehicleService.class);
		ApproveListService approveListService = Mockito.mock(ApproveListService.class);
		SmtStaffService staffService = Mockito.mock(SmtStaffService.class);
		SmtApprovalNodeService approvalNodeService = Mockito.mock(SmtApprovalNodeService.class);
		VisitorSelfQueryServiceImpl service = newService(mapper, smsService, redisTemplate, valueOperations, parkService,
				fellowService, vehicleService);
		setField(service, "approveListService", approveListService);
		setField(service, "smtStaffService", staffService);
		setField(service, "smtApprovalNodeService", approvalNodeService);
		Mockito.when(valueOperations.get("smart:admittance:visitor-query:tok-existing")).thenReturn("13712341234");
		SmtAdmittanceApply apply = apply(1011L, "李明", "13712341234", VisitorStatusEnum.Status_2.getCode());
		Mockito.when(mapper.selectList(Mockito.any())).thenReturn(Collections.singletonList(apply));
		Mockito.when(fellowService.getByApplyId(1011L)).thenReturn(Collections.emptyList());
		Mockito.when(vehicleService.getByApplyId(1011L)).thenReturn(Collections.emptyList());
		ApproveList pending = approve("被访人审批", ApproveListStateEnum.PENDING.getCode(), 2);
		pending.setApproveType(ApproveListTypeConstants.VISITOR);
		pending.setApproveBadge("A002");
		pending.setNodeId(20);
		Mockito.when(approveListService.list(Mockito.any())).thenReturn(Collections.singletonList(pending));
		Mockito.when(staffService.getSimpleSttaffByBadge("A002")).thenReturn(staff("张三"));
		Mockito.when(approvalNodeService.getById(20)).thenReturn(approvalNode("被访人审批"));

		VisitorSelfQueryRespDTO response = service.listMyApply(new VisitorSelfQueryReqDTO(), "tok-existing");

		Assert.assertEquals("PENDING", response.getRecords().get(0).getApplyStatus());
		Assert.assertEquals("被访人审批 张三 审批中", response.getRecords().get(0).getCurrentNode());
	}

	@Test
	public void getApplyDetailRejectsApplyOwnedByDifferentMobile() throws Exception {
		SmtAdmittanceApplyMapper mapper = Mockito.mock(SmtAdmittanceApplyMapper.class);
		RemoteAppSmsService smsService = Mockito.mock(RemoteAppSmsService.class);
		StringRedisTemplate redisTemplate = Mockito.mock(StringRedisTemplate.class);
		ValueOperations<String, String> valueOperations = Mockito.mock(ValueOperations.class);
		SmtParkService parkService = Mockito.mock(SmtParkService.class);
		SmtAdmittanceFellowService fellowService = Mockito.mock(SmtAdmittanceFellowService.class);
		SmtAdmittanceVehicleService vehicleService = Mockito.mock(SmtAdmittanceVehicleService.class);
		VisitorSelfQueryServiceImpl service = newService(mapper, smsService, redisTemplate, valueOperations, parkService,
				fellowService, vehicleService);
		Mockito.when(valueOperations.get("smart:admittance:visitor-query:tok-existing")).thenReturn("13712341234");
		Mockito.when(mapper.selectById(1003L)).thenReturn(
				apply(1003L, "王五", "13900000000", VisitorStatusEnum.Status_0.getCode()));

		try {
			service.getApplyDetail("1003", "tok-existing");
			Assert.fail("Expected detail access to reject a different visitor mobile");
		} catch (SmartException error) {
			Assert.assertEquals(Integer.valueOf(403), error.getCode());
		}
	}

	@Test
	public void getApplyDetailReturnsFullNamesAndMaskedPhoneForTokenOwner() throws Exception {
		SmtAdmittanceApplyMapper mapper = Mockito.mock(SmtAdmittanceApplyMapper.class);
		RemoteAppSmsService smsService = Mockito.mock(RemoteAppSmsService.class);
		StringRedisTemplate redisTemplate = Mockito.mock(StringRedisTemplate.class);
		ValueOperations<String, String> valueOperations = Mockito.mock(ValueOperations.class);
		SmtParkService parkService = Mockito.mock(SmtParkService.class);
		SmtAdmittanceFellowService fellowService = Mockito.mock(SmtAdmittanceFellowService.class);
		SmtAdmittanceVehicleService vehicleService = Mockito.mock(SmtAdmittanceVehicleService.class);
		VisitorSelfQueryServiceImpl service = newService(mapper, smsService, redisTemplate, valueOperations, parkService,
				fellowService, vehicleService);
		Mockito.when(valueOperations.get("smart:admittance:visitor-query:tok-existing")).thenReturn("13712341234");
		SmtPark park = new SmtPark();
		park.setParkName("裕同科技许昌园区");
		Mockito.when(parkService.getById(5000021)).thenReturn(park);
		SmtAdmittanceApply apply = apply(1004L, "李明", "13712341234", VisitorStatusEnum.Status_0.getCode());
		apply.setPermitArea("研发楼A座,样品展示厅");
		Mockito.when(mapper.selectById(1004L)).thenReturn(apply);
		Mockito.when(fellowService.getByApplyId(1004L)).thenReturn(Arrays.asList(fellow("李明", 1), fellow("赵六", 0)));
		Mockito.when(vehicleService.getByApplyId(1004L)).thenReturn(Collections.singletonList(vehicle("豫A12345")));

		VisitorApplyRecordDetailRespDTO detail = service.getApplyDetail("1004", "tok-existing");

		Assert.assertEquals("1004", detail.getApplyId());
		Assert.assertEquals("PASSED", detail.getApplyStatus());
		Assert.assertEquals("王强", detail.getReceptionistName());
		Assert.assertEquals("李明", detail.getVisitorName());
		Assert.assertEquals("137****1234", detail.getVisitorPhone());
		Assert.assertEquals(1, detail.getFellows().size());
		Assert.assertEquals("赵六", detail.getFellows().get(0).getName());
		Assert.assertEquals(Collections.singletonList("研发楼A座"), Collections.singletonList(detail.getAreas().get(0)));
	}

	@Test
	public void getApprovalProgressUsesOaProcessFlowWhenApplyHasProcessId() throws Exception {
		SmtAdmittanceApplyMapper mapper = Mockito.mock(SmtAdmittanceApplyMapper.class);
		RemoteAppSmsService smsService = Mockito.mock(RemoteAppSmsService.class);
		StringRedisTemplate redisTemplate = Mockito.mock(StringRedisTemplate.class);
		ValueOperations<String, String> valueOperations = Mockito.mock(ValueOperations.class);
		SmtParkService parkService = Mockito.mock(SmtParkService.class);
		SmtAdmittanceFellowService fellowService = Mockito.mock(SmtAdmittanceFellowService.class);
		SmtAdmittanceVehicleService vehicleService = Mockito.mock(SmtAdmittanceVehicleService.class);
		ApproveListService approveListService = Mockito.mock(ApproveListService.class);
		SmtOutDormitoryStaffService outDormitoryStaffService = Mockito.mock(SmtOutDormitoryStaffService.class);
		VisitorSelfQueryServiceImpl service = newService(mapper, smsService, redisTemplate, valueOperations, parkService,
				fellowService, vehicleService);
		setField(service, "approveListService", approveListService);
		setField(service, "smtOutDormitoryStaffService", outDormitoryStaffService);
		Mockito.when(valueOperations.get("smart:admittance:visitor-query:tok-existing")).thenReturn("13712341234");
		SmtAdmittanceApply apply = apply(1005L, "李明", "13712341234", VisitorStatusEnum.Status_2.getCode());
		apply.setProcessId("OA-9001");
		Mockito.when(mapper.selectById(1005L)).thenReturn(apply);
		Mockito.doAnswer(invocation -> {
			List<FlowVO> flowList = (List<FlowVO>) invocation.getArguments()[1];
			flowList.add(flow("提交申请", ApplicationEnum.RECORD_STATUS_2.getDesc(), "李明",
					LocalDateTime.of(2026, 6, 13, 9, 5), "已提交 OA"));
			flowList.add(flow("部门经理审批", ApplicationEnum.RECORD_STATUS_c.getDesc(), "张三", null, ""));
			return null;
		}).when(outDormitoryStaffService).getOAProcessFlow(Mockito.eq("OA-9001"), Mockito.anyList());

		VisitorApprovalProgressRespDTO progress = service.getApprovalProgress("1005", "tok-existing");

		Assert.assertEquals(2, progress.getNodes().size());
		Assert.assertEquals("提交申请", progress.getNodes().get(0).getTitle());
		Assert.assertEquals("done", progress.getNodes().get(0).getState());
		Assert.assertEquals("提交", progress.getNodes().get(0).getStatusText());
		Assert.assertEquals("李明", progress.getNodes().get(0).getApproverName());
		Assert.assertEquals("2026-06-13 09:05", progress.getNodes().get(0).getTime());
		Assert.assertEquals("已提交 OA", progress.getNodes().get(0).getComment());
		Assert.assertEquals("部门经理审批", progress.getNodes().get(1).getTitle());
		Assert.assertEquals("current", progress.getNodes().get(1).getState());
		Assert.assertEquals("当前审批人", progress.getNodes().get(1).getStatusText());
		Assert.assertEquals("张三", progress.getNodes().get(1).getApproverName());
		Mockito.verify(outDormitoryStaffService).getOAProcessFlow(Mockito.eq("OA-9001"), Mockito.anyList());
		Mockito.verify(approveListService, Mockito.never()).list(Mockito.any());
	}

	@Test
	public void getApprovalProgressMapsOaRejectedStatuses() throws Exception {
		SmtAdmittanceApplyMapper mapper = Mockito.mock(SmtAdmittanceApplyMapper.class);
		RemoteAppSmsService smsService = Mockito.mock(RemoteAppSmsService.class);
		StringRedisTemplate redisTemplate = Mockito.mock(StringRedisTemplate.class);
		ValueOperations<String, String> valueOperations = Mockito.mock(ValueOperations.class);
		SmtParkService parkService = Mockito.mock(SmtParkService.class);
		SmtAdmittanceFellowService fellowService = Mockito.mock(SmtAdmittanceFellowService.class);
		SmtAdmittanceVehicleService vehicleService = Mockito.mock(SmtAdmittanceVehicleService.class);
		ApproveListService approveListService = Mockito.mock(ApproveListService.class);
		SmtOutDormitoryStaffService outDormitoryStaffService = Mockito.mock(SmtOutDormitoryStaffService.class);
		VisitorSelfQueryServiceImpl service = newService(mapper, smsService, redisTemplate, valueOperations, parkService,
				fellowService, vehicleService);
		setField(service, "approveListService", approveListService);
		setField(service, "smtOutDormitoryStaffService", outDormitoryStaffService);
		Mockito.when(valueOperations.get("smart:admittance:visitor-query:tok-existing")).thenReturn("13712341234");
		SmtAdmittanceApply apply = apply(1008L, "李明", "13712341234", VisitorStatusEnum.Status_1.getCode());
		apply.setProcessId("OA-9002");
		Mockito.when(mapper.selectById(1008L)).thenReturn(apply);
		Mockito.doAnswer(invocation -> {
			List<FlowVO> flowList = (List<FlowVO>) invocation.getArguments()[1];
			flowList.add(flow("部门经理审批", ApplicationEnum.RECORD_STATUS_3.getDesc(), "张三",
					LocalDateTime.of(2026, 6, 13, 10, 15), "资料不完整"));
			flowList.add(flow("厂区负责人审批", ApplicationEnum.RECORD_STATUS_12.getDesc(), "赵六",
					LocalDateTime.of(2026, 6, 13, 10, 20), "回退补充"));
			flowList.add(flow("系统撤销", ApplicationEnum.RECORD_STATUS_13.getDesc(), "王强",
					LocalDateTime.of(2026, 6, 13, 10, 25), "申请撤销"));
			return null;
		}).when(outDormitoryStaffService).getOAProcessFlow(Mockito.eq("OA-9002"), Mockito.anyList());

		VisitorApprovalProgressRespDTO progress = service.getApprovalProgress("1008", "tok-existing");

		Assert.assertEquals(3, progress.getNodes().size());
		Assert.assertEquals("rejected", progress.getNodes().get(0).getState());
		Assert.assertEquals("张三", progress.getNodes().get(0).getApproverName());
		Assert.assertEquals("2026-06-13 10:15", progress.getNodes().get(0).getTime());
		Assert.assertEquals("资料不完整", progress.getNodes().get(0).getComment());
		Assert.assertEquals("rejected", progress.getNodes().get(1).getState());
		Assert.assertEquals("赵六", progress.getNodes().get(1).getApproverName());
		Assert.assertEquals("rejected", progress.getNodes().get(2).getState());
		Assert.assertEquals("王强", progress.getNodes().get(2).getApproverName());
		Mockito.verify(approveListService, Mockito.never()).list(Mockito.any());
	}

	@Test
	public void getApprovalProgressDoesNotFallbackToApproveListWhenOaReturnsNoNodes() throws Exception {
		SmtAdmittanceApplyMapper mapper = Mockito.mock(SmtAdmittanceApplyMapper.class);
		RemoteAppSmsService smsService = Mockito.mock(RemoteAppSmsService.class);
		StringRedisTemplate redisTemplate = Mockito.mock(StringRedisTemplate.class);
		ValueOperations<String, String> valueOperations = Mockito.mock(ValueOperations.class);
		SmtParkService parkService = Mockito.mock(SmtParkService.class);
		SmtAdmittanceFellowService fellowService = Mockito.mock(SmtAdmittanceFellowService.class);
		SmtAdmittanceVehicleService vehicleService = Mockito.mock(SmtAdmittanceVehicleService.class);
		ApproveListService approveListService = Mockito.mock(ApproveListService.class);
		SmtOutDormitoryStaffService outDormitoryStaffService = Mockito.mock(SmtOutDormitoryStaffService.class);
		VisitorSelfQueryServiceImpl service = newService(mapper, smsService, redisTemplate, valueOperations, parkService,
				fellowService, vehicleService);
		setField(service, "approveListService", approveListService);
		setField(service, "smtOutDormitoryStaffService", outDormitoryStaffService);
		Mockito.when(valueOperations.get("smart:admittance:visitor-query:tok-existing")).thenReturn("13712341234");
		SmtAdmittanceApply apply = apply(1009L, "李明", "13712341234", VisitorStatusEnum.Status_2.getCode());
		apply.setProcessId("OA-9003");
		Mockito.when(mapper.selectById(1009L)).thenReturn(apply);
		Mockito.when(approveListService.list(Mockito.any())).thenReturn(Collections.singletonList(
				approve("本地审批节点", ApproveListStateEnum.PENDING.getCode(), 1)));

		VisitorApprovalProgressRespDTO progress = service.getApprovalProgress("1009", "tok-existing");

		Assert.assertTrue(progress.getNodes().isEmpty());
		Mockito.verify(outDormitoryStaffService).getOAProcessFlow(Mockito.eq("OA-9003"), Mockito.anyList());
		Mockito.verify(approveListService, Mockito.never()).list(Mockito.any());
	}

	@Test
	public void getApprovalProgressMapsApproveStatesForTokenOwner() throws Exception {
		SmtAdmittanceApplyMapper mapper = Mockito.mock(SmtAdmittanceApplyMapper.class);
		RemoteAppSmsService smsService = Mockito.mock(RemoteAppSmsService.class);
		StringRedisTemplate redisTemplate = Mockito.mock(StringRedisTemplate.class);
		ValueOperations<String, String> valueOperations = Mockito.mock(ValueOperations.class);
		SmtParkService parkService = Mockito.mock(SmtParkService.class);
		SmtAdmittanceFellowService fellowService = Mockito.mock(SmtAdmittanceFellowService.class);
		SmtAdmittanceVehicleService vehicleService = Mockito.mock(SmtAdmittanceVehicleService.class);
		ApproveListService approveListService = Mockito.mock(ApproveListService.class);
		SmtStaffService staffService = Mockito.mock(SmtStaffService.class);
		SmtApprovalNodeService approvalNodeService = Mockito.mock(SmtApprovalNodeService.class);
		VisitorSelfQueryServiceImpl service = newService(mapper, smsService, redisTemplate, valueOperations, parkService,
				fellowService, vehicleService);
		setField(service, "approveListService", approveListService);
		setField(service, "smtStaffService", staffService);
		setField(service, "smtApprovalNodeService", approvalNodeService);
		Mockito.when(valueOperations.get("smart:admittance:visitor-query:tok-existing")).thenReturn("13712341234");
		Mockito.when(mapper.selectById(1005L)).thenReturn(
				apply(1005L, "李明", "13712341234", VisitorStatusEnum.Status_2.getCode()));
		ApproveList agreed = approve("提交申请", ApproveListStateEnum.AGREE.getCode(), 1);
		ApproveList pending = approve("被访人审批", ApproveListStateEnum.PENDING.getCode(), 2);
		agreed.setApproveType(ApproveListTypeConstants.VISITOR);
		agreed.setApproveBadge("A001");
		agreed.setNodeId(10);
		pending.setApproveType(ApproveListTypeConstants.VISITOR);
		pending.setApproveBadge("A002");
		pending.setNodeId(20);
		Mockito.when(approveListService.list(Mockito.any())).thenReturn(Arrays.asList(pending, agreed));
		Mockito.when(staffService.getSimpleSttaffByBadge("A001")).thenReturn(staff("王强"));
		Mockito.when(staffService.getSimpleSttaffByBadge("A002")).thenReturn(staff("张三"));
		Mockito.when(approvalNodeService.getById(10)).thenReturn(approvalNode("提交申请"));
		Mockito.when(approvalNodeService.getById(20)).thenReturn(approvalNode("被访人审批"));

		VisitorApprovalProgressRespDTO progress = service.getApprovalProgress("1005", "tok-existing");

		Assert.assertEquals(2, progress.getNodes().size());
		Assert.assertEquals("提交申请", progress.getNodes().get(0).getTitle());
		Assert.assertEquals("done", progress.getNodes().get(0).getState());
		Assert.assertEquals("王强", progress.getNodes().get(0).getApproverName());
		Assert.assertEquals("被访人审批", progress.getNodes().get(1).getTitle());
		Assert.assertEquals("current", progress.getNodes().get(1).getState());
		Assert.assertEquals("张三", progress.getNodes().get(1).getApproverName());
	}

	@Test
	public void getApprovalProgressRejectsApplyOwnedByDifferentMobile() throws Exception {
		SmtAdmittanceApplyMapper mapper = Mockito.mock(SmtAdmittanceApplyMapper.class);
		RemoteAppSmsService smsService = Mockito.mock(RemoteAppSmsService.class);
		StringRedisTemplate redisTemplate = Mockito.mock(StringRedisTemplate.class);
		ValueOperations<String, String> valueOperations = Mockito.mock(ValueOperations.class);
		SmtParkService parkService = Mockito.mock(SmtParkService.class);
		SmtAdmittanceFellowService fellowService = Mockito.mock(SmtAdmittanceFellowService.class);
		SmtAdmittanceVehicleService vehicleService = Mockito.mock(SmtAdmittanceVehicleService.class);
		VisitorSelfQueryServiceImpl service = newService(mapper, smsService, redisTemplate, valueOperations, parkService,
				fellowService, vehicleService);
		Mockito.when(valueOperations.get("smart:admittance:visitor-query:tok-existing")).thenReturn("13712341234");
		Mockito.when(mapper.selectById(1006L)).thenReturn(
				apply(1006L, "王五", "13900000000", VisitorStatusEnum.Status_2.getCode()));

		try {
			service.getApprovalProgress("1006", "tok-existing");
			Assert.fail("Expected progress access to reject a different visitor mobile");
		} catch (SmartException error) {
			Assert.assertEquals(Integer.valueOf(403), error.getCode());
		}
	}

	@Test
	public void getApprovalProgressFiltersVisitorApproveTypeAndGroupsSameNodeApprovers() throws Exception {
		SmtAdmittanceApplyMapper mapper = Mockito.mock(SmtAdmittanceApplyMapper.class);
		RemoteAppSmsService smsService = Mockito.mock(RemoteAppSmsService.class);
		StringRedisTemplate redisTemplate = Mockito.mock(StringRedisTemplate.class);
		ValueOperations<String, String> valueOperations = Mockito.mock(ValueOperations.class);
		SmtParkService parkService = Mockito.mock(SmtParkService.class);
		SmtAdmittanceFellowService fellowService = Mockito.mock(SmtAdmittanceFellowService.class);
		SmtAdmittanceVehicleService vehicleService = Mockito.mock(SmtAdmittanceVehicleService.class);
		ApproveListService approveListService = Mockito.mock(ApproveListService.class);
		SmtStaffService staffService = Mockito.mock(SmtStaffService.class);
		SmtApprovalNodeService approvalNodeService = Mockito.mock(SmtApprovalNodeService.class);
		VisitorSelfQueryServiceImpl service = newService(mapper, smsService, redisTemplate, valueOperations, parkService,
				fellowService, vehicleService);
		setField(service, "approveListService", approveListService);
		setField(service, "smtStaffService", staffService);
		setField(service, "smtApprovalNodeService", approvalNodeService);
		Mockito.when(valueOperations.get("smart:admittance:visitor-query:tok-existing")).thenReturn("13712341234");
		Mockito.when(mapper.selectById(1007L)).thenReturn(
				apply(1007L, "李明", "13712341234", VisitorStatusEnum.Status_2.getCode()));
		ApproveList visitorAgreed = approve("访客申请", ApproveListStateEnum.AGREE.getCode(), 1);
		visitorAgreed.setApproveType(ApproveListTypeConstants.VISITOR);
		visitorAgreed.setApproveBadge("A001");
		visitorAgreed.setNodeId(10);
		ApproveList closedPeer = approve("访客申请", ApproveListStateEnum.CLOSE.getCode(), 1);
		closedPeer.setApproveType(ApproveListTypeConstants.VISITOR);
		closedPeer.setApproveBadge("A002");
		closedPeer.setNodeId(10);
		ApproveList waiting = approve("访客申请", ApproveListStateEnum.WAITING.getCode(), 2);
		waiting.setApproveType(ApproveListTypeConstants.VISITOR);
		waiting.setApproveBadge("A003");
		waiting.setNodeId(20);
		ApproveList articleApproval = approve("物品放行", ApproveListStateEnum.PENDING.getCode(), 1);
		articleApproval.setApproveType(3);
		articleApproval.setApproveBadge("B001");
		articleApproval.setNodeId(30);
		Mockito.when(approveListService.list(Mockito.any())).thenReturn(Arrays.asList(articleApproval, closedPeer, waiting, visitorAgreed));
		Mockito.when(staffService.getSimpleSttaffByBadge("A001")).thenReturn(staff("王强"));
		Mockito.when(staffService.getSimpleSttaffByBadge("A003")).thenReturn(staff("赵六"));
		Mockito.when(approvalNodeService.getById(10)).thenReturn(approvalNode("被访人审批"));
		Mockito.when(approvalNodeService.getById(20)).thenReturn(approvalNode("门岗审批"));

		VisitorApprovalProgressRespDTO progress = service.getApprovalProgress("1007", "tok-existing");

		Assert.assertEquals(2, progress.getNodes().size());
		Assert.assertEquals("被访人审批", progress.getNodes().get(0).getTitle());
		Assert.assertEquals("done", progress.getNodes().get(0).getState());
		Assert.assertEquals("王强", progress.getNodes().get(0).getApproverName());
		Assert.assertEquals("门岗审批", progress.getNodes().get(1).getTitle());
		Assert.assertEquals("wait", progress.getNodes().get(1).getState());
		Assert.assertEquals("赵六", progress.getNodes().get(1).getApproverName());
	}

	@Test
	public void deviceStatusAlready_mapsToIssuing() throws Exception {
		// 测试设备状态 ALRAEDY(4) 映射到 ISSUING 过渡态
		// 场景：任务已提交ISC等待确认，H5自查接口应返回ISSUING状态
		SmtAdmittanceApplyMapper mapper = Mockito.mock(SmtAdmittanceApplyMapper.class);
		RemoteAppSmsService smsService = Mockito.mock(RemoteAppSmsService.class);
		StringRedisTemplate redisTemplate = Mockito.mock(StringRedisTemplate.class);
		ValueOperations<String, String> valueOperations = Mockito.mock(ValueOperations.class);
		SmtParkService parkService = Mockito.mock(SmtParkService.class);
		SmtAdmittanceFellowService fellowService = Mockito.mock(SmtAdmittanceFellowService.class);
		SmtAdmittanceVehicleService vehicleService = Mockito.mock(SmtAdmittanceVehicleService.class);
		VisitorSelfQueryServiceImpl service = newService(mapper, smsService, redisTemplate, valueOperations, parkService,
				fellowService, vehicleService);
		Mockito.when(valueOperations.get("smart:admittance:visitor-query:tok-existing")).thenReturn("13712341234");
		SmtPark park = new SmtPark();
		park.setParkName("裕同科技许昌园区");
		Mockito.when(parkService.getById(5000021)).thenReturn(park);
		SmtAdmittanceApply apply = apply(2001L, "李明", "13712341234", VisitorStatusEnum.Status_0.getCode());
		apply.setDeviceStatus(DeviceDownStatusEnum.ALRAEDY.getCode()); // 状态码 4
		apply.setEndTime(LocalDateTime.of(2026, 12, 31, 23, 59)); // 设为未来日期，避免过期判定
		Mockito.when(mapper.selectList(Mockito.any())).thenReturn(Collections.singletonList(apply));
		Mockito.when(fellowService.getByApplyId(2001L)).thenReturn(Collections.emptyList());
		Mockito.when(vehicleService.getByApplyId(2001L)).thenReturn(Collections.emptyList());

		VisitorSelfQueryRespDTO response = service.listMyApply(new VisitorSelfQueryReqDTO(), "tok-existing");

		Assert.assertEquals(1, response.getRecords().size());
		Assert.assertEquals("PASSED", response.getRecords().get(0).getApplyStatus());
		Assert.assertEquals("ISSUING", response.getRecords().get(0).getDispatchStatus());
	}

	@Test
	public void deviceStatusSuccess_mapsToSuccess() throws Exception {
		// 测试设备状态 SUCCESS(1) 映射到 SUCCESS（保持不变）
		// 场景：照片已下发到设备成功，H5自查接口应返回SUCCESS状态
		SmtAdmittanceApplyMapper mapper = Mockito.mock(SmtAdmittanceApplyMapper.class);
		RemoteAppSmsService smsService = Mockito.mock(RemoteAppSmsService.class);
		StringRedisTemplate redisTemplate = Mockito.mock(StringRedisTemplate.class);
		ValueOperations<String, String> valueOperations = Mockito.mock(ValueOperations.class);
		SmtParkService parkService = Mockito.mock(SmtParkService.class);
		SmtAdmittanceFellowService fellowService = Mockito.mock(SmtAdmittanceFellowService.class);
		SmtAdmittanceVehicleService vehicleService = Mockito.mock(SmtAdmittanceVehicleService.class);
		VisitorSelfQueryServiceImpl service = newService(mapper, smsService, redisTemplate, valueOperations, parkService,
				fellowService, vehicleService);
		Mockito.when(valueOperations.get("smart:admittance:visitor-query:tok-existing")).thenReturn("13712341234");
		SmtPark park = new SmtPark();
		park.setParkName("裕同科技许昌园区");
		Mockito.when(parkService.getById(5000021)).thenReturn(park);
		SmtAdmittanceApply apply = apply(2002L, "李明", "13712341234", VisitorStatusEnum.Status_0.getCode());
		apply.setDeviceStatus(DeviceDownStatusEnum.SUCCESS.getCode()); // 状态码 1
		apply.setEndTime(LocalDateTime.of(2026, 12, 31, 23, 59)); // 设为未来日期，避免过期判定
		Mockito.when(mapper.selectList(Mockito.any())).thenReturn(Collections.singletonList(apply));
		Mockito.when(fellowService.getByApplyId(2002L)).thenReturn(Collections.emptyList());
		Mockito.when(vehicleService.getByApplyId(2002L)).thenReturn(Collections.emptyList());

		VisitorSelfQueryRespDTO response = service.listMyApply(new VisitorSelfQueryReqDTO(), "tok-existing");

		Assert.assertEquals(1, response.getRecords().size());
		Assert.assertEquals("PASSED", response.getRecords().get(0).getApplyStatus());
		Assert.assertEquals("SUCCESS", response.getRecords().get(0).getDispatchStatus());
	}

	private VisitorSelfQueryServiceImpl newService(SmtAdmittanceApplyMapper mapper, RemoteAppSmsService smsService,
			StringRedisTemplate redisTemplate, ValueOperations<String, String> valueOperations, SmtParkService parkService,
			SmtAdmittanceFellowService fellowService, SmtAdmittanceVehicleService vehicleService) throws Exception {
		Mockito.when(redisTemplate.opsForValue()).thenReturn(valueOperations);
		VisitorSelfQueryServiceImpl service = new VisitorSelfQueryServiceImpl();
		setField(service, "baseMapper", mapper);
		setField(service, "remoteAppSmsService", smsService);
		setField(service, "stringRedisTemplate", redisTemplate);
		setField(service, "smtParkService", parkService);
		setField(service, "smtAdmittanceFellowService", fellowService);
		setField(service, "smtAdmittanceVehicleService", vehicleService);
		return service;
	}

	private SmtAdmittanceApply apply(Long id, String visitorName, String mobile, Integer status) {
		SmtAdmittanceApply apply = new SmtAdmittanceApply();
		apply.setId(id);
		apply.setParkId(5000021);
		apply.setVisitorName(visitorName);
		apply.setVisitorPhone(mobile);
		apply.setReceptionistName("王强");
		apply.setStatus(status);
		apply.setStartTime(LocalDateTime.of(2026, 6, 15, 9, 30));
		apply.setEndTime(LocalDateTime.of(2026, 6, 15, 18, 0));
		apply.setCreateTime(LocalDateTime.of(2026, 6, 13, 8, 0));
		apply.setCause(1);
		return apply;
	}

	private SmtAdmittanceFellow fellow(String name, Integer isMain) {
		SmtAdmittanceFellow fellow = new SmtAdmittanceFellow();
		fellow.setFellowName(name);
		fellow.setIsMain(isMain);
		return fellow;
	}

	private SmtAdmittanceVehicle vehicle(String plate) {
		SmtAdmittanceVehicle vehicle = new SmtAdmittanceVehicle();
		vehicle.setPlate(plate);
		return vehicle;
	}

	private ApproveList approve(String name, Integer state, Integer sort) {
		ApproveList approve = new ApproveList();
		approve.setApproveName(name);
		approve.setApproveState(state);
		approve.setSort(sort);
		approve.setUpdateTime(LocalDateTime.of(2026, 6, 13, 9, sort));
		return approve;
	}

	private FlowVO flow(String nodeName, String processDesc, String createUser, LocalDateTime processDate, String remark) {
		FlowVO flow = new FlowVO();
		flow.setNodeName(nodeName);
		flow.setProcessDesc(processDesc);
		flow.setCreateUser(createUser);
		flow.setProcessDate(toDate(processDate));
		flow.setRemark(remark);
		return flow;
	}

	private Date toDate(LocalDateTime time) {
		return time == null ? null : Date.from(time.atZone(ZoneId.systemDefault()).toInstant());
	}

	private SmtStaff staff(String name) {
		SmtStaff staff = new SmtStaff();
		staff.setName(name);
		return staff;
	}

	private SmtApprovalNode approvalNode(String name) {
		SmtApprovalNode approvalNode = new SmtApprovalNode();
		approvalNode.setName(name);
		return approvalNode;
	}

	private void setField(Object target, String name, Object value) throws Exception {
		Field field = findField(target.getClass(), name);
		field.setAccessible(true);
		field.set(target, value);
	}

	private Field findField(Class<?> type, String name) throws NoSuchFieldException {
		Class<?> cursor = type;
		while (cursor != null) {
			try {
				return cursor.getDeclaredField(name);
			} catch (NoSuchFieldException ignored) {
				cursor = cursor.getSuperclass();
			}
		}
		throw new NoSuchFieldException(name);
	}
}
