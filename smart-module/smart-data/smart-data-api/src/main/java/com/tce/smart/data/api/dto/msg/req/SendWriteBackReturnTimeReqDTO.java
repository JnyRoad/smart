package com.tce.smart.data.api.dto.msg.req;

import lombok.Data;

/**
 * @author: Li.JiaJun
 * @since: 2021/8/10 13:49
 */
@Data
public class SendWriteBackReturnTimeReqDTO {

	/**
	 * oa流程单编号
	 */
	private String requestid;

	/**
	 * 返厂日期
	 */
	private String fcrq;

	/**
	 * 返厂时间
	 */
	private String fcsj;
}
