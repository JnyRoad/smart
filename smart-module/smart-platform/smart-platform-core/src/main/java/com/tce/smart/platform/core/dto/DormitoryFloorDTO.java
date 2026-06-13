package com.tce.smart.platform.core.dto;



import lombok.Data;

/**
 * 修改或添加宿舍楼层
 * @author dell
 *
 */
@Data
public class DormitoryFloorDTO {

	/**
	 * 宿舍楼ID
	 */
	private Integer dormitoryId;


	/**
	 * 宿舍楼楼层
	 */
	private Integer floorNum;


	/**
	 * 楼层起始编码
	 */
	private Integer startNum;

	/**
	 * 所属园区ID
	 */
	private Integer parkId;
}
