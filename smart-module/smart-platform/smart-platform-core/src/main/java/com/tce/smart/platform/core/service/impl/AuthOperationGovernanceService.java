package com.tce.smart.platform.core.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.tce.smart.platform.core.config.AuthOperationGovernanceProperties;
import com.tce.smart.platform.core.dto.authgovernance.AuthOperationGovernanceConflictException;
import com.tce.smart.platform.core.dto.authgovernance.AuthOperationGovernanceData.ActionResult;
import com.tce.smart.platform.core.dto.authgovernance.AuthOperationGovernanceData.ActionRow;
import com.tce.smart.platform.core.dto.authgovernance.AuthOperationGovernanceData.Actor;
import com.tce.smart.platform.core.dto.authgovernance.AuthOperationGovernanceData.ManualVerificationCommand;
import com.tce.smart.platform.core.dto.authgovernance.AuthOperationGovernanceData.RetryCommand;
import com.tce.smart.platform.core.dto.authgovernance.AuthOperationGovernanceData.ReviewRow;
import com.tce.smart.platform.core.dto.authgovernance.AuthOperationGovernanceData.TargetSnapshot;
import com.tce.smart.platform.core.dto.authversion.AuthVersion.ResourceDecision;
import com.tce.smart.platform.core.entity.SmtAuthGovernanceAction;
import com.tce.smart.platform.core.entity.SmtAuthOperationAttempt;
import com.tce.smart.platform.core.mapper.AuthOperationGovernanceMapper;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Base64;
import java.util.Iterator;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;

/**
 * 权限治理首个写门禁。每次只处理一个目标，事务内不调用设备或外部系统。
 */
@Service
public class AuthOperationGovernanceService {

	public static final String RETRY_PERMISSION = "platform_auth_operation_retry";
	public static final String MANUAL_PERMISSION = "platform_auth_operation_manual_verify";
	public static final String REVIEW_PERMISSION = "platform_auth_operation_review_view";
	public static final String GLOBAL_REVIEW_PERMISSION = "platform_auth_operation_global_review_view";
	private static final int MAX_PAGE_SIZE = 100;
	private static final int MAX_EVIDENCE_BYTES = 16 * 1024;
	private static final Set<String> EVIDENCE_FIELDS = new TreeSet<>(Arrays.asList(
			"deviceId", "observation", "recordId"));
	private static final Set<String> EVIDENCE_TYPES = new TreeSet<>(Arrays.asList(
			"ACCESS_LOG_REFERENCE", "OPERATOR_OBSERVATION", "TICKET_REFERENCE"));
	private static final Pattern EVIDENCE_CREDENTIAL_MARKER = Pattern.compile(
			"(?i)(?:^|[^A-Za-z0-9])(?:access[_-]?token|refresh[_-]?token|id[_-]?token|password|passwd|pwd|token|authorization|api[_-]?key|credential|secret)[\\\"']?\\s*[:=]");
	private static final Pattern EVIDENCE_BEARER_MARKER = Pattern.compile(
			"(?i)(?:^|[^A-Za-z0-9])bearer\\s+[A-Za-z0-9._~+/-]{8,}");
	private static final Pattern IMAGE_DATA_URI = Pattern.compile(
			"(?i)data\\s*:\\s*image/[A-Za-z0-9.+-]+(?:\\s*;[^,]*)?\\s*;\\s*base64\\s*,");
	private static final ObjectMapper EVIDENCE_JSON = new ObjectMapper();
	private static final String HIDDEN_TARGET = "治理目标不存在或无访问权限";

	private final AuthOperationGovernanceMapper mapper;
	private final AuthOperationVersionService versions;
	private final AuthOperationGovernanceProperties properties;

	public AuthOperationGovernanceService(AuthOperationGovernanceMapper mapper,
			AuthOperationVersionService versions, AuthOperationGovernanceProperties properties) {
		this.mapper = Objects.requireNonNull(mapper, "治理Mapper不能为空");
		this.versions = Objects.requireNonNull(versions, "版本服务不能为空");
		this.properties = Objects.requireNonNull(properties, "治理配置不能为空");
	}

