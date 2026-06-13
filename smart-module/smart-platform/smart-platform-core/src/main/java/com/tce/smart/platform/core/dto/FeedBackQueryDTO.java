package com.tce.smart.platform.core.dto;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class FeedBackQueryDTO {

	/**
	 * 反馈员工工号
	 */
	private String staffBadge;

	/**
	 * 反馈员工姓名
	 */
	private String staffName;

	/**
	 * 反馈员工电话
	 */
	private String staffPhone;

	/**
	 * 反馈问题标签
	 */
	private String question;

	/**
	 * 反馈时间开始
	 */
	private String startTime;


	/**
	 * 反馈时间结束
	 */
	private String endTime;

	/**
	 * 处理状态 0-未处理 1-已处理
	 */
	private Integer status;
}
