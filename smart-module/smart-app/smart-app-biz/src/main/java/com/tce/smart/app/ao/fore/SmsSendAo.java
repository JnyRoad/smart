package com.tce.smart.app.ao.fore;

import com.tce.smart.common.core.ao.BaseAO;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 短信发送Ao
 *
 * @author mingkai.wu
 * @date 2019-05-09 15:13:30
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SmsSendAo extends BaseAO {

	/**
	 * 序列号
	 */
	private static final long serialVersionUID = 5409394882441484183L;

	/**
	 * 手机号
	 */
	private String mobile;
}
