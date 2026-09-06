package com.tce.smart.platform.core.client.supplier;

import java.time.Duration;
import java.time.Instant;

/**
 * 供应商厂牌核验与明确进出登记的纯领域规则。
 *
 * 本类不解析二维码、不创建供应商账号，也不访问数据库、网络或认证设施。调用方必须传入
 * 当前可信资格、服务端认证操作人、岗位目录映射、人员区域状态、服务端时钟和服务端标识。
 * 版本检查只证明规则层拒绝旧快照；原子更新、核验消耗及幂等事件写入仍由持久化事务负责。
 */
public final class SupplierAccessWorkflow {

	private static final String EXECUTE_PERMISSION = "supplier:execute";
	private static final Duration MAX_VERIFICATION_VALIDITY = Duration.ofMinutes(5);

	public SupplierVerification verify(SupplierQualificationSnapshot qualification,
			SupplierOperator operator, SupplierPostAreaMapping postArea,
			SupplierPresenceSnapshot presence, Instant now, String verificationId) {
		requireNow(now);
		String normalizedVerificationId = requiredText(verificationId, "服务端核验编号不能为空");
		checkAuthority(operator, postArea);
		checkQualification(qualification, postArea.getAreaId(), now);
		checkPresence(presence, qualification.getPersonId(), postArea.getAreaId());

		Instant maximumExpiry = now.plus(MAX_VERIFICATION_VALIDITY);
		Instant expiresAt = qualification.getValidUntil().isBefore(maximumExpiry)
				? qualification.getValidUntil() : maximumExpiry;
		return new SupplierVerification(normalizedVerificationId, operator.getOperatorId(),
				postArea.getPostId(), postArea.getAreaId(), qualification, presence.getPresence(),
				presence.getVersion(), now, expiresAt);
	}

	public SupplierPassageResult record(SupplierVerification verification,
			SupplierQualificationSnapshot currentQualification, SupplierOperator currentOperator,
			SupplierPostAreaMapping currentPostArea, SupplierPresenceSnapshot currentPresence,
			SupplierDirection direction, Instant now, String eventId) {
		requireNow(now);
		String normalizedEventId = requiredText(eventId, "服务端事件编号不能为空");
		if (direction == null) {
			throw violation(SupplierRuleViolation.Code.INVALID_INPUT, "进出方向不能为空");
		}
		if (verification == null) {
			throw violation(SupplierRuleViolation.Code.VERIFICATION_MISMATCH, "缺少当前厂牌核验");
		}

		checkAuthority(currentOperator, currentPostArea);
		checkQualification(currentQualification, currentPostArea.getAreaId(), now);
		checkPresence(currentPresence, currentQualification.getPersonId(), currentPostArea.getAreaId());
		checkVerification(verification, currentQualification, currentOperator, currentPostArea,
				currentPresence, now);
		checkDirection(currentPresence.getPresence(), direction);

		if (currentPresence.getVersion() == Long.MAX_VALUE) {
			throw violation(SupplierRuleViolation.Code.VERSION_CONFLICT, "人员区域状态版本无法继续递增");
		}
		long nextVersion = currentPresence.getVersion() + 1L;
		SupplierPresence nextPresence = direction == SupplierDirection.ENTER
				? SupplierPresence.INSIDE : SupplierPresence.OUTSIDE;
		SupplierPresenceSnapshot updatedPresence = SupplierPresenceSnapshot.current(
				currentPresence.getPersonId(), currentPresence.getAreaId(), nextPresence, nextVersion);
		SupplierPassageEvent event = new SupplierPassageEvent(normalizedEventId,
				verification.getVerificationId(), currentOperator.getOperatorId(), currentPostArea.getPostId(),
				currentPostArea.getAreaId(), direction, now, nextVersion, currentQualification);
		return new SupplierPassageResult(updatedPresence, event);
	}

	private void checkAuthority(SupplierOperator operator, SupplierPostAreaMapping postArea) {
		if (operator == null || isBlank(operator.getOperatorId())) {
			throw violation(SupplierRuleViolation.Code.MISSING_PERMISSION, "缺少服务端认证操作人");
		}
		if (!operator.hasPermission(EXECUTE_PERMISSION)) {
			throw violation(SupplierRuleViolation.Code.MISSING_PERMISSION,
					"缺少权限：" + EXECUTE_PERMISSION);
		}
		if (postArea == null || isBlank(postArea.getPostId()) || isBlank(postArea.getAreaId())) {
			throw violation(SupplierRuleViolation.Code.INVALID_INPUT, "可信岗位区域映射不能为空");
		}
		if (!operator.isAuthorizedForPost(postArea.getPostId())) {
			throw violation(SupplierRuleViolation.Code.UNAUTHORIZED_POST, "操作人未获授权使用当前岗位");
		}
	}

