package com.tce.smart.data.api.dto.msg.req;

import lombok.Data;

/**
 * 短息发送失败后，再次提醒用户短息内容
 * @author QIPEI
 *
 */
@Data
public class SendSmsErrorReqDTO {

	/**
	 * 手机号
	 */
	private String phoneNumber;

	/**
	 * 错误模板名称
	 */
	private String tempNameError;


	/**
	 * 错误原因
	 */
	private String remark;

	/**
	 * 模板编码
	 */
	private String tempCode;
}
