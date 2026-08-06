package com.tce.smart.platform.api.dto.resp.admittance;

import lombok.Data;

import java.io.Serializable;

/**
 * 已通过短信本人核验后可查看的最小通行码载荷。
 *
 * 不返回访客、接待人、园区或行程等资料，避免通行码展示端点重新成为详情泄露接口。
 */
@Data
public class VisitorPassCodeRespDTO implements Serializable {
	private static final long serialVersionUID = 1L;

	private String applyId;

	private Boolean valid;

	private String qrCode;

	private String smsCode;
}