	@Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
	public ActionResult retryKnownUnsent(Actor actor, RetryCommand command) {
		enabled();
		validateActor(actor, RETRY_PERMISSION);
		command = normalizeRetry(command);
		TargetSnapshot visible = visibleTarget(actor, command.getTargetId());
		String subjectKey = subject(command.getTargetId());
		String requestFingerprint = fingerprint(command);
		ActionResult replay = replay(mapper.selectActionByKey(actor.getUserId(), command.getIdempotencyKey(), subjectKey),
				requestFingerprint, command.getTargetId());
		if (replay != null) {
			return replay;
		}
		LocalDateTime now = requiredNow();
		if (mapper.hasCurrentSourceBinding(visible.getTargetId(), visible.getResourceCoordId(),
				visible.getOperationVersion()) <= 0) {
			return rejected(actor, visible, command, requestFingerprint, now, "SOURCE_BINDING_MISSING");
		}

		// currentDesired 内部按主体→资源取锁；随后才允许锁目标和当前尝试。
		ResourceDecision current = versions.currentDesired(visible.getResourceCoordId());
		TargetSnapshot target = mapper.lockTarget(command.getTargetId());
		SmtAuthOperationAttempt attempt = mapper.lockCurrentAttempt(command.getTargetId(), command.getExpectedAttemptId());
		if (target == null || !actor.hasPark(target.getParkId())) {
			throw new AccessDeniedException(HIDDEN_TARGET);
		}
		replay = replay(mapper.selectActionByKey(actor.getUserId(), command.getIdempotencyKey(), subjectKey),
				requestFingerprint, command.getTargetId());
		if (replay != null) {
			return replay;
		}
		if (mapper.hasCurrentSourceBinding(target.getTargetId(), visible.getResourceCoordId(),
				target.getOperationVersion()) <= 0) {
			return rejected(actor, target, command, requestFingerprint, now, "SOURCE_BINDING_CHANGED");
		}
		String rejection = retryRejection(target, attempt, current, visible.getResourceCoordId(), command, now);
		if (rejection == null && mapper.countAttemptTrace(command.getExpectedAttemptId()) != 0) {
			rejection = "SEND_TRACE_PRESENT";
		}
		if (rejection != null) {
			return rejected(actor, target, command, requestFingerprint, now, rejection);
		}

		SmtAuthGovernanceAction action = baseAction(actor, target, command.getIdempotencyKey(), "RETRY",
				command.getReasonText(), command.getExpectedOperationVersion(), command.getExpectedState(),
				command.getExpectedAttemptId(), requestFingerprint, "QUEUED", "REQUEUED",
				"KNOWN_UNSENT_REQUEUED", now);
		action.setExpectedAttemptNo(command.getExpectedAttemptNo());
		insert(action);
		if (mapper.expireKnownUnsentAttempt(attempt.getId(), target.getTargetId(), target.getLeaseToken(), now) != 1) {
			throw new IllegalStateException("治理重试作废旧尝试失败");
		}
		if (current.getBlockingAttemptId() != null && mapper.releaseKnownUnsentResourceAttempt(
				current.getResourceId(), current.getGeneration(), target.getTargetId(), attempt.getId(), now) != 1) {
			throw new IllegalStateException("治理重试释放资源旧尝试失败");
		}
		if (mapper.requeueKnownUnsentTarget(target.getTargetId(), target.getLeaseToken(),
				target.getOperationVersion(), now) != 1) {
			throw new IllegalStateException("治理重试目标入队失败");
		}
		return result(action, false);
	}

