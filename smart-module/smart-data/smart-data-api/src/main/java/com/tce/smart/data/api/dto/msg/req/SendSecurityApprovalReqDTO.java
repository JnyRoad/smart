package com.tce.smart.data.api.dto.msg.req;

import lombok.Data;

/**
 * @description: 保安审批接口
 * @date: 2021/4/1 0001 17:28
 * @author: wuling
 * @version: 1.0
 */
@Data
public class SendSecurityApprovalReqDTO {

	/**
	 * oa流程单编号
	 */
	private String requestid;

	/**
	 * 单子申请人的工号
	 */
	private String userid;

	/**
	 * 备注
	 */
	private String remark;
}
