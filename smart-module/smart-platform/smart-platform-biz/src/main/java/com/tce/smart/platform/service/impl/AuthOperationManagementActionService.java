package com.tce.smart.platform.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.tce.smart.platform.core.dto.authgovernance.AuthOperationGovernanceData.ActionResult;
import com.tce.smart.platform.core.dto.authgovernance.AuthOperationGovernanceData.ActionRow;
import com.tce.smart.platform.core.dto.authgovernance.AuthOperationGovernanceData.Actor;
import com.tce.smart.platform.core.dto.authgovernance.AuthOperationGovernanceData.ManualVerificationCommand;
import com.tce.smart.platform.core.dto.authgovernance.AuthOperationGovernanceData.RetryCommand;
import com.tce.smart.platform.core.dto.authgovernance.AuthOperationGovernanceData.ReviewRow;
import com.tce.smart.platform.core.service.impl.AuthOperationGovernanceService;
import com.tce.smart.common.security.component.PermissionService;
import com.tce.smart.common.security.service.SmartUser;
import com.tce.smart.common.security.util.SecurityUtils;
import com.tce.smart.platform.dto.authgovernance.*;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeParseException;
import java.util.*;

/** 管理 Web 编排；身份只从当前 SecurityContext 获取，批量目标逐个调用短事务。 */
@Service
public class AuthOperationManagementActionService {

	private final AuthOperationGovernanceService governance;
	private final PermissionService permissions;
	private final ObjectMapper objectMapper;

	public AuthOperationManagementActionService(AuthOperationGovernanceService governance,
			PermissionService permissions, ObjectMapper objectMapper) {
		this.governance = Objects.requireNonNull(governance, "治理服务不能为空");
		this.permissions = Objects.requireNonNull(permissions, "权限服务不能为空");
		this.objectMapper = Objects.requireNonNull(objectMapper, "JSON服务不能为空");
	}

	public List<AuthOperationActionResultView> retry(AuthOperationRetryRequest request) {
		Actor actor = actor(AuthOperationGovernanceService.RETRY_PERMISSION, true);
		if (request == null || request.getTargets() == null || request.getTargets().isEmpty()
				|| request.getTargets().size() > 100) throw new IllegalArgumentException("重试目标数量必须为1至100");
		List<AuthOperationRetryItem> ordered = new ArrayList<>(request.getTargets());
		ordered.sort(Comparator.comparing(item -> id(item == null ? null : item.getTargetId(), "目标ID")));
		Set<Long> unique = new HashSet<>();
		List<RetryCommand> commands = new ArrayList<>(ordered.size());
		for (AuthOperationRetryItem item : ordered) {
			Long targetId = id(item == null ? null : item.getTargetId(), "目标ID");
			if (!unique.add(targetId)) throw new IllegalArgumentException("同一请求不能包含重复目标");
			RetryCommand command = RetryCommand.builder().targetId(targetId)
					.expectedOperationVersion(id(item.getExpectedOperationVersion(), "期望操作代次"))
					.expectedAttemptId(id(item.getExpectedAttemptId(), "期望尝试ID"))
					.expectedAttemptNo(item.getExpectedAttemptNo()).expectedState(item.getExpectedState())
					.idempotencyKey(itemKey(request.getIdempotencyKey())).reasonText(request.getReasonText()).build();
			commands.add(command);
		}
		List<AuthOperationActionResultView> results = new ArrayList<>(commands.size());
		for (RetryCommand command : commands) {
			results.add(toView(invokeRetry(actor, command)));
		}
		return Collections.unmodifiableList(results);
	}

	public AuthOperationActionResultView manualVerification(Long targetId,
			AuthOperationManualVerificationRequest request) {
		Actor actor = actor(AuthOperationGovernanceService.MANUAL_PERMISSION, true);
		if (request == null || request.getEvidence() == null) throw new IllegalArgumentException("人工观察证据不能为空");
		AuthOperationManualEvidenceRequest evidence = request.getEvidence();
		ManualVerificationCommand command = ManualVerificationCommand.builder().targetId(positive(targetId, "目标ID"))
				.expectedOperationVersion(id(request.getExpectedOperationVersion(), "期望操作代次"))
				.expectedAttemptId(id(request.getExpectedAttemptId(), "期望尝试ID"))
				.expectedState(request.getExpectedState()).idempotencyKey(request.getIdempotencyKey())
				.observedConclusion(request.getObservedConclusion()).reasonText(request.getReasonText())
				.evidenceType(evidence.getType()).evidenceReference(evidence.getReference())
				.evidenceBody(canonicalJson(evidence.getBody())).observedAt(utc(evidence.getObservedAt())).build();
		try {
			return toView(governance.recordManualVerification(actor, command));
		} catch (DuplicateKeyException concurrent) {
			// 前一事务已经回滚结束，第二次代理调用用新事务读取唯一键赢家。
			return toView(governance.recordManualVerification(actor, command));
		}
	}

