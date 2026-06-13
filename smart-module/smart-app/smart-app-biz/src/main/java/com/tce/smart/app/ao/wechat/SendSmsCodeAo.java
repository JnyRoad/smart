package com.tce.smart.app.ao.wechat;

import com.tce.smart.common.core.ao.BaseAO;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Wechat应聘简历填写验证码Ao
 *
 * @author mingkai.wu
 * @date 2019-05-09 15:13:00
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SendSmsCodeAo extends BaseAO {

	/**
	 * 序列号
	 */
	private static final long serialVersionUID = 2938173266332492810L;

	/**
	 * 招聘岗位id
	 */
	private Integer applicationId;

	/**
	 * 手机号码
	 */
	private String mobile;
}
