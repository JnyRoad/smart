package com.tce.smart.platform.dto.authgovernance;

import lombok.Data;
import lombok.EqualsAndHashCode;
import java.util.List;

/** 已知完全未发送尝试的有界重试请求。 */
@Data
@EqualsAndHashCode(callSuper = false)
public class AuthOperationRetryRequest extends StrictAuthOperationGovernanceRequest {
	private String idempotencyKey;
	private String reasonText;
	private List<AuthOperationRetryItem> targets;
}
