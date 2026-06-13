package com.tce.smart.platform.api.dto.req;

import lombok.Data;

@Data
public class AddFeedBackReqDTO {

	/**
	 * 员工号
	 */
	private String staffBadge;

	/**
	 * 反馈的问题
	 */
	private String question;
}
