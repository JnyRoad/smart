package com.tce.smart.platform.dto.authgovernance;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;
import lombok.EqualsAndHashCode;

/** 人工观察证据只保存受限 JSON，不接受照片、密码或令牌。 */
@Data
@EqualsAndHashCode(callSuper = false)
public class AuthOperationManualEvidenceRequest extends StrictAuthOperationGovernanceRequest {
	private String type;
	private String reference;
	private String observedAt;
	private JsonNode body;
}
