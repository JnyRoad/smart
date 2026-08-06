package com.tce.smart.platform.api.dto.resp.admittance;

import lombok.Data;

import java.io.Serializable;

@Data
public class VisitorWechatIdentityRespDTO implements Serializable {
	private static final long serialVersionUID = 1L;

	private String openId;

	private String unionId;
}
