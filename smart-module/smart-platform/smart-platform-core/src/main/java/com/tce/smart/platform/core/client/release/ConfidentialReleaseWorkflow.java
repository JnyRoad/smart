package com.tce.smart.platform.core.client.release;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * 保密物品放行纯领域规则。
 *
 * 本类不访问数据库、网络或认证设施。调用方必须传入服务端认证身份、授权岗位、
 * 已验证卡证快照、服务端时钟与服务端生成的标识。版本检查只保护当前规则调用，
 * 持久化层仍需使用条件更新与事务解决并发和幂等问题。
 */
public final class ConfidentialReleaseWorkflow {

	private static final String APPLY_PERMISSION = "item-pass:apply";
	private static final String APPROVE_PERMISSION = "item-pass:approve";
	private static final String EXECUTE_PERMISSION = "item-pass:execute";
	private static final Duration MAX_CARD_VALIDITY = Duration.ofMinutes(5);

	public ConfidentialRelease create(ReleaseApplicationRequest request, ReleaseCreationContext context,
			Instant now, String releaseId, String eventId) {
		if (request == null || context == null || context.getApplicant() == null) {
			throw violation(ReleaseRuleViolation.Code.INVALID_INPUT, "申请与服务端上下文不能为空");
		}
		ReleasePrincipal applicant = context.getApplicant();
		String applicantId = requiredText(applicant.getActorId(), ReleaseRuleViolation.Code.INVALID_INPUT,
				"申请人不能为空");
		requirePermission(applicant, APPLY_PERMISSION);

		String originPostId = requiredText(request.getOriginPostId(), ReleaseRuleViolation.Code.INVALID_ROUTE,
				"起点岗位不能为空");
		String destinationPostId = requiredText(request.getDestinationPostId(),
				ReleaseRuleViolation.Code.INVALID_ROUTE, "终点岗位不能为空");
		if (originPostId.equals(destinationPostId)
				|| !context.getCandidatePostIds().contains(originPostId)
				|| !context.getCandidatePostIds().contains(destinationPostId)) {
			throw violation(ReleaseRuleViolation.Code.INVALID_ROUTE, "起终点必须来自服务端候选且不能相同");
		}

		String title = requiredText(request.getTitle(), ReleaseRuleViolation.Code.INVALID_INPUT, "标题不能为空");
		String reason = requiredText(request.getReason(), ReleaseRuleViolation.Code.INVALID_INPUT, "原因不能为空");
		List<String> materials = normalizeMaterials(request.getMaterials());
		List<String> sealCodes = normalizeSealCodes(request.getSealCodes());
		String assignedApproverId = requiredText(context.getAssignedApproverId(),
				ReleaseRuleViolation.Code.INVALID_INPUT, "服务端指派审批人不能为空");
		String normalizedReleaseId = requiredText(releaseId, ReleaseRuleViolation.Code.INVALID_INPUT,
				"服务端单号不能为空");
		String normalizedEventId = requiredText(eventId, ReleaseRuleViolation.Code.INVALID_INPUT,
				"服务端事件编号不能为空");
		requireNow(now);

		ReleaseAuditEvent event = new ReleaseAuditEvent(normalizedEventId, normalizedReleaseId,
				ReleaseAction.CREATE, applicantId, null, null, ReleaseStatus.PENDING, now, null, null, null,
				null, null, 1L);
		return new ConfidentialRelease(normalizedReleaseId, applicantId, assignedApproverId, title, reason,
				materials, sealCodes, originPostId, destinationPostId, ReleaseStatus.PENDING, null, null, 1L,
				Collections.singletonList(event));
	}

	public ConfidentialRelease approve(ConfidentialRelease current, ReleasePrincipal approver,
			long expectedVersion, Instant now, String eventId) {
		checkApprovalContext(current, approver, expectedVersion);
		return transition(current, ReleaseStatus.APPROVED, ReleaseAction.APPROVE, approver.getActorId(), null,
				null, null, null, null, null, now, eventId);
	}

