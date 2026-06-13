package com.tce.smart.data.api.dto.msg.req;

import com.tce.smart.common.core.ao.BaseAO;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 发送离职通知短信Ao
 *
 * @author mingkai.wu
 * @date 2019-05-15 10:33:17
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class DimissionMsgReqDTO extends BaseAO {

	/**
	 * 序列号
	 */
	private static final long serialVersionUID = 7786508733278020397L;

	/**
	 * 手机号码
	 */
	private String number;

	/**
	 * 模板编码
	 */
	private String tempCode;

	/**
	 * 离职人员姓名
	 */
	private String dimissioName;

}
