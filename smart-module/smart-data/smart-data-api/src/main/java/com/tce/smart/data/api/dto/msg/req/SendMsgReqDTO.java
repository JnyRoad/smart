package com.tce.smart.data.api.dto.msg.req;

import com.tce.smart.common.core.ao.BaseAO;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 发送短信Ao
 *
 * @author mingkai.wu
 * @date 2019-05-15 10:33:17
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SendMsgReqDTO extends BaseAO {

	/**
	 * 序列号
	 */
	private static final long serialVersionUID = -145162091570332772L;

	/**
	 * 手机号码
	 */
	private String number;

	/**
	 * 短信内容
	 */
	private String contents;

	/**
	 * 短信模板code
	 */
	private String tempCode;

}
