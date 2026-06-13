package com.tce.smart.platform.api.dto.resp.bigdatapanel;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * @description: 大数据面板-园区概述数据实体类
 * @date: 2020-08-04 15:02
 * @author: wuling
 * @version: 1.0
 */

@Data
public class ParkSummaryRespDTO implements Serializable {

	private static final long serialVersionUID = -4163999245683849463L;

	/**
	 * 园区面积
	 */
	@ApiModelProperty("园区面积")
	private Double areaAmount;

	/**
	 * 厂房数量
	 */
	@ApiModelProperty("厂房数量")
	private Integer factoryCount;

	/**
	 * 宿舍楼数量
	 */
	@ApiModelProperty("宿舍楼数量")
	private Integer dormitoryCount;

	/**
	 * 餐厅楼数量
	 */
	@ApiModelProperty("餐厅楼数量")
	private Integer diningRoomCount;
}