	@Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
	public ActionResult recordManualVerification(Actor actor, ManualVerificationCommand command) {
		enabled();
		validateActor(actor, MANUAL_PERMISSION);
		TargetSnapshot visible = visibleTarget(actor, positive(command == null ? null : command.getTargetId(), "目标ID"));
		LocalDateTime now = requiredNow();
		command = normalizeManual(command, now);
		String requestFingerprint = fingerprint(command);
		String subjectKey = subject(command.getTargetId());
		ActionResult replay = replay(mapper.selectActionByKey(actor.getUserId(), command.getIdempotencyKey(), subjectKey),
				requestFingerprint, command.getTargetId());
		if (replay != null) {
			return replay;
		}
		if (mapper.hasCurrentSourceBinding(visible.getTargetId(), visible.getResourceCoordId(),
				visible.getOperationVersion()) <= 0) {
			return rejectedManual(actor, visible, command, requestFingerprint, now, "SOURCE_BINDING_MISSING");
		}

		ResourceDecision current = versions.currentDesired(visible.getResourceCoordId());
		TargetSnapshot target = mapper.lockTarget(command.getTargetId());
		SmtAuthOperationAttempt attempt = mapper.lockCurrentAttempt(command.getTargetId(), command.getExpectedAttemptId());
		if (target == null || !actor.hasPark(target.getParkId())) {
			throw new AccessDeniedException(HIDDEN_TARGET);
		}
		replay = replay(mapper.selectActionByKey(actor.getUserId(), command.getIdempotencyKey(), subjectKey),
				requestFingerprint, command.getTargetId());
		if (replay != null) {
			return replay;
		}
		if (mapper.hasCurrentSourceBinding(target.getTargetId(), visible.getResourceCoordId(),
				target.getOperationVersion()) <= 0) {
			return rejectedManual(actor, target, command, requestFingerprint, now, "SOURCE_BINDING_CHANGED");
		}
		String rejection = commonVersionRejection(target, attempt, current, visible.getResourceCoordId(), command.getExpectedOperationVersion(),
				command.getExpectedAttemptId(), command.getExpectedState());
		if (rejection != null) {
			return rejectedManual(actor, target, command, requestFingerprint, now, rejection);
		}

		SmtAuthGovernanceAction action = baseAction(actor, target, command.getIdempotencyKey(),
				"MANUAL_VERIFICATION", command.getReasonText(), command.getExpectedOperationVersion(),
				command.getExpectedState(), command.getExpectedAttemptId(), requestFingerprint, target.getState(),
				"RECORDED_PENDING_VERIFICATION", "MANUAL_EVIDENCE_NOT_TRUSTED", now);
		action.setObservedConclusion(command.getObservedConclusion());
		action.setEvidenceType(command.getEvidenceType());
		action.setEvidenceReference(command.getEvidenceReference());
		action.setEvidenceBody(command.getEvidenceBody());
		action.setEvidenceSha256(sha256(command.getEvidenceBody()));
		action.setObservedAt(command.getObservedAt());
		insert(action);
		// 本增量只保存观察，不写 target/attempt/source/result-event/物理记录。
		return result(action, false);
	}

	@Transactional(readOnly = true)
	public IPage<ReviewRow> getParkReviews(Actor actor, Integer parkId, int current, int size) {
		validateActor(actor, REVIEW_PERMISSION);
		if (parkId == null || parkId <= 0 || !actor.hasPark(parkId)) {
			throw new AccessDeniedException("无可访问园区");
		}
		return mapper.selectParkReviews(page(current, size), parkId);
	}

	@Transactional(readOnly = true)
	public IPage<ReviewRow> getGlobalReviews(Actor actor, int current, int size) {
		validateActor(actor, GLOBAL_REVIEW_PERMISSION);
		return mapper.selectGlobalReviews(page(current, size));
	}

	@Transactional(readOnly = true)
	public IPage<ActionRow> getTargetActions(Actor actor, Long targetId, int current, int size) {
		validateHistoryActor(actor);
		TargetSnapshot target = visibleTarget(actor, positive(targetId, "目标ID"));
		return mapper.selectTargetActions(page(current, size), targetId, target.getParkId());
	}

	@Transactional(readOnly = true)
	public ActionRow getTargetAction(Actor actor, Long targetId, Long actionId) {
		validateHistoryActor(actor);
		TargetSnapshot target = visibleTarget(actor, positive(targetId, "目标ID"));
		ActionRow row = mapper.selectTargetAction(positive(actionId, "动作ID"), targetId, target.getParkId());
		if (row == null) {
			throw new AccessDeniedException("治理动作不存在或无访问权限");
		}
		return row;
	}

