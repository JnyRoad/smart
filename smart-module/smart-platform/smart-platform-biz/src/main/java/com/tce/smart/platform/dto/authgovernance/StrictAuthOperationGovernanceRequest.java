package com.tce.smart.platform.dto.authgovernance;

import com.fasterxml.jackson.annotation.JsonAnySetter;

/** 治理写请求拒绝未知字段，防止客户端夹带 actor、trusted 或外部号。 */
public abstract class StrictAuthOperationGovernanceRequest {

	@JsonAnySetter
	public void rejectUnknownField(String field, Object value) {
		throw new IllegalArgumentException("治理请求包含未知字段: " + field);
	}
}
