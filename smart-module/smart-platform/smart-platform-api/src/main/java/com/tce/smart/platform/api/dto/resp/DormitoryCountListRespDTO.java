package com.tce.smart.platform.api.dto.resp;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * @description: DormitoryCountListRespDTO
 * @date: 2020/9/28 15:08
 * @author: wuling
 * @version: 1.0
 */
@Data
public class DormitoryCountListRespDTO implements Serializable {

	private static final long serialVersionUID = 1906277213444682886L;

	/**
	 * 园区ID
	 */
	@ApiModelProperty("园区ID")
	private Integer parkId;

	/**
	 * 园区名称
	 */
	@ApiModelProperty("园区名称")
	private String parkName;

	/**
	 * 楼栋数
	 */
	@ApiModelProperty("楼栋数")
	private Integer buildingNumber;

	/**
	 * 容纳人数
	 */
	@ApiModelProperty("容纳人数")
	private Integer total;

	/**
	 * 实际入住人数
	 */
	@ApiModelProperty("实际入住人数")
	private Integer actualNumber;
}
