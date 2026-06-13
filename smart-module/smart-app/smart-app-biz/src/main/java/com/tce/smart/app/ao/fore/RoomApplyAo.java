package com.tce.smart.app.ao.fore;

import lombok.Data;

/**
 * 内宿申请
 * @author Administrator
 *
 */
@Data
public class RoomApplyAo {
	/**
	 *  园区ID
	 */
	private Integer parkId;
	/**
	 * 员工号
	 */
	private String staffBadge;

	/**
	 * 床位类型
	 */
	private Integer bedType;
}