	public ConfidentialRelease reject(ConfidentialRelease current, ReleasePrincipal approver,
			long expectedVersion, String rejectionReason, Instant now, String eventId) {
		checkApprovalContext(current, approver, expectedVersion);
		String normalizedReason = requiredText(rejectionReason,
				ReleaseRuleViolation.Code.INVALID_REJECTION_REASON, "驳回原因不能为空");
		return transition(current, ReleaseStatus.REJECTED, ReleaseAction.REJECT, approver.getActorId(), null,
				normalizedReason, null, null, null, null, now, eventId);
	}

	public ConfidentialRelease depart(ConfidentialRelease current, ReleasePrincipal operator,
			long expectedVersion, EscortMode escortMode, String positioningLockId,
			CardEvidence securityEvidence, CardEvidence escortEvidence, Instant now, String eventId) {
		checkSnapshot(current, expectedVersion, ReleaseStatus.APPROVED);
		checkExecutionAuthority(operator, current.getOriginPostId());
		requireNow(now);
		validateCardEvidence(securityEvidence, CardRole.SECURITY_CHECK, operator.getActorId(), true,
				current, current.getOriginPostId(), ReleaseAction.DEPART, now);
		String normalizedLockId = validateEscort(escortMode, positioningLockId, securityEvidence,
				escortEvidence, operator, current, current.getOriginPostId(), ReleaseAction.DEPART, now);
		return transition(current, ReleaseStatus.TRANSPORTING, ReleaseAction.DEPART, operator.getActorId(),
				current.getOriginPostId(), null, escortMode, normalizedLockId, securityEvidence, escortEvidence,
				now, eventId);
	}

	public ConfidentialRelease arrive(ConfidentialRelease current, ReleasePrincipal operator,
			long expectedVersion, EscortMode escortMode, String positioningLockId,
			CardEvidence securityEvidence, CardEvidence escortEvidence, Instant now, String eventId) {
		checkSnapshot(current, expectedVersion, ReleaseStatus.TRANSPORTING);
		checkExecutionAuthority(operator, current.getDestinationPostId());
		if (escortMode != current.getEscortMode()) {
			throw violation(ReleaseRuleViolation.Code.ESCORT_METHOD_CHANGED, "到达时不得更换押运方式");
		}
		requireNow(now);
		validateCardEvidence(securityEvidence, CardRole.SECURITY_CHECK, operator.getActorId(), true,
				current, current.getDestinationPostId(), ReleaseAction.ARRIVE, now);
		String normalizedLockId = validateEscort(escortMode, positioningLockId, securityEvidence,
				escortEvidence, operator, current, current.getDestinationPostId(), ReleaseAction.ARRIVE, now);
		if (escortMode == EscortMode.POSITIONING_LOCK
				&& !current.getPositioningLockId().equals(normalizedLockId)) {
			throw violation(ReleaseRuleViolation.Code.LOCK_ID_CHANGED, "到达时定位锁编号必须与出发登记一致");
		}
		return transition(current, ReleaseStatus.COMPLETED, ReleaseAction.ARRIVE, operator.getActorId(),
				current.getDestinationPostId(), null, current.getEscortMode(), current.getPositioningLockId(),
				securityEvidence, escortEvidence, now, eventId);
	}

	private void checkApprovalContext(ConfidentialRelease current, ReleasePrincipal approver,
			long expectedVersion) {
		checkSnapshot(current, expectedVersion, ReleaseStatus.PENDING);
		if (approver == null) {
			throw violation(ReleaseRuleViolation.Code.MISSING_PERMISSION, "缺少服务端认证审批人");
		}
		String approverId = requiredText(approver.getActorId(), ReleaseRuleViolation.Code.MISSING_PERMISSION,
				"缺少服务端认证审批人");
		requirePermission(approver, APPROVE_PERMISSION);
		if (!current.getAssignedApproverId().equals(approverId)) {
			throw violation(ReleaseRuleViolation.Code.NOT_ASSIGNED_APPROVER, "仅当前指派审批人可以处理");
		}
		if (current.getApplicantId().equals(approverId)) {
			throw violation(ReleaseRuleViolation.Code.SELF_APPROVAL, "申请人不得审批本人申请");
		}
	}

