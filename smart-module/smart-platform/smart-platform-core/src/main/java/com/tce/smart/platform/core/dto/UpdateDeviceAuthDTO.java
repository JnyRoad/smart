package com.tce.smart.platform.core.dto;

import java.util.List;

import lombok.Data;

/**
 * 批量修改员工权限策略参数
 * @author 齐佩
 *
 */
@Data
public class UpdateDeviceAuthDTO {

	/**
	 * 员工的id集合
	 */
	private List<String> ids;

	/**
	 * 权限策略
	 */
	private List<Integer> deviceAuthIds;

	/**
	 * 权限开始日期，格式为 yyyy-MM-dd；未传时服务端默认当天。
	 */
	private String startTime;

	/**
	 * 权限结束日期，格式为 yyyy-MM-dd；未传时服务端默认 2030-12-31。
	 */
	private String endTime;
}
