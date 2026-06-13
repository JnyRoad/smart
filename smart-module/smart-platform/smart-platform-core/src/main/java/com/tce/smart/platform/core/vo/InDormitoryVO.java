package com.tce.smart.platform.core.vo;

import lombok.Data;

/**
 * 申请内宿返回值
 * @author qipei
 *
 */
@Data
public class InDormitoryVO {

	/**
	 * 楼栋名称
	 */
	private String buildingName;

	/**
	 * 楼层名称
	 */
	private String floor;

	/**
	 * 房间名称
	 */
	private String room;

	/**
	 * 最大入住数
	 */
	private String maxBed;

	/**
	 * 床位名称
	 */
	private String bedName;



}