	private void checkExecutionAuthority(ReleasePrincipal operator, String requiredPostId) {
		if (operator == null) {
			throw violation(ReleaseRuleViolation.Code.MISSING_PERMISSION, "缺少服务端认证操作人");
		}
		requiredText(operator.getActorId(), ReleaseRuleViolation.Code.MISSING_PERMISSION,
				"缺少服务端认证操作人");
		requirePermission(operator, EXECUTE_PERMISSION);
		if (!operator.isAuthorizedForPost(requiredPostId)) {
			throw violation(ReleaseRuleViolation.Code.UNAUTHORIZED_POST, "操作人未获授权办理当前岗位");
		}
	}

	private void checkSnapshot(ConfidentialRelease current, long expectedVersion, ReleaseStatus expectedStatus) {
		if (current == null) {
			throw violation(ReleaseRuleViolation.Code.INVALID_INPUT, "放行单快照不能为空");
		}
		if (current.getVersion() != expectedVersion) {
			throw violation(ReleaseRuleViolation.Code.VERSION_CONFLICT, "放行单版本已变化");
		}
		if (current.getStatus() != expectedStatus) {
			throw violation(ReleaseRuleViolation.Code.INVALID_STATUS, "当前状态不允许执行该动作");
		}
	}

	private String validateEscort(EscortMode escortMode, String positioningLockId,
			CardEvidence securityEvidence, CardEvidence escortEvidence, ReleasePrincipal operator,
			ConfidentialRelease release, String postId, ReleaseAction action, Instant now) {
		if (escortMode == null) {
			throw violation(ReleaseRuleViolation.Code.INVALID_ESCORT, "必须选择押运方式");
		}
		if (escortMode == EscortMode.POSITIONING_LOCK) {
			if (escortEvidence != null) {
				throw violation(ReleaseRuleViolation.Code.INVALID_ESCORT, "定位锁方式不得同时提交押运卡证");
			}
			return requiredText(positioningLockId, ReleaseRuleViolation.Code.INVALID_ESCORT,
					"定位锁编号不能为空");
		}
		if (!isBlank(positioningLockId)) {
			throw violation(ReleaseRuleViolation.Code.INVALID_ESCORT, "刷卡押运方式不得同时提交定位锁编号");
		}
		if (escortEvidence == null) {
			throw violation(ReleaseRuleViolation.Code.INVALID_ESCORT, "刷卡押运必须提交押运卡证证明");
		}
		validateCardEvidence(escortEvidence, CardRole.ESCORT, operator.getActorId(), false,
				release, postId, action, now);
		if (securityEvidence.getEvidenceId() != null
				&& securityEvidence.getEvidenceId().equals(escortEvidence.getEvidenceId())) {
			throw violation(ReleaseRuleViolation.Code.INVALID_CARD_EVIDENCE, "安检与押运必须使用不同角色证明");
		}
		return null;
	}

	private void validateCardEvidence(CardEvidence evidence, CardRole expectedRole, String expectedOperatorId,
			boolean holderMustMatch, ConfidentialRelease release, String postId, ReleaseAction action,
			Instant now) {
		if (evidence == null
				|| isBlank(evidence.getEvidenceId())
				|| evidence.getRole() != expectedRole
				|| isBlank(evidence.getHolderId())
				|| !release.getReleaseId().equals(evidence.getReleaseId())
				|| !postId.equals(evidence.getPostId())
				|| evidence.getAction() != action
				|| !matchesOperator(expectedOperatorId, holderMustMatch, evidence)
				|| evidence.getVerifiedAt() == null
				|| evidence.getValidUntil() == null
				|| evidence.getVerifiedAt().isAfter(now)
				|| !evidence.getValidUntil().isAfter(now)
				|| !evidence.getValidUntil().isAfter(evidence.getVerifiedAt())
				|| Duration.between(evidence.getVerifiedAt(), evidence.getValidUntil())
						.compareTo(MAX_CARD_VALIDITY) > 0) {
			throw violation(ReleaseRuleViolation.Code.INVALID_CARD_EVIDENCE,
					"卡证证明与当前单据、岗位、动作、操作人或有效期不匹配");
		}
	}

