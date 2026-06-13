package com.tce.smart.platform.core.dto;

import lombok.Data;

@Data
public class SearchToC6DTO {

	/**
	 * 员工号
	 */
	private String empNo;

	/**
	 * 姓名
	 */
	private String name;

	/**
	 * 开始时间
	 */
	private String startTime;

	/**
	 * 结束时间
	 */
	private String endTime;


}
