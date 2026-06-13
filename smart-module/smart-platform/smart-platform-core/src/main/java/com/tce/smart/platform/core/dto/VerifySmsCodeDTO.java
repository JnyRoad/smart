package com.tce.smart.platform.core.dto;


import com.tce.smart.common.core.ao.BaseAO;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 应聘者校验短信验证码Ao
 *
 * @author mingkai.wu
 * @date 2019-05-09 15:13:00
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class VerifySmsCodeDTO  extends BaseAO{
	/**
	 * 序列号
	 */
	private static final long serialVersionUID = 2938173266332492810L;

	/**
	 * 招聘岗位id
	 */
	private String applicationId;

	/**
	 * 手机号码
	 */
	private String mobile;

	/**
	 * 短信验证码
	 */
	private String smsCode;
}
