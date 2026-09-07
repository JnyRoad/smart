package com.tce.smart.platform.client.release;

import com.tce.smart.platform.core.client.release.CardEvidence;
import com.tce.smart.platform.core.client.release.ConfidentialRelease;
import com.tce.smart.platform.core.client.release.ReleaseAction;
import java.time.Instant;

/** 将现场扫码头输入的工牌与当前操作绑定为一次卡证证据；可替换为读卡器签名适配器。 */
public interface ReleaseCardEvidenceVerifier {
	CardEvidence security(ConfidentialRelease release, String postId, ReleaseAction action, String operatorId, Instant now);
	CardEvidence escort(ConfidentialRelease release, String postId, ReleaseAction action, String operatorId,
			String credential, Instant now);
}
