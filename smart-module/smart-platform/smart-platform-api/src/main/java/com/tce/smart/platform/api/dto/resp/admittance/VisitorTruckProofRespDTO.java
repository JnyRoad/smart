package com.tce.smart.platform.api.dto.resp.admittance;

import lombok.Data;

import java.io.Serializable;

/**
 * 短信本人核验成功后签发的短时货车预约 proof。
 *
 * proof 只在服务端 Redis 中绑定手机号，响应不回传手机号、验证码或申请资料。
 */
@Data
public class VisitorTruckProofRespDTO implements Serializable {
	private static final long serialVersionUID = 1L;

	private String proof;
}
