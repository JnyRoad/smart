package com.tce.smart.platform.api.dto.req;

import lombok.Data;

@Data
public class SearchPaperRecordReqDTO {

	/**
	 * 问卷唯一标识
	 */
	private Integer paperId;

	/**
	 * 员工号
	 */
	private String badge;
}
