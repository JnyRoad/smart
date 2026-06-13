package com.tce.smart.platform.api.dto.req;

import lombok.Data;

import java.util.List;

@Data
public class SearchOutDormitoryReqDTO {

	/**
	 * 员工工号
	 */
	private String staffBadge;

	private String staffName;

	private String startTime;

	private String endTime;

	private List<Integer> parkIds;
}
