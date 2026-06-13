package com.tce.smart.platform.api.dto.resp.bigdatapanel;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * @description: 大数据面板-园区数据实体类
 * @date: 2020-08-04 11:26
 * @author: wuling
 * @version: 1.0
 */

@Data
public class ParkDataRespDTO implements Serializable {
	private static final long serialVersionUID = -2854082698077085664L;

	/**
	 * 园区数量
	 */
	@ApiModelProperty("园区数量")
	private Integer parkCount;

	/**
	 * 涉及城市
	 */
	@ApiModelProperty("涉及城市")
	private Integer cityCount;
}