	private String retryRejection(TargetSnapshot target, SmtAuthOperationAttempt attempt,
			ResourceDecision current, String resourceCoordId, RetryCommand command, LocalDateTime now) {
		String common = commonVersionRejection(target, attempt, current, resourceCoordId, command.getExpectedOperationVersion(),
				command.getExpectedAttemptId(), command.getExpectedState());
		if (common != null) return common;
		if (!"EXECUTING".equals(target.getState())) return "TARGET_NOT_EXECUTING";
		if (attempt == null || !"CLAIMED".equals(attempt.getStatus())) return "ATTEMPT_NOT_CLAIMED";
		if (!Objects.equals(command.getExpectedAttemptNo(), attempt.getAttemptNo())) return "ATTEMPT_CHANGED";
		if (attempt.getAttemptNo() >= properties.checkedMaxRetryAttempts()) return "MAX_ATTEMPTS_REACHED";
		if (blank(target.getLeaseToken()) || !Objects.equals(target.getLeaseToken(), attempt.getLeaseToken())) return "LEASE_OWNER_CHANGED";
		if (target.getLeaseUntil() == null || target.getLeaseUntil().isAfter(now)
				|| attempt.getLeaseUntil() == null || attempt.getLeaseUntil().isAfter(now)) return "LEASE_NOT_EXPIRED";
		if (target.getDispatchedAt() != null || attempt.getTaskId() != null || attempt.getExternalBatchId() != null
				|| attempt.getExternalCommandId() != null || attempt.getDispatchedAt() != null
				|| attempt.getResultEventId() != null) return "SEND_TRACE_PRESENT";
		return null;
	}

	private String commonVersionRejection(TargetSnapshot target, SmtAuthOperationAttempt attempt,
			ResourceDecision current, String resourceCoordId, Long expectedVersion, Long expectedAttempt, String expectedState) {
		if (target == null || !Objects.equals(target.getOperationVersion(), expectedVersion)
				|| !Objects.equals(target.getState(), expectedState)) return "TARGET_VERSION_CHANGED";
		if (attempt == null || !Objects.equals(attempt.getId(), expectedAttempt)) return "ATTEMPT_CHANGED";
		if (current == null || !Objects.equals(current.getResourceId(), resourceCoordId)
				|| current.getGeneration() != target.getOperationVersion()) return "RESOURCE_VERSION_CHANGED";
		if (!Objects.equals(current.getBlockingTargetId(), target.getTargetId())
				|| current.getBlockingAttemptId() != null
				&& !Objects.equals(current.getBlockingAttemptId(), attempt.getId())) return "RESOURCE_OWNER_CHANGED";
		return null;
	}

	private ActionResult rejected(Actor actor, TargetSnapshot target, RetryCommand command, String fingerprint,
			LocalDateTime now, String reasonCode) {
		SmtAuthGovernanceAction action = baseAction(actor, target, command.getIdempotencyKey(), "RETRY",
				command.getReasonText(), command.getExpectedOperationVersion(), command.getExpectedState(),
				command.getExpectedAttemptId(), fingerprint, target.getState(), "REJECTED", reasonCode, now);
		action.setExpectedAttemptNo(command.getExpectedAttemptNo());
		insert(action);
		return result(action, false);
	}

	private ActionResult rejectedManual(Actor actor, TargetSnapshot target, ManualVerificationCommand command,
			String fingerprint, LocalDateTime now, String reasonCode) {
		SmtAuthGovernanceAction action = baseAction(actor, target, command.getIdempotencyKey(),
				"MANUAL_VERIFICATION", command.getReasonText(), command.getExpectedOperationVersion(),
				command.getExpectedState(), command.getExpectedAttemptId(), fingerprint, target.getState(),
				"REJECTED", reasonCode, now);
		action.setObservedConclusion(command.getObservedConclusion());
		action.setEvidenceType(command.getEvidenceType());
		action.setEvidenceReference(command.getEvidenceReference());
		action.setEvidenceBody(command.getEvidenceBody());
		action.setEvidenceSha256(sha256(command.getEvidenceBody()));
		action.setObservedAt(command.getObservedAt());
		insert(action);
		return result(action, false);
	}

	private SmtAuthGovernanceAction baseAction(Actor actor, TargetSnapshot target, String key, String type,
			String reason, Long version, String expectedState, Long attemptId, String fingerprint,
			String afterState, String outcome, String resultCode, LocalDateTime now) {
		SmtAuthGovernanceAction action = new SmtAuthGovernanceAction();
		action.setId(IdWorker.getId());
		action.setIdempotencyKey(key);
		action.setSubjectKey(subject(target.getTargetId()));
		action.setTargetId(target.getTargetId());
		action.setActionType(type);
		action.setActorUserId(actor.getUserId());
		action.setActorUsername(actor.getUsername());
		action.setReasonText(reason);
		action.setExpectedOperationVersion(version);
		action.setExpectedState(expectedState);
		action.setExpectedAttemptId(attemptId);
		action.setBeforeState(target.getState());
		action.setAfterState(afterState);
		action.setRequestFingerprint(fingerprint);
		action.setResult(outcome);
		action.setResultCode(resultCode);
		action.setCreateTime(now);
		return action;
	}

