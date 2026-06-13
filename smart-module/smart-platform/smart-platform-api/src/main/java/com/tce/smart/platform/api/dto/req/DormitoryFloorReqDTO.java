package com.tce.smart.platform.api.dto.req;



import com.tce.smart.common.core.dto.BaseDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 修改或添加宿舍楼层
 * @author dell
 *
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class DormitoryFloorReqDTO extends BaseDTO {

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
