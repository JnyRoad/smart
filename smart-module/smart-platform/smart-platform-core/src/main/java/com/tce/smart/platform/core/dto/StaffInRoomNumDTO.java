package com.tce.smart.platform.core.dto;

import lombok.Data;

/**
 * 员工住房数量批量查询结果，用于替代按工号逐条查询（避免宿舍水电导出接口的 N+1 查询）。
 */
@Data
public class StaffInRoomNumDTO {

	/**
	 * 员工工号
	 */
	private String badge;

	/**
	 * 住过的房间数
	 */
	private Integer inRoomNum;
}