	private boolean matchesOperator(String expectedOperatorId, boolean holderMustMatch, CardEvidence evidence) {
		if (isBlank(expectedOperatorId) || !expectedOperatorId.equals(evidence.getOperatorId())) {
			return false;
		}
		return !holderMustMatch || expectedOperatorId.equals(evidence.getHolderId());
	}

	private ConfidentialRelease transition(ConfidentialRelease current, ReleaseStatus targetStatus,
			ReleaseAction action, String actorId, String postId, String reason, EscortMode escortMode,
			String positioningLockId, CardEvidence securityEvidence, CardEvidence escortEvidence,
			Instant now, String eventId) {
		requireNow(now);
		String normalizedEventId = requiredText(eventId, ReleaseRuleViolation.Code.INVALID_INPUT,
				"服务端事件编号不能为空");
		long nextVersion = current.getVersion() + 1L;
		ReleaseAuditEvent event = new ReleaseAuditEvent(normalizedEventId, current.getReleaseId(), action,
				actorId, postId, current.getStatus(), targetStatus, now, reason, escortMode, positioningLockId,
				securityEvidence, escortEvidence, nextVersion);
		List<ReleaseAuditEvent> auditTrail = new ArrayList<>(current.getAuditTrail());
		auditTrail.add(event);
		return new ConfidentialRelease(current.getReleaseId(), current.getApplicantId(),
				current.getAssignedApproverId(), current.getTitle(), current.getReason(), current.getMaterials(),
				current.getSealCodes(), current.getOriginPostId(), current.getDestinationPostId(), targetStatus,
				escortMode == null ? current.getEscortMode() : escortMode,
				positioningLockId == null ? current.getPositioningLockId() : positioningLockId,
				nextVersion, auditTrail);
	}

	private List<String> normalizeMaterials(List<String> materials) {
		if (materials == null || materials.isEmpty()) {
			throw violation(ReleaseRuleViolation.Code.INVALID_INPUT, "至少需要一项物料");
		}
		List<String> normalized = new ArrayList<>(materials.size());
		for (String material : materials) {
			normalized.add(requiredText(material, ReleaseRuleViolation.Code.INVALID_INPUT, "物料不能为空"));
		}
		return normalized;
	}

	private List<String> normalizeSealCodes(List<String> sealCodes) {
		if (sealCodes == null || sealCodes.isEmpty()) {
			return Collections.emptyList();
		}
		List<String> normalized = new ArrayList<>(sealCodes.size());
		Set<String> unique = new LinkedHashSet<>();
		for (String sealCode : sealCodes) {
			String value = requiredText(sealCode, ReleaseRuleViolation.Code.INVALID_SEAL, "封条编号不能为空");
			if (!unique.add(value)) {
				throw violation(ReleaseRuleViolation.Code.INVALID_SEAL, "封条编号不能重复");
			}
			normalized.add(value);
		}
		return normalized;
	}

	private void requirePermission(ReleasePrincipal principal, String permission) {
		if (!principal.hasPermission(permission)) {
			throw violation(ReleaseRuleViolation.Code.MISSING_PERMISSION, "缺少权限：" + permission);
		}
	}

	private String requiredText(String value, ReleaseRuleViolation.Code code, String message) {
		if (isBlank(value) || containsControlCharacter(value)) {
			throw violation(code, message);
		}
		return value.trim();
	}

	private boolean isBlank(String value) {
		return value == null || value.trim().isEmpty();
	}

	private boolean containsControlCharacter(String value) {
		if (value == null) {
			return false;
		}
		for (int index = 0; index < value.length(); index++) {
			if (Character.isISOControl(value.charAt(index))) {
				return true;
			}
		}
		return false;
	}

	private void requireNow(Instant now) {
		if (now == null) {
			throw violation(ReleaseRuleViolation.Code.INVALID_INPUT, "服务端时间不能为空");
		}
	}

	private ReleaseRuleViolation violation(ReleaseRuleViolation.Code code, String message) {
		return new ReleaseRuleViolation(code, message);
	}
}
