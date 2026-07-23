package com.tce.smart.platform.api.dto.req;

import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 当前认证员工通过人脸核验刷新门锁动态码的请求。
 */
@Data
public class SelfLockPwdRefreshReqDTO {
	@NotBlank(message = "人脸图不可为空")
	private String facePic;

	private String deviceNo;
}
