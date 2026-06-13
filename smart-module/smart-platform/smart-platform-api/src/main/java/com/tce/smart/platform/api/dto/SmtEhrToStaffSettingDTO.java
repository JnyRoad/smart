package com.tce.smart.platform.api.dto;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class SmtEhrToStaffSettingDTO {

	private Integer id;

	/**
	 * BUId
	 */
	private String compId;

	/**
	 * 同步时长
	 */
	private Integer time;
	/**
	 * 同步单位
	 */
	private String timeUnit;
	/**
	 * 同步时间 s
	 */
	private Integer timeSecond;

	/**
	 * 创建时间
	 */
	private LocalDateTime createTime;

	/**
	 * 创建人
	 */
	private String createUser;
}
