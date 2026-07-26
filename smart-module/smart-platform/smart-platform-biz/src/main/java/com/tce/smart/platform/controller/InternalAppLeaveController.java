package com.tce.smart.platform.controller;

import cn.hutool.core.util.StrUtil;
import com.tce.smart.common.core.constant.SecurityConstants;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.core.wrapper.BaseController;
import com.tce.smart.common.security.annotation.Inner;
import com.tce.smart.common.security.annotation.OpenApi;
import com.tce.smart.common.security.openapi.OpenApiAuthenticationAdapter;
import com.tce.smart.common.security.util.SecurityUtils;
import com.tce.smart.platform.api.dto.LeaveHandoverDepJjrDTO;
import com.tce.smart.platform.api.dto.ProcessRecordFlowDTO;
import com.tce.smart.platform.api.dto.req.LeaveApplicationReqDTO;
import com.tce.smart.platform.api.dto.req.LeaveHandoverReqDTO;
import com.tce.smart.platform.core.dto.LeaveApplicationDTO;
import com.tce.smart.platform.core.dto.LeaveHandoverDTO;
import com.tce.smart.platform.core.entity.SmtLeaveApplication;
import com.tce.smart.platform.core.entity.SmtLeaveHandover;
import com.tce.smart.platform.core.entity.SmtProcessRecord;
import com.tce.smart.platform.core.model.LeaveHandoverDepJjr;
import com.tce.smart.platform.core.model.ProcessRecordFlow;
import com.tce.smart.platform.core.service.SmtLeaveApplicationService;
import com.tce.smart.platform.core.vo.LeaveApplicationVO;
import com.tce.smart.platform.service.ILeaveApplicationService;
import com.tce.smart.platform.service.SmtLeaveHandoverService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.HashSet;
import java.util.Set;