	public IPage<AuthOperationReviewView> getParkReviews(AuthOperationReviewPageQuery query) {
		Actor actor = actor(AuthOperationGovernanceService.REVIEW_PERMISSION, true);
		AuthOperationReviewPageQuery actual = query == null ? new AuthOperationReviewPageQuery() : query;
		return reviewPage(governance.getParkReviews(actor, actual.getParkId(), current(actual.getCurrent()), size(actual.getSize())));
	}

	public IPage<AuthOperationReviewView> getGlobalReviews(AuthOperationReviewPageQuery query) {
		Actor actor = actor(AuthOperationGovernanceService.GLOBAL_REVIEW_PERMISSION, false);
		AuthOperationReviewPageQuery actual = query == null ? new AuthOperationReviewPageQuery() : query;
		if (actual.getParkId() != null) throw new IllegalArgumentException("全局问题队列不接受园区参数");
		return reviewPage(governance.getGlobalReviews(actor, current(actual.getCurrent()), size(actual.getSize())));
	}

	public IPage<AuthOperationActionView> getActions(Long targetId, AuthOperationActionPageQuery query) {
		Actor actor = historyActor();
		AuthOperationActionPageQuery actual = query == null ? new AuthOperationActionPageQuery() : query;
		return actionPage(governance.getTargetActions(actor, positive(targetId, "目标ID"),
				current(actual.getCurrent()), size(actual.getSize())));
	}

	public AuthOperationActionView getAction(Long targetId, Long actionId) {
		return toView(governance.getTargetAction(historyActor(), positive(targetId, "目标ID"),
				positive(actionId, "动作ID")));
	}

	private ActionResult invokeRetry(Actor actor, RetryCommand command) {
		try {
			return governance.retryKnownUnsent(actor, command);
		} catch (DuplicateKeyException concurrent) {
			return governance.retryKnownUnsent(actor, command);
		}
	}

	private Actor actor(String permission, boolean requirePark) {
		if (!permissions.hasPermission(permission)) throw new AccessDeniedException("无治理权限");
		Authentication authentication = SecurityUtils.getAuthentication();
		SmartUser user = authentication == null ? null : SecurityUtils.getUser(authentication);
		if (user == null || user.getId() == null) throw new AccessDeniedException("无治理用户");
		List<Integer> parks = user.getParkIdList() == null ? Collections.emptyList() : new ArrayList<>(user.getParkIdList());
		if (requirePark && parks.isEmpty()) throw new AccessDeniedException("无可访问园区");
		List<String> granted = new ArrayList<>();
		for (GrantedAuthority authority : authentication.getAuthorities()) {
			if (authority != null && authority.getAuthority() != null) granted.add(authority.getAuthority());
		}
		return Actor.builder().userId(user.getId()).username(user.getUsername())
				.parkIds(Collections.unmodifiableList(parks)).permissions(Collections.unmodifiableList(granted)).build();
	}

	private Actor historyActor() {
		if (permissions.hasPermission(AuthOperationGovernanceService.MANUAL_PERMISSION)) {
			return actor(AuthOperationGovernanceService.MANUAL_PERMISSION, true);
		}
		return actor(AuthOperationGovernanceService.RETRY_PERMISSION, true);
	}

	private IPage<AuthOperationReviewView> reviewPage(IPage<ReviewRow> source) {
		Page<AuthOperationReviewView> target = new Page<>(source.getCurrent(), source.getSize(), source.getTotal());
		List<AuthOperationReviewView> records = new ArrayList<>();
		for (ReviewRow row : source.getRecords()) records.add(AuthOperationReviewView.builder()
				.reviewId(row.getReviewId()).parkId(row.getParkId()).accessType(row.getAccessType())
				.deviceId(row.getDeviceId()).taskKey(row.getTaskKey()).reason(row.getReason())
				.state(row.getState()).createdAt(utc(row.getCreatedAt())).build());
		target.setRecords(records);
		return target;
	}

