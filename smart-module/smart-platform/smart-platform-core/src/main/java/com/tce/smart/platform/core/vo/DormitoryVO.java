package com.tce.smart.platform.core.vo;



import lombok.Data;

/**
 * 宿舍楼信息
 * @author 齐佩
 *
 */
@Data
public class DormitoryVO {


	/**
	 * 宿舍楼ID
	 */
	private Integer id;
	/**
	 * 宿舍楼名称
	 */
	private String dormitoryName;

	/**
	 * 宿舍楼楼层
	 */
	private Integer floorNum;

	/**
	 * 所属园区ID
	 */
	private Integer parkId;

	/**
	 * 所属园区名称
	 */
	private String parkName;
}