/**
 * Smart App 离职流程内部入口。
 *
 * processId 和 employeeId 都是客户端可篡改的定位键，Platform 在每个读取或写入操作中
 * 重新按离职申请本人或交接人归属授权；服务令牌只证明调用服务，不替代该记录级校验。
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/internal/app-leave")
public class InternalAppLeaveController extends BaseController {
	private static final String PURPOSE = "app-leave-self";

	private final SmtLeaveApplicationService leaveApplicationService;
	private final ILeaveApplicationService leaveService;
	private final SmtLeaveHandoverService handoverService;
	private final OpenApiAuthenticationAdapter authenticationAdapter;

	/** 缺少精确受管 client_id 时必须拒绝，禁止任意 server scope 服务代入。 */
	@Value("${security.inner.leave-app.app-client-id:}")
	private String appServiceClientId;

	@Inner
	@OpenApi("server")
	@PostMapping("/application")
	public Result save(@RequestBody LeaveApplicationReqDTO request,
			@RequestHeader("X-Smart-Actor-Badge") String actorBadge,
			@RequestHeader(value = "X-Smart-Actor-Park-Ids", required = false) String actorParkIds,
			@RequestHeader(value = SecurityConstants.FROM, required = false) String from,
			@RequestHeader(value = "X-Smart-Internal-Purpose", required = false) String purpose) {
		assertAppCaller(from, purpose);
		if (request == null || !actorBadge.equals(request.getBadge()) || !actorBadge.equals(request.getApplyBadge())
				|| parseActorParks(actorParkIds).isEmpty()) {
			throw new AccessDeniedException("离职申请不存在或无权操作");
		}
		LeaveApplicationDTO command = new LeaveApplicationDTO();
		BeanUtils.copyProperties(request, command);
		return leaveService.saveLeaveApplication(command);
	}

	@Inner
	@OpenApi("server")
	@GetMapping("/application/{processId}")
	public Result application(@PathVariable("processId") String processId,
			@RequestHeader("X-Smart-Actor-Badge") String actorBadge,
			@RequestHeader(value = "X-Smart-Actor-Park-Ids", required = false) String actorParkIds,
			@RequestHeader(value = SecurityConstants.FROM, required = false) String from,
			@RequestHeader(value = "X-Smart-Internal-Purpose", required = false) String purpose) {
		assertAppCaller(from, purpose);
		SmtLeaveApplication application = assertApplicant(processId, actorBadge, actorParkIds);
		return success(application, LeaveApplicationVO.class);
	}

	@Inner
	@OpenApi("server")
	@GetMapping("/record/{processId}")
	public Result record(@PathVariable("processId") String processId,
			@RequestHeader("X-Smart-Actor-Badge") String actorBadge,
			@RequestHeader(value = "X-Smart-Actor-Park-Ids", required = false) String actorParkIds,
			@RequestHeader(value = SecurityConstants.FROM, required = false) String from,
			@RequestHeader(value = "X-Smart-Internal-Purpose", required = false) String purpose) {
		assertAppCaller(from, purpose);
		assertApplicant(processId, actorBadge, actorParkIds);
		List<SmtProcessRecord> records = leaveApplicationService.getLeaveApplication(processId);
		return success(records, ProcessRecordFlow.class);
	}

	@Inner
	@OpenApi("server")
	@GetMapping("/handover/{processId}")
	public Result handover(@PathVariable("processId") String processId,
			@RequestHeader("X-Smart-Actor-Badge") String actorBadge,
			@RequestHeader(value = "X-Smart-Actor-Park-Ids", required = false) String actorParkIds,
			@RequestHeader(value = SecurityConstants.FROM, required = false) String from,
			@RequestHeader(value = "X-Smart-Internal-Purpose", required = false) String purpose) {
		assertAppCaller(from, purpose);
		assertApplicant(processId, actorBadge, actorParkIds);
		List<SmtLeaveHandover> records = handoverService.getLeaveHandover(processId);
		return success(records, LeaveHandoverDepJjr.class);
	}

	@Inner
	@OpenApi("server")
	@GetMapping("/handover/assignee/{processId}")
	public Result assignee(@PathVariable("processId") String processId,
			@RequestHeader("X-Smart-Actor-Badge") String actorBadge,
			@RequestHeader(value = "X-Smart-Actor-Park-Ids", required = false) String actorParkIds,
			@RequestHeader(value = SecurityConstants.FROM, required = false) String from,
			@RequestHeader(value = "X-Smart-Internal-Purpose", required = false) String purpose) {
		assertAppCaller(from, purpose);
		SmtLeaveApplication application = application(processId, actorParkIds);
		assertAssignee(processId, actorBadge, actorParkIds);
		return success(application, com.tce.smart.platform.core.vo.LeaveHandoverApplicationVO.class);
	}

	@Inner
	@OpenApi("server")
	@GetMapping("/handover/start/{processId}")
	public Result start(@PathVariable("processId") String processId,
			@RequestHeader("X-Smart-Actor-Badge") String actorBadge,
			@RequestHeader(value = "X-Smart-Actor-Park-Ids", required = false) String actorParkIds,
			@RequestHeader(value = SecurityConstants.FROM, required = false) String from,
			@RequestHeader(value = "X-Smart-Internal-Purpose", required = false) String purpose) {
		assertAppCaller(from, purpose);
		assertApplicant(processId, actorBadge, actorParkIds);
		return success(handoverService.startLeaveHandover(processId));
	}

	@Inner
	@OpenApi("server")
	@PostMapping("/handover/commit")
	public Result commit(@RequestBody LeaveHandoverReqDTO request,
			@RequestHeader("X-Smart-Actor-Badge") String actorBadge,
			@RequestHeader(value = "X-Smart-Actor-Park-Ids", required = false) String actorParkIds,
			@RequestHeader(value = SecurityConstants.FROM, required = false) String from,
			@RequestHeader(value = "X-Smart-Internal-Purpose", required = false) String purpose) {
		assertAppCaller(from, purpose);
		if (request == null) {
			throw new AccessDeniedException("离职交接不存在或无权操作");
		}
		assertAssignee(request.getProcessId(), actorBadge, actorParkIds);
		LeaveHandoverDTO command = new LeaveHandoverDTO();
		BeanUtils.copyProperties(request, command);
		// 不接受客户端写入的 jjr；交接完成只能以认证 actor 的身份落库。
		command.setJjr(actorBadge);
		return success(handoverService.endLeaveHandover(command));
	}

	@Inner
	@OpenApi("server")
	@GetMapping("/handover/close/{processId}")
	public Result close(@PathVariable("processId") String processId,
			@RequestHeader("X-Smart-Actor-Badge") String actorBadge,
			@RequestHeader(value = "X-Smart-Actor-Park-Ids", required = false) String actorParkIds,
			@RequestHeader(value = SecurityConstants.FROM, required = false) String from,
			@RequestHeader(value = "X-Smart-Internal-Purpose", required = false) String purpose) {
		assertAppCaller(from, purpose);
		assertApplicant(processId, actorBadge, actorParkIds);
		return success(handoverService.closeLeaveHandover(processId));
	}

	private SmtLeaveApplication assertApplicant(String processId, String actorBadge, String actorParkIds) {
		SmtLeaveApplication application = application(processId, actorParkIds);
		if (!actorBadge.equals(application.getBadge()) && !actorBadge.equals(application.getApplyBadge())) {
			throw new AccessDeniedException("离职申请不存在或无权访问");
		}
		return application;
	}

	private void assertAssignee(String processId, String actorBadge, String actorParkIds) {
		application(processId, actorParkIds);
		List<SmtLeaveHandover> handovers = handoverService.getLeaveHandover(processId, actorBadge);
		if (handovers == null || handovers.isEmpty()) {
			throw new AccessDeniedException("离职交接不存在或无权操作");
		}
	}

	private SmtLeaveApplication application(String processId, String actorParkIds) {
		if (StrUtil.isBlank(processId)) {
			throw new AccessDeniedException("离职申请不存在或无权访问");
		}
		try {
			SmtLeaveApplication application = leaveApplicationService.getLeaveApplicationRecord(processId);
			if (application == null || application.getParkId() == null
					|| !parseActorParks(actorParkIds).contains(application.getParkId())) {
				throw new AccessDeniedException("离职申请不存在或无权访问");
			}
			return application;
		} catch (RuntimeException ignored) {
			throw new AccessDeniedException("离职申请不存在或无权访问");
		}
	}

	/** 园区头由 App 从已认证会话派生；缺失或格式错误一律按无园区处理。 */
	private Set<Integer> parseActorParks(String actorParkIds) {
		Set<Integer> parks = new HashSet<>();
		if (StrUtil.isBlank(actorParkIds)) {
			return parks;
		}
		for (String value : actorParkIds.split(",")) {
			try {
				parks.add(Integer.valueOf(value.trim()));
			} catch (RuntimeException ignored) {
				return new HashSet<>();
			}
		}
		return parks;
	}

	private void assertAppCaller(String from, String purpose) {
		Authentication authentication = SecurityUtils.getAuthentication();
		if (!SecurityConstants.FROM_IN.equals(from) || !PURPOSE.equals(purpose)
				|| StrUtil.isBlank(appServiceClientId) || authentication == null
				|| !authenticationAdapter.isClientOnly(authentication)
				|| !appServiceClientId.equals(authenticationAdapter.clientId(authentication))) {
			throw new AccessDeniedException("App 离职内部调用未获授权");
		}
	}
}
