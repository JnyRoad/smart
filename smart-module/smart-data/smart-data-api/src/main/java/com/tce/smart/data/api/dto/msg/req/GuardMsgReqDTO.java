package com.tce.smart.data.api.dto.msg.req;

import com.tce.smart.common.core.ao.BaseAO;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 发送预约短信Ao
 *
 * @author mingkai.wu
 * @date 2019-05-15 10:33:17
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class GuardMsgReqDTO extends BaseAO {

	/**
	 * 序列号
	 */
	private static final long serialVersionUID = -145162091570332772L;

	/**
	 * 手机号码
	 */
	private String number;

	/**
	 * 模板编码
	 */
	private String tempCode;

	/**
	 * 访客姓名
	 */
	private String visitorName;

	/**
	 * 预计来访时间
	 */
	private String appointmentDate;

	/**
	 * 被访园区
	 */
	private String parkName;

	/**
	 * 车牌号
	 */
	private String plat;


}
