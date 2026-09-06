package com.tce.smart.platform.client.release;

import com.tce.smart.platform.client.identity.ClientApiException;
import com.tce.smart.platform.client.identity.ClientPersonnelDirectory;
import com.tce.smart.platform.core.client.release.CardEvidence;
import com.tce.smart.platform.core.client.release.CardRole;
import com.tce.smart.platform.core.client.release.ConfidentialRelease;
import com.tce.smart.platform.core.client.release.ReleaseAction;
import org.springframework.stereotype.Service;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

/**
 * 首期使用员工主数据的工牌号复核押运人；凭证仅能从页面当前扫码输入带入，
 * 不信任客户端自报姓名或角色。后续读卡器可替换本实现并保留同一领域契约。
 */
@Service
public class StaffBadgeReleaseCardEvidenceVerifier implements ReleaseCardEvidenceVerifier {
	private final ClientPersonnelDirectory personnel;
	public StaffBadgeReleaseCardEvidenceVerifier(ClientPersonnelDirectory personnel) { this.personnel = personnel; }

	@Override
	public CardEvidence security(ConfidentialRelease release, String postId, ReleaseAction action,
			String operatorId, Instant now) {
		personnel.require(operatorId);
		return evidence(CardRole.SECURITY_CHECK, operatorId, release, postId, action, operatorId, now);
	}

	@Override
	public CardEvidence escort(ConfidentialRelease release, String postId, ReleaseAction action, String operatorId,
			String credential, Instant now) {
		if (!ReleaseAccessProperties.identifier(credential)) throw new ClientApiException(400);
		String escort = personnel.require(credential).getStaffNo();
		if (operatorId.equals(escort)) throw new ClientApiException(403);
		return evidence(CardRole.ESCORT, escort, release, postId, action, operatorId, now);
	}

	private CardEvidence evidence(CardRole role, String holder, ConfidentialRelease release, String postId,
			ReleaseAction action, String operatorId, Instant now) {
		return CardEvidence.verified(UUID.randomUUID().toString(), role, holder, release.getReleaseId(), postId,
				action, operatorId, now, now.plus(2, ChronoUnit.MINUTES));
	}
}