	private IPage<AuthOperationActionView> actionPage(IPage<ActionRow> source) {
		Page<AuthOperationActionView> target = new Page<>(source.getCurrent(), source.getSize(), source.getTotal());
		List<AuthOperationActionView> records = new ArrayList<>();
		for (ActionRow row : source.getRecords()) records.add(toView(row));
		target.setRecords(records);
		return target;
	}

	private AuthOperationActionResultView toView(ActionResult result) {
		return AuthOperationActionResultView.builder().actionId(id(result.getActionId())).targetId(id(result.getTargetId()))
				.outcome(result.getOutcome()).reasonCode(result.getReasonCode()).beforeState(result.getBeforeState())
				.afterState(result.getAfterState()).replay(result.isReplay()).build();
	}

	private AuthOperationActionView toView(ActionRow row) {
		return AuthOperationActionView.builder().actionId(id(row.getActionId())).targetId(id(row.getTargetId()))
				.actionType(row.getActionType()).actorUserId(row.getActorUserId()).actorUsername(row.getActorUsername())
				.reasonText(row.getReasonText()).expectedOperationVersion(id(row.getExpectedOperationVersion()))
				.expectedState(row.getExpectedState()).expectedAttemptId(id(row.getExpectedAttemptId()))
				.expectedAttemptNo(row.getExpectedAttemptNo())
				.observedConclusion(row.getObservedConclusion()).beforeState(row.getBeforeState()).afterState(row.getAfterState())
				.result(row.getResult()).resultCode(row.getResultCode()).evidenceType(row.getEvidenceType())
				.evidenceReference(row.getEvidenceReference()).evidenceBody(row.getEvidenceBody())
				.evidenceSha256(row.getEvidenceSha256()).observedAt(utc(row.getObservedAt())).createdAt(utc(row.getCreatedAt())).build();
	}

	private String canonicalJson(JsonNode body) {
		if (body == null || body.isNull()) throw new IllegalArgumentException("证据正文不能为空");
		try {
			return objectMapper.writeValueAsString(sort(body));
		} catch (JsonProcessingException failure) {
			throw new IllegalArgumentException("证据正文无法规范化", failure);
		}
	}

	private JsonNode sort(JsonNode value) {
		if (value.isObject()) {
			ObjectNode sorted = objectMapper.createObjectNode();
			List<String> names = new ArrayList<>();
			value.fieldNames().forEachRemaining(names::add);
			Collections.sort(names);
			for (String name : names) sorted.set(name, sort(value.get(name)));
			return sorted;
		}
		if (value.isArray()) {
			ArrayNode sorted = objectMapper.createArrayNode();
			for (JsonNode item : value) sorted.add(sort(item));
			return sorted;
		}
		return value;
	}

	private static String itemKey(String base) {
		if (base == null || base.trim().isEmpty()) throw new IllegalArgumentException("幂等键不能为空");
		String key = base.trim();
		if (key.length() > 128) throw new IllegalArgumentException("幂等键超过128字符");
		return key;
	}

	private static LocalDateTime utc(String value) {
		try {
			return LocalDateTime.ofInstant(Instant.parse(value), ZoneOffset.UTC);
		} catch (NullPointerException | DateTimeParseException invalid) {
			throw new IllegalArgumentException("观察时间必须为UTC且带Z", invalid);
		}
	}

	private static String utc(LocalDateTime value) {
		return value == null ? null : value.toInstant(ZoneOffset.UTC).toString();
	}

	private static int current(Integer value) {
		return value == null ? 1 : value;
	}

	private static int size(Integer value) {
		return value == null ? 20 : value;
	}

	private static Long id(String value, String name) {
		try {
			return positive(Long.valueOf(value), name);
		} catch (NumberFormatException | NullPointerException invalid) {
			throw new IllegalArgumentException(name + "必须为正整数字符串", invalid);
		}
	}

	private static Long positive(Long value, String name) {
		if (value == null || value <= 0) throw new IllegalArgumentException(name + "必须为正数");
		return value;
	}

	private static String id(Long value) {
		return value == null ? null : String.valueOf(value);
	}
}