	private void insert(SmtAuthGovernanceAction action) {
		if (mapper.insertAction(action) != 1) {
			throw new IllegalStateException("治理审计记录保存失败");
		}
	}

	private ActionResult replay(SmtAuthGovernanceAction action, String fingerprint, Long targetId) {
		if (action == null) return null;
		if (!Objects.equals(action.getRequestFingerprint(), fingerprint)) {
			throw new AuthOperationGovernanceConflictException("同一幂等键的规范请求不一致");
		}
		String outcome = "REQUEUED".equals(action.getResult()) ? "ALREADY_APPLIED" : action.getResult();
		return ActionResult.builder().actionId(action.getId()).targetId(targetId).outcome(outcome)
				.reasonCode(action.getResultCode()).beforeState(action.getBeforeState())
				.afterState(action.getAfterState()).replay(true).build();
	}

	private ActionResult result(SmtAuthGovernanceAction action, boolean replay) {
		return ActionResult.builder().actionId(action.getId()).targetId(action.getTargetId())
				.outcome(action.getResult()).reasonCode(action.getResultCode())
				.beforeState(action.getBeforeState()).afterState(action.getAfterState()).replay(replay).build();
	}

	private TargetSnapshot visibleTarget(Actor actor, Long targetId) {
		TargetSnapshot target = mapper.selectTargetScope(targetId);
		if (target == null || !actor.hasPark(target.getParkId())) {
			throw new AccessDeniedException(HIDDEN_TARGET);
		}
		return target;
	}

	private RetryCommand normalizeRetry(RetryCommand command) {
		if (command == null) throw new IllegalArgumentException("重试命令不能为空");
		positive(command.getTargetId(), "目标ID");
		positive(command.getExpectedOperationVersion(), "期望操作代次");
		positive(command.getExpectedAttemptId(), "期望尝试ID");
		if (command.getExpectedAttemptNo() == null || command.getExpectedAttemptNo() <= 0) {
			throw new IllegalArgumentException("期望尝试号必须为正数");
		}
		String expectedState = text(command.getExpectedState(), 32, "期望状态");
		if (!"EXECUTING".equals(expectedState)) {
			throw new IllegalArgumentException("首个治理重试只接受EXECUTING目标");
		}
		return RetryCommand.builder().targetId(command.getTargetId())
				.expectedOperationVersion(command.getExpectedOperationVersion())
				.expectedAttemptId(command.getExpectedAttemptId()).expectedAttemptNo(command.getExpectedAttemptNo())
				.expectedState(expectedState).idempotencyKey(key(command.getIdempotencyKey()))
				.reasonText(reason(command.getReasonText())).build();
	}

	private ManualVerificationCommand normalizeManual(ManualVerificationCommand command, LocalDateTime now) {
		if (command == null) throw new IllegalArgumentException("人工核验命令不能为空");
		positive(command.getTargetId(), "目标ID");
		positive(command.getExpectedOperationVersion(), "期望操作代次");
		positive(command.getExpectedAttemptId(), "期望尝试ID");
		String expectedState = text(command.getExpectedState(), 32, "期望状态");
		String idempotencyKey = key(command.getIdempotencyKey());
		String reasonText = reason(command.getReasonText());
		String conclusion = text(command.getObservedConclusion(), 32, "人工观察结论");
		if (!Arrays.asList("PERMISSION_ABSENT", "PERMISSION_PRESENT", "INCONCLUSIVE")
				.contains(conclusion)) throw new IllegalArgumentException("人工观察结论无效");
		String evidenceType = text(command.getEvidenceType(), 64, "证据类型");
		if (!EVIDENCE_TYPES.contains(evidenceType)) throw new IllegalArgumentException("证据类型不在允许范围");
		String evidenceReference = restrictedReference(command.getEvidenceReference(), "证据引用");
		if (command.getObservedAt() == null || command.getObservedAt().isAfter(now)) {
			throw new IllegalArgumentException("观察时间不能为空或晚于数据库时间");
		}
		if (command.getObservedAt().isBefore(now.minusDays(properties.checkedMaxObservationAgeDays()))) {
			throw new IllegalArgumentException("观察时间超过有效范围");
		}
		String evidenceBody = canonicalEvidence(command.getEvidenceBody());
		return ManualVerificationCommand.builder().targetId(command.getTargetId())
				.expectedOperationVersion(command.getExpectedOperationVersion())
				.expectedAttemptId(command.getExpectedAttemptId()).expectedState(expectedState)
				.idempotencyKey(idempotencyKey).observedConclusion(conclusion).reasonText(reasonText)
				.evidenceType(evidenceType).evidenceReference(evidenceReference)
				.evidenceBody(evidenceBody).observedAt(command.getObservedAt()).build();
	}