	private void checkQualification(SupplierQualificationSnapshot qualification, String areaId, Instant now) {
		if (qualification == null) {
			throw violation(SupplierRuleViolation.Code.INVALID_INPUT, "可信供应商资格快照不能为空");
		}
		requireQualificationIdentifiers(qualification);
		if (!qualification.isBadgeActive() || !qualification.isPersonActive()
				|| !qualification.isCompanyActive() || !qualification.isAdmissionActive()) {
			throw violation(SupplierRuleViolation.Code.INACTIVE_QUALIFICATION,
					"厂牌、人员、单位或入厂申请当前已停用");
		}
		if (!qualification.isAdmissionApproved()) {
			throw violation(SupplierRuleViolation.Code.ADMISSION_NOT_APPROVED, "入厂申请当前未审批通过");
		}
		if (qualification.getValidFrom() == null || qualification.getValidUntil() == null
				|| !qualification.getValidUntil().isAfter(qualification.getValidFrom())) {
			throw violation(SupplierRuleViolation.Code.INVALID_INPUT, "供应商资格有效期无效");
		}
		if (now.isBefore(qualification.getValidFrom())) {
			throw violation(SupplierRuleViolation.Code.QUALIFICATION_NOT_YET_VALID, "供应商资格尚未生效");
		}
		if (!now.isBefore(qualification.getValidUntil())) {
			throw violation(SupplierRuleViolation.Code.QUALIFICATION_EXPIRED, "供应商资格已过期");
		}
		if (!qualification.getAuthorizedAreaIds().contains(areaId)) {
			throw violation(SupplierRuleViolation.Code.AREA_NOT_AUTHORIZED, "当前岗位区域不在入厂资格内");
		}
	}

	private void requireQualificationIdentifiers(SupplierQualificationSnapshot qualification) {
		requiredText(qualification.getBadgeId(), "可信厂牌标识不能为空");
		requiredText(qualification.getPersonId(), "可信人员标识不能为空");
		requiredText(qualification.getCompanyId(), "可信单位标识不能为空");
		requiredText(qualification.getAdmissionId(), "可信入厂申请标识不能为空");
	}

	private void checkPresence(SupplierPresenceSnapshot presence, String personId, String areaId) {
		if (presence == null || isBlank(presence.getPersonId()) || isBlank(presence.getAreaId())
				|| presence.getPresence() == null || presence.getVersion() < 0L) {
			throw violation(SupplierRuleViolation.Code.INVALID_INPUT, "人员区域状态快照无效");
		}
		if (!personId.equals(presence.getPersonId()) || !areaId.equals(presence.getAreaId())) {
			throw violation(SupplierRuleViolation.Code.PRESENCE_MISMATCH,
					"人员区域状态与当前资格或岗位区域不匹配");
		}
	}

	private void checkVerification(SupplierVerification verification,
			SupplierQualificationSnapshot currentQualification, SupplierOperator currentOperator,
			SupplierPostAreaMapping currentPostArea, SupplierPresenceSnapshot currentPresence,
			Instant now) {
		SupplierQualificationSnapshot verifiedQualification = verification.getQualificationSnapshot();
		if (isBlank(verification.getVerificationId()) || verifiedQualification == null
				|| !currentOperator.getOperatorId().equals(verification.getOperatorId())
				|| !currentPostArea.getPostId().equals(verification.getPostId())
				|| !currentPostArea.getAreaId().equals(verification.getAreaId())
				|| !sameQualificationIdentity(verifiedQualification, currentQualification)) {
			throw violation(SupplierRuleViolation.Code.VERIFICATION_MISMATCH,
					"厂牌核验与当前操作人、岗位、区域或资格主体不匹配");
		}
		if (verification.getVerifiedAt() == null || verification.getExpiresAt() == null
				|| !verification.getExpiresAt().isAfter(verification.getVerifiedAt())
				|| Duration.between(verification.getVerifiedAt(), verification.getExpiresAt())
						.compareTo(MAX_VERIFICATION_VALIDITY) > 0
				|| verification.getExpiresAt().isAfter(verifiedQualification.getValidUntil())
				|| now.isBefore(verification.getVerifiedAt())
				|| !now.isBefore(verification.getExpiresAt())) {
			throw violation(SupplierRuleViolation.Code.VERIFICATION_EXPIRED, "厂牌核验已过期，请重新扫描");
		}
		if (verification.getPresenceVersion() != currentPresence.getVersion()
				|| verification.getPresence() != currentPresence.getPresence()) {
			throw violation(SupplierRuleViolation.Code.VERSION_CONFLICT, "人员区域状态版本已变化");
		}
	}

	private boolean sameQualificationIdentity(SupplierQualificationSnapshot left,
			SupplierQualificationSnapshot right) {
		return left.getBadgeId().equals(right.getBadgeId())
				&& left.getPersonId().equals(right.getPersonId())
				&& left.getCompanyId().equals(right.getCompanyId())
				&& left.getAdmissionId().equals(right.getAdmissionId());
	}

	private void checkDirection(SupplierPresence presence, SupplierDirection direction) {
		if (presence == SupplierPresence.UNKNOWN) {
			return;
		}
		boolean allowed = presence == SupplierPresence.OUTSIDE
				? direction == SupplierDirection.ENTER : direction == SupplierDirection.LEAVE;
		if (!allowed) {
			throw violation(SupplierRuleViolation.Code.DUPLICATE_DIRECTION,
					"当前人员区域状态不允许重复登记同一方向");
		}
	}

	private String requiredText(String value, String message) {
		if (isBlank(value) || containsControlCharacter(value)) {
			throw violation(SupplierRuleViolation.Code.INVALID_INPUT, message);
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
			throw violation(SupplierRuleViolation.Code.INVALID_INPUT, "服务端时间不能为空");
		}
	}

	private SupplierRuleViolation violation(SupplierRuleViolation.Code code, String message) {
		return new SupplierRuleViolation(code, message);
	}
}
