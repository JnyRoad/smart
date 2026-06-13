package com.tce.smart.platform.core.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

/**
 * @description: StaffAppealSearchDTO
 * @date: 2020-07-23 16:23
 * @author: wuling
 * @version: 1.0
 */
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class StaffAppealSearchDTO {

	/**
	 * 标识Id
	 */
	private Long id;

	/**
	 * 员工标识
	 */
	private String staffBadge;

	/**
	 * 申诉类型
	 */
	private Integer appealType;

	/**
	 * 开始时间
	 */
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private Date beginTime;

	/**
	 * 结束时间
	 */
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private Date endTime;

	/**
	 * 状态
	 */
	private Integer status;
}
