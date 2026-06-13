package com.tce.smart.platform.api.dto.resp.badge;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author fushiping
 * @date 2020/7/16 10:33
 **/
@Data
public class BadgeRecordRespDTO {

	/**
	 * 处理人账号
	 */
	private String createrName;
	/**
	 * 处理人角色
	 */
	private String createRole;
	/**
	 * 处理类型描述
	 */
	private String operateTypeDesc;
	/**
	 * 处理时间
	 */
	private LocalDateTime createTime;
	/**
	 * 处理类型标题
	 */
	private String operateTitleDesc;
	/**
	 * 备注
	 */
	private String remark;
}