	private static String restrictedReference(String value, String name) {
		String result = text(value, 256, name);
		if (!result.matches("[A-Za-z0-9][A-Za-z0-9._-]{0,255}")) {
			throw new IllegalArgumentException(name + "只能是受限工单或记录标识");
		}
		return result;
	}

	private static String canonicalEvidence(String value) {
		String raw = text(value, MAX_EVIDENCE_BYTES, "证据正文");
		if (raw.getBytes(StandardCharsets.UTF_8).length > MAX_EVIDENCE_BYTES) {
			throw new IllegalArgumentException("证据正文不得超过16KiB");
		}
		try {
			JsonNode parsed = EVIDENCE_JSON.readTree(raw);
			if (parsed == null || !parsed.isObject()) throw new IllegalArgumentException("证据正文必须是JSON对象");
			ObjectNode canonical = EVIDENCE_JSON.createObjectNode();
			Iterator<String> names = parsed.fieldNames();
			Set<String> actual = new TreeSet<>();
			while (names.hasNext()) actual.add(names.next());
			if (!EVIDENCE_FIELDS.containsAll(actual) || !actual.contains("observation")) {
				throw new IllegalArgumentException("证据正文仅允许observation、recordId、deviceId，且observation必填");
			}
			for (String name : actual) {
				JsonNode field = parsed.get(name);
				if (field == null || !field.isTextual()) throw new IllegalArgumentException("证据字段必须是字符串");
				String normalized = text(field.textValue(), "observation".equals(name) ? 2048 : 256, "证据字段" + name);
				if (!"observation".equals(name)) normalized = restrictedReference(normalized, "证据字段" + name);
				validateEvidenceField(name, normalized);
				canonical.put(name, normalized);
			}
			String result = EVIDENCE_JSON.writeValueAsString(canonical);
			if (result.getBytes(StandardCharsets.UTF_8).length > MAX_EVIDENCE_BYTES) {
				throw new IllegalArgumentException("证据正文不得超过16KiB");
			}
			return result;
		} catch (IOException invalid) {
			throw new IllegalArgumentException("证据正文必须是有效JSON", invalid);
		}
	}

	private static void validateEvidenceField(String name, String value) {
		if (EVIDENCE_CREDENTIAL_MARKER.matcher(value).find()
				|| EVIDENCE_BEARER_MARKER.matcher(value).find()) {
			throw new IllegalArgumentException("证据字段" + name + "不得包含明显凭据标记");
		}
		if (looksLikeImageBinary(value)) {
			throw new IllegalArgumentException("证据正文不得包含图片二进制载荷");
		}
	}

	private static boolean looksLikeImageBinary(String value) {
		if (value == null) return false;
		if (IMAGE_DATA_URI.matcher(value).find()) return true;
		String compact = value.replaceAll("\\s+", "");
		if (compact.length() < 32 || compact.length() % 4 == 1
				|| !compact.matches("[A-Za-z0-9+/]+={0,2}")) return false;
		try {
			byte[] decoded = Base64.getDecoder().decode(paddedBase64(compact));
			return starts(decoded, 0x89, 0x50, 0x4e, 0x47)
					|| starts(decoded, 0xff, 0xd8, 0xff)
					|| starts(decoded, 0x47, 0x49, 0x46, 0x38)
					|| starts(decoded, 0x42, 0x4d)
					|| decoded.length >= 12 && starts(decoded, 0x52, 0x49, 0x46, 0x46)
					&& decoded[8] == 0x57 && decoded[9] == 0x45 && decoded[10] == 0x42 && decoded[11] == 0x50;
		} catch (IllegalArgumentException invalidBase64) {
			return false;
		}
	}

