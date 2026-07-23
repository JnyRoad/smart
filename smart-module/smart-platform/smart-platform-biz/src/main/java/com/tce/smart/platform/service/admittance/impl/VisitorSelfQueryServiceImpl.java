package com.tce.smart.platform.service.admittance.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tce.smart.app.api.dto.InternalSmsVerifyReqDTO;
import com.tce.smart.app.api.feign.RemoteAppSmsService;
import com.tce.smart.common.core.constant.SecurityConstants;
import com.tce.smart.common.core.exception.SmartException;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.platform.api.dto.req.admittance.VisitorSelfQueryReqDTO;
import com.tce.smart.platform.api.dto.resp.admittance.VisitorApplyFellowRespDTO;
import com.tce.smart.platform.api.dto.resp.admittance.VisitorApplyRecordDetailRespDTO;
import com.tce.smart.platform.api.dto.resp.admittance.VisitorApplyRecordRespDTO;
import com.tce.smart.platform.api.dto.resp.admittance.VisitorApplyVehicleRespDTO;
import com.tce.smart.platform.api.dto.resp.admittance.VisitorApprovalNodeRespDTO;
import com.tce.smart.platform.api.dto.resp.admittance.VisitorApprovalProgressRespDTO;
import com.tce.smart.platform.api.dto.resp.admittance.VisitorSelfQueryRespDTO;
import com.tce.smart.platform.core.entity.ApproveList;
import com.tce.smart.platform.core.entity.SmtApprovalNode;
import com.tce.smart.platform.core.entity.SmtPark;
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
import com.tce.smart.platform.service.admittance.VisitorSelfQueryService;
import com.tce.smart.tool.constant.ApproveListTypeConstants;
import com.tce.smart.tool.enums.AdmittanceCarCauseEnum;
import com.tce.smart.tool.enums.AdmittanceCauseEnum;
import com.tce.smart.tool.enums.AdmittanceTypeEnum;
import com.tce.smart.tool.enums.ApplicationEnum;
import com.tce.smart.tool.enums.ApproveListStateEnum;
import com.tce.smart.tool.enums.DeviceDownStatusEnum;
import com.tce.smart.tool.enums.VisitorStatusEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Service
public class VisitorSelfQueryServiceImpl extends ServiceImpl<SmtAdmittanceApplyMapper, SmtAdmittanceApply>
		implements VisitorSelfQueryService {

	private static final String TOKEN_KEY_PREFIX = "smart:admittance:visitor-query:";
	private static final long TOKEN_TTL_DAYS = 1L;
	private static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

	@Autowired
	private RemoteAppSmsService remoteAppSmsService;
	@Autowired
	private StringRedisTemplate stringRedisTemplate;
	@Autowired
	private SmtParkService smtParkService;
	@Autowired
	private SmtAdmittanceFellowService smtAdmittanceFellowService;
	@Autowired
	private SmtAdmittanceVehicleService smtAdmittanceVehicleService;
	@Autowired
	private ApproveListService approveListService;
	@Autowired
	private SmtStaffService smtStaffService;
	@Autowired
	private SmtApprovalNodeService smtApprovalNodeService;
	@Autowired
	private SmtOutDormitoryStaffService smtOutDormitoryStaffService;

	private Supplier<String> queryTokenSupplier = () -> UUID.randomUUID().toString().replace("-", "");

	@Override
	public VisitorSelfQueryRespDTO listMyApply(VisitorSelfQueryReqDTO request, String queryToken) {
		QueryCredential credential = resolveCredential(request, queryToken);
		List<SmtAdmittanceApply> applies = queryAppliesByMobile(credential.mobile);
		VisitorSelfQueryRespDTO response = new VisitorSelfQueryRespDTO();
		response.setQueryToken(credential.queryToken);
		response.setMaskedMobile(maskPhone(credential.mobile));
		response.setMaskedName(applies.isEmpty() ? "" : trim(applies.get(0).getVisitorName()));
		response.setRecords(applies.stream().map(this::toRecord).collect(Collectors.toList()));
		return response;
	}

	@Override
	public VisitorApplyRecordDetailRespDTO getApplyDetail(String applyId, String queryToken) {
		SmtAdmittanceApply apply = requireOwnedApply(applyId, queryToken);
		return toDetail(apply);
	}

	@Override
	public VisitorApprovalProgressRespDTO getApprovalProgress(String applyId, String queryToken) {
		SmtAdmittanceApply apply = requireOwnedApply(applyId, queryToken);
		VisitorApprovalProgressRespDTO response = new VisitorApprovalProgressRespDTO();
		response.setNodes(approvalNodes(apply));
		return response;
	}

	private QueryCredential resolveCredential(VisitorSelfQueryReqDTO request, String queryToken) {
		String cleanedToken = trim(queryToken);
		if (StringUtils.hasText(cleanedToken)) {
			String mobile = mobileFromToken(cleanedToken);
			return new QueryCredential(cleanedToken, mobile);
		}
		if (request == null) {
			throw forbidden("请先完成身份验证");
		}
		String mobile = trim(request.getMobile());
		String smsCode = trim(request.getSmsCode());
		if (StringUtils.hasText(mobile) && StringUtils.hasText(smsCode)) {
			verifySmsCode(mobile, smsCode);
			return new QueryCredential(issueToken(mobile), mobile);
		}
		throw forbidden("请先完成身份验证");
	}

	private void verifySmsCode(String mobile, String smsCode) {
		InternalSmsVerifyReqDTO request = new InternalSmsVerifyReqDTO();
		request.setMobile(mobile);
		request.setSmsCode(smsCode);
		Result<Boolean> result = remoteAppSmsService.verifySmsCode(request, SecurityConstants.FROM_IN,
				SecurityConstants.INTERNAL_SERVICE_AUTH_REQUIRED);
		if (result == null || !result.isSuccess() || !Boolean.TRUE.equals(result.getData())) {
			throw new SmartException("验证码错误或已过期");
		}
	}

	private String issueToken(String mobile) {
		String token = queryTokenSupplier.get();
		stringRedisTemplate.opsForValue().set(tokenKey(token), mobile, TOKEN_TTL_DAYS, TimeUnit.DAYS);
		return token;
	}

	private String mobileFromToken(String token) {
		String mobile = stringRedisTemplate.opsForValue().get(tokenKey(token));
		if (!StringUtils.hasText(mobile)) {
			throw forbidden("token expired");
		}
		return mobile;
	}

	private List<SmtAdmittanceApply> queryAppliesByMobile(String mobile) {
		List<SmtAdmittanceApply> applies = baseMapper.selectList(Wrappers.<SmtAdmittanceApply>lambdaQuery()
				.eq(SmtAdmittanceApply::getVisitorPhone, mobile)
				.orderByDesc(SmtAdmittanceApply::getCreateTime));
		return applies == null ? Collections.emptyList() : applies;
	}

	private SmtAdmittanceApply requireOwnedApply(String applyId, String queryToken) {
		String mobile = mobileFromToken(trim(queryToken));
		Long id = parseApplyId(applyId);
		SmtAdmittanceApply apply = baseMapper.selectById(id);
		if (apply == null) {
			throw new SmartException("申请单不存在");
		}
		if (!Objects.equals(mobile, apply.getVisitorPhone())) {
			throw forbidden("无权查看该申请");
		}
		return apply;
	}

	private Long parseApplyId(String applyId) {
		try {
			return Long.parseLong(trim(applyId));
		} catch (Exception error) {
			throw new SmartException("申请单不存在");
		}
	}

	private VisitorApplyRecordRespDTO toRecord(SmtAdmittanceApply apply) {
		VisitorApplyRecordRespDTO record = new VisitorApplyRecordRespDTO();
		record.setApplyId(String.valueOf(apply.getId()));
		record.setParkName(parkName(apply));
		record.setApplyStatus(applyStatus(apply));
		record.setReceptionistName(trim(apply.getReceptionistName()));
		record.setStartTime(format(apply.getStartTime()));
		record.setEndTime(format(apply.getEndTime()));
		record.setFellowCount(nonMainFellows(apply.getId()).size());
		record.setPlates(vehicles(apply.getId()).stream()
				.map(SmtAdmittanceVehicle::getPlate)
				.filter(StringUtils::hasText)
				.collect(Collectors.toList()));
		record.setSubmitTime(format(apply.getCreateTime()));
		if ("PENDING".equals(record.getApplyStatus())) {
			record.setCurrentNode(currentNode(apply));
		}
		if ("PASSED".equals(record.getApplyStatus())) {
			record.setDispatchStatus(dispatchStatus(apply));
		}
		return record;
	}

	private VisitorApplyRecordDetailRespDTO toDetail(SmtAdmittanceApply apply) {
		VisitorApplyRecordDetailRespDTO detail = new VisitorApplyRecordDetailRespDTO();
		detail.setApplyId(String.valueOf(apply.getId()));
		detail.setApplyNo(String.valueOf(apply.getId()));
		detail.setParkName(parkName(apply));
		detail.setApplyStatus(applyStatus(apply));
		if ("PASSED".equals(detail.getApplyStatus())) {
			detail.setDispatchStatus(dispatchStatus(apply));
		}
		detail.setReceptionistName(trim(apply.getReceptionistName()));
		detail.setStartTime(format(apply.getStartTime()));
		detail.setEndTime(format(apply.getEndTime()));
		detail.setCause(causeDesc(apply));
		detail.setVisitorName(trim(apply.getVisitorName()));
		detail.setVisitorPhone(maskPhone(apply.getVisitorPhone()));
		detail.setFellows(nonMainFellows(apply.getId()).stream().map(this::toFellow).collect(Collectors.toList()));
		detail.setVehicles(vehicles(apply.getId()).stream().map(this::toVehicle).collect(Collectors.toList()));
		detail.setAreas(areas(apply));
		detail.setSubmitTime(format(apply.getCreateTime()));
		return detail;
	}

	private VisitorApplyFellowRespDTO toFellow(SmtAdmittanceFellow fellow) {
		VisitorApplyFellowRespDTO response = new VisitorApplyFellowRespDTO();
		response.setName(trim(fellow.getFellowName()));
		response.setPhone("");
		return response;
	}

	private VisitorApplyVehicleRespDTO toVehicle(SmtAdmittanceVehicle vehicle) {
		VisitorApplyVehicleRespDTO response = new VisitorApplyVehicleRespDTO();
		response.setPlate(vehicle.getPlate());
		response.setType(vehicle.getModle());
		return response;
	}

	private List<VisitorApprovalNodeRespDTO> approvalNodes(SmtAdmittanceApply apply) {
		if (StringUtils.hasText(apply.getProcessId())) {
			return toOaApprovalNodes(oaProcessFlow(apply.getProcessId()));
		}
		return toApprovalNodes(visitorApproves(String.valueOf(apply.getId())));
	}

	private List<FlowVO> oaProcessFlow(String processId) {
		List<FlowVO> flowList = new ArrayList<>();
		smtOutDormitoryStaffService.getOAProcessFlow(processId, flowList);
		return flowList;
	}

	private List<VisitorApprovalNodeRespDTO> toOaApprovalNodes(List<FlowVO> flowList) {
		if (flowList == null) {
			return Collections.emptyList();
		}
		return flowList.stream()
				.filter(Objects::nonNull)
				.map(this::toOaApprovalNode)
				.collect(Collectors.toList());
	}

	private VisitorApprovalNodeRespDTO toOaApprovalNode(FlowVO flow) {
		VisitorApprovalNodeRespDTO node = new VisitorApprovalNodeRespDTO();
		node.setTitle(StringUtils.hasText(trim(flow.getNodeName())) ? trim(flow.getNodeName()) : "审批节点");
		node.setState(oaNodeState(flow));
		node.setStatusText(trim(flow.getProcessDesc()));
		node.setApproverName(trim(flow.getCreateUser()));
		node.setTime(format(flow.getProcessDate()));
		node.setComment(trim(flow.getRemark()));
		return node;
	}

	private String oaNodeState(FlowVO flow) {
		String processDesc = trim(flow.getProcessDesc());
		if (Objects.equals(processDesc, ApplicationEnum.RECORD_STATUS_3.getDesc())
				|| Objects.equals(processDesc, ApplicationEnum.RECORD_STATUS_12.getDesc())
				|| Objects.equals(processDesc, ApplicationEnum.RECORD_STATUS_13.getDesc())) {
			return "rejected";
		}
		if (Objects.equals(processDesc, ApplicationEnum.RECORD_STATUS_c.getDesc())) {
			return "current";
		}
		return "done";
	}

	private List<VisitorApprovalNodeRespDTO> toApprovalNodes(List<ApproveList> approveList) {
		if (approveList == null) {
			return Collections.emptyList();
		}
		Map<Integer, List<ApproveList>> approveBySort = approveList.stream()
				.filter(Objects::nonNull)
				.filter(approve -> Objects.equals(approve.getApproveType(), ApproveListTypeConstants.VISITOR))
				.sorted(Comparator.comparing(ApproveList::getSort, Comparator.nullsLast(Integer::compareTo)))
				.collect(Collectors.groupingBy(approve -> approve.getSort() == null ? Integer.MAX_VALUE : approve.getSort(),
						LinkedHashMap::new, Collectors.toList()));
		return approveBySort.values().stream()
				.map(this::toApprovalNode)
				.filter(Objects::nonNull)
				.collect(Collectors.toList());
	}

	private VisitorApprovalNodeRespDTO toApprovalNode(List<ApproveList> approveGroup) {
		ApproveList approve = displayApprove(approveGroup);
		if (approve == null) {
			return null;
		}
		VisitorApprovalNodeRespDTO node = new VisitorApprovalNodeRespDTO();
		node.setTitle(approvalNodeName(approve));
		node.setState(nodeState(approveGroup));
		node.setApproverName(trim(approverName(approve)));
		node.setTime(format(approve.getUpdateTime()));
		node.setComment(approve.getRemark());
		return node;
	}

	private ApproveList displayApprove(List<ApproveList> approveGroup) {
		if (approveGroup == null || approveGroup.isEmpty()) {
			return null;
		}
		ApproveList refused = firstByState(approveGroup, ApproveListStateEnum.REFUSE.getCode());
		if (refused != null) {
			return refused;
		}
		ApproveList pending = firstByState(approveGroup, ApproveListStateEnum.PENDING.getCode());
		if (pending != null) {
			return pending;
		}
		ApproveList agreed = firstByState(approveGroup, ApproveListStateEnum.AGREE.getCode());
		if (agreed != null) {
			return agreed;
		}
		return firstByState(approveGroup, ApproveListStateEnum.WAITING.getCode());
	}

	private ApproveList firstByState(List<ApproveList> approveGroup, Integer state) {
		return approveGroup.stream()
				.filter(approve -> Objects.equals(approve.getApproveState(), state))
				.findFirst()
				.orElse(null);
	}

	private String approvalNodeName(ApproveList approve) {
		if (smtApprovalNodeService != null && approve.getNodeId() != null) {
			SmtApprovalNode approvalNode = smtApprovalNodeService.getById(approve.getNodeId());
			if (approvalNode != null && StringUtils.hasText(approvalNode.getName())) {
				return approvalNode.getName();
			}
		}
		return defaultText(approve.getApproveName(), "审批节点");
	}

	private String approverName(ApproveList approve) {
		if (smtStaffService == null || !StringUtils.hasText(approve.getApproveBadge())) {
			return "";
		}
		SmtStaff staff = smtStaffService.getSimpleSttaffByBadge(approve.getApproveBadge());
		return staff == null ? "" : staff.getName();
	}

	private String nodeState(List<ApproveList> approveGroup) {
		if (firstByState(approveGroup, ApproveListStateEnum.REFUSE.getCode()) != null) {
			return "rejected";
		}
		if (firstByState(approveGroup, ApproveListStateEnum.PENDING.getCode()) != null) {
			return "current";
		}
		if (firstByState(approveGroup, ApproveListStateEnum.AGREE.getCode()) != null) {
			return "done";
		}
		return "wait";
	}

	private String currentNode(SmtAdmittanceApply apply) {
		if (StringUtils.hasText(apply.getProcessId())) {
			return oaCurrentNode(apply.getProcessId());
		}
		if (approveListService == null || apply.getId() == null) {
			return null;
		}
		ApproveList pendingApprove = visitorApproves(String.valueOf(apply.getId())).stream()
				.filter(approve -> Objects.equals(approve.getApproveState(), ApproveListStateEnum.PENDING.getCode()))
				.findFirst()
				.orElse(null);
		String node = pendingApprove == null ? null : approvalNodeName(pendingApprove);
		String approver = pendingApprove == null ? null : approverName(pendingApprove);
		return currentNodeText(node, approver);
	}

	private String oaCurrentNode(String processId) {
		FlowVO currentFlow = oaProcessFlow(processId).stream()
				.filter(flow -> Objects.equals(trim(flow.getProcessDesc()), ApplicationEnum.RECORD_STATUS_c.getDesc()))
				.findFirst()
				.orElse(null);
		if (currentFlow == null) {
			return null;
		}
		return currentNodeText(currentFlow.getNodeName(), currentFlow.getCreateUser());
	}

	private String currentNodeText(String nodeName, String approverName) {
		String node = trim(nodeName);
		String approver = trim(approverName);
		if (StringUtils.hasText(node) && StringUtils.hasText(approver)) {
			return node + " " + approver + " 审批中";
		}
		if (StringUtils.hasText(node)) {
			return node + " 审批中";
		}
		if (StringUtils.hasText(approver)) {
			return approver + " 审批中";
		}
		return null;
	}

	private List<ApproveList> visitorApproves(String applyId) {
		if (approveListService == null) {
			return Collections.emptyList();
		}
		List<ApproveList> approveList = approveListService.list(Wrappers.<ApproveList>lambdaQuery()
				.eq(ApproveList::getBusinessId, applyId)
				.eq(ApproveList::getApproveType, ApproveListTypeConstants.VISITOR)
				.orderByAsc(ApproveList::getSort));
		if (approveList == null) {
			return Collections.emptyList();
		}
		return approveList.stream()
				.filter(approve -> Objects.equals(approve.getApproveType(), ApproveListTypeConstants.VISITOR))
				.sorted(Comparator.comparing(ApproveList::getSort, Comparator.nullsLast(Integer::compareTo)))
				.collect(Collectors.toList());
	}

	private String applyStatus(SmtAdmittanceApply apply) {
		if (Objects.equals(apply.getStatus(), VisitorStatusEnum.Status_1.getCode())) {
			return "REJECTED";
		}
		if (Objects.equals(apply.getStatus(), VisitorStatusEnum.CAUSE_4.getCode())
				|| Objects.equals(apply.getStatus(), VisitorStatusEnum.CAUSE_6.getCode())
				|| (apply.getEndTime() != null && LocalDateTime.now().isAfter(apply.getEndTime()))) {
			return "EXPIRED";
		}
		if (Objects.equals(apply.getStatus(), VisitorStatusEnum.Status_2.getCode())) {
			return "PENDING";
		}
		return "PASSED";
	}

	private String dispatchStatus(SmtAdmittanceApply apply) {
		if (Objects.equals(apply.getDeviceStatus(), DeviceDownStatusEnum.SUCCESS.getCode())) {
			return "SUCCESS";
		}
		// ALRAEDY(4)：任务已提交ISC等待确认，过渡态映射到ISSUING
		if (Objects.equals(apply.getDeviceStatus(), DeviceDownStatusEnum.ALRAEDY.getCode())) {
			return "ISSUING";
		}
		if (Objects.equals(apply.getDeviceStatus(), DeviceDownStatusEnum.FAIL.getCode())) {
			return "FAILED";
		}
		return "ISSUING";
	}

	private String causeDesc(SmtAdmittanceApply apply) {
		String desc = null;
		if (Objects.equals(apply.getApplyType(), AdmittanceTypeEnum.CAR.getCode())) {
			desc = AdmittanceCarCauseEnum.desc(apply.getCause());
		} else {
			desc = AdmittanceCauseEnum.desc(apply.getCause());
		}
		return StringUtils.hasText(desc) ? desc : defaultText(apply.getCause(), "");
	}

	private List<SmtAdmittanceFellow> nonMainFellows(Long applyId) {
		List<SmtAdmittanceFellow> fellows = smtAdmittanceFellowService.getByApplyId(applyId);
		if (fellows == null) {
			return Collections.emptyList();
		}
		return fellows.stream()
				.filter(fellow -> !Objects.equals(fellow.getIsMain(), 1))
				.collect(Collectors.toList());
	}

	private List<SmtAdmittanceVehicle> vehicles(Long applyId) {
		List<SmtAdmittanceVehicle> vehicles = smtAdmittanceVehicleService.getByApplyId(applyId);
		return vehicles == null ? Collections.emptyList() : vehicles;
	}

	private List<String> areas(SmtAdmittanceApply apply) {
		List<String> areas = new ArrayList<>();
		addAreas(areas, apply.getPermitArea());
		addAreas(areas, apply.getPermitOldArea());
		return areas;
	}

	private void addAreas(List<String> areas, String raw) {
		if (!StringUtils.hasText(raw)) {
			return;
		}
		for (String item : raw.split("[,，]")) {
			String area = trim(item);
			if (StringUtils.hasText(area)) {
				areas.add(area);
			}
		}
	}

	private String parkName(SmtAdmittanceApply apply) {
		if (apply.getParkId() == null) {
			return "";
		}
		SmtPark park = smtParkService.getById(apply.getParkId());
		return park == null ? "" : defaultText(park.getParkName(), "");
	}

	private String maskPhone(String phone) {
		String clean = trim(phone);
		if (clean.length() < 7) {
			return "";
		}
		return clean.substring(0, 3) + "****" + clean.substring(clean.length() - 4);
	}

	private String format(LocalDateTime time) {
		return time == null ? "" : DATE_TIME.format(time);
	}

	private String format(Date time) {
		return time == null ? "" : DATE_TIME.format(LocalDateTime.ofInstant(time.toInstant(), ZoneId.systemDefault()));
	}

	private String tokenKey(String token) {
		return TOKEN_KEY_PREFIX + token;
	}

	private SmartException forbidden(String message) {
		return new SmartException(403, message);
	}

	private String trim(String value) {
		return value == null ? "" : value.trim();
	}

	private String defaultText(Object value, String fallback) {
		return value == null ? fallback : String.valueOf(value);
	}

	private static class QueryCredential {
		private final String queryToken;
		private final String mobile;

		private QueryCredential(String queryToken, String mobile) {
			this.queryToken = queryToken;
			this.mobile = mobile;
		}
	}
}
