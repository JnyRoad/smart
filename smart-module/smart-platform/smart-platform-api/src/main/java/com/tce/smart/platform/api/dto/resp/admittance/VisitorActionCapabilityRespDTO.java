package com.tce.smart.platform.api.dto.resp.admittance;

import lombok.Data;

import java.io.Serializable;

/** 一次性访客业务动作 capability，只能置于请求头，禁止出现在 URL。 */
@Data
public class VisitorActionCapabilityRespDTO implements Serializable {
	private static final long serialVersionUID = 1L;

	private String capability;
}
