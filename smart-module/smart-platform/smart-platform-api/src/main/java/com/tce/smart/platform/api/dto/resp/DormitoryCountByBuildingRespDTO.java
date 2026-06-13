package com.tce.smart.platform.api.dto.resp;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * @description: 按楼栋统计入住情况相应DTO
 * @date: 2020/9/28 0028 15:57
 * @author: wuling
 * @version: 1.0
 */
@Data
public class DormitoryCountByBuildingRespDTO implements Serializable {

	private static final long serialVersionUID = -340877541960727744L;

	/**
	 * 园区ID
	 */
	@ApiModelProperty("园区ID")
	private Integer parkId;

	/**
	 * 楼栋ID
	 */
	@ApiModelProperty("楼栋ID")
	private Integer dormitoryId;

	/**
	 * 楼栋名称
	 */
	@ApiModelProperty("楼栋名称")
	private String dormitoryName;

	/**
	 * 床位总数
	 */
	@ApiModelProperty("床位总数")
	private Integer total;

	/**
	 * 实际入住人数
	 */
	@ApiModelProperty("实际入住人数")
	private Integer actualNumber;
}