	private static String paddedBase64(String value) {
		int remainder = value.length() % 4;
		if (remainder == 2) return value + "==";
		if (remainder == 3) return value + "=";
		return value;
	}

	private static boolean starts(byte[] value, int... prefix) {
		if (value.length < prefix.length) return false;
		for (int i = 0; i < prefix.length; i++) if ((value[i] & 255) != prefix[i]) return false;
		return true;
	}

	private void validateActor(Actor actor, String permission) {
		if (actor == null || actor.getUserId() == null || actor.getUserId() <= 0 || blank(actor.getUsername())
				|| !actor.hasPermission(permission)) throw new AccessDeniedException("无治理权限");
	}

	private void validateHistoryActor(Actor actor) {
		if (actor == null || actor.getUserId() == null || actor.getUserId() <= 0 || blank(actor.getUsername())
				|| !actor.hasPermission(RETRY_PERMISSION) && !actor.hasPermission(MANUAL_PERMISSION)) {
			throw new AccessDeniedException("无治理历史查看权限");
		}
	}

	private void enabled() {
		if (!properties.isActionsEnabled()) throw new AccessDeniedException("权限治理写动作尚未启用");
		properties.checkedMaxRetryAttempts();
	}

	private LocalDateTime requiredNow() {
		return Objects.requireNonNull(mapper.now(), "数据库UTC时间不能为空");
	}

	private static <T> Page<T> page(int current, int size) {
		if (current < 1 || size < 1 || size > MAX_PAGE_SIZE) throw new IllegalArgumentException("分页范围必须为current>=1且size为1至100");
		long offset = (long) (current - 1) * size;
		if (offset > Integer.MAX_VALUE) throw new IllegalArgumentException("分页偏移过大");
		return new Page<>(current, size);
	}

	private static String subject(Long targetId) {
		return "TARGET:" + targetId;
	}

	private static Long positive(Long value, String name) {
		if (value == null || value <= 0) throw new IllegalArgumentException(name + "必须为正数");
		return value;
	}

	private static String key(String value) {
		String result = text(value, 128, "幂等键");
		if (!result.matches("[A-Za-z0-9._:-]+")) throw new IllegalArgumentException("幂等键格式无效");
		return result;
	}

	private static String reason(String value) {
		return text(value, 512, "原因说明");
	}

	private static String text(String value, int max, String name) {
		String result = value == null ? null : value.trim();
		if (blank(result) || result.length() > max) throw new IllegalArgumentException(name + "长度无效");
		return result;
	}

	private static boolean blank(String value) {
		return value == null || value.trim().isEmpty();
	}

	public static String fingerprint(RetryCommand command) {
		return sha256(tuple("RETRY", command.getTargetId(), command.getExpectedOperationVersion(),
				command.getExpectedAttemptId(), command.getExpectedAttemptNo(), command.getExpectedState(), command.getReasonText()));
	}

	public static String fingerprint(ManualVerificationCommand command) {
		return sha256(tuple("MANUAL_VERIFICATION", command.getTargetId(), command.getExpectedOperationVersion(),
				command.getExpectedAttemptId(), command.getExpectedState(), command.getObservedConclusion(),
				command.getReasonText(), command.getEvidenceType(), command.getEvidenceReference(),
				command.getEvidenceBody(), command.getObservedAt()));
	}

	private static String tuple(Object... values) {
		StringBuilder out = new StringBuilder();
		for (Object value : values) {
			String text = String.valueOf(value);
			out.append(text.length()).append(':').append(text).append('|');
		}
		return out.toString();
	}

	private static String sha256(String value) {
		try {
			byte[] digest = MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));
			StringBuilder out = new StringBuilder(64);
			for (byte b : digest) out.append(String.format("%02x", b & 255));
			return out.toString();
		} catch (NoSuchAlgorithmException impossible) {
			throw new IllegalStateException(impossible);
		}
	}
}
