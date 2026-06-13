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
}
