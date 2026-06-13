package com.tce.smart.data.api.dto.msg.req;

import com.tce.smart.common.core.ao.BaseAO;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 发送厂牌补领同意短信Ao
 *
 * @author fushiping
 * @date 2019-05-15 10:33:17
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class BadgeAgreeMsgReqDTO extends BaseAO {

	/**
	 * 序列号
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * 手机号码
	 */
	private String number;

	/**
	 * 模板编码
	 */
	private String tempCode;

	/**
	 * 领取地址
	 */
	private String adress;

}
