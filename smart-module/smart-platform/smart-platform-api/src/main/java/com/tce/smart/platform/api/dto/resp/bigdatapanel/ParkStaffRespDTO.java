package com.tce.smart.platform.api.dto.resp.bigdatapanel;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * @description: 大数据面板-园区员工数据实体类
 * @date: 2020-08-04 15:02
 * @author: wuling
 * @version: 1.0
 */

@Data
public class ParkStaffRespDTO implements Serializable {

	private static final long serialVersionUID = -4163999245683849463L;

	/**
	 * BU数量
	 */
	@ApiModelProperty("BU数量")
	private Integer BUCount;

	/**
	 * 部门数量
	 */
	@ApiModelProperty("部门数量")
	private Integer depCount;

    /**
     * 岗位数量
	 */
	@ApiModelProperty("岗位数量")
	private Integer postsCount;

	/**
	 * 员工数量
	 */
	@ApiModelProperty("员工数量")
	private Integer staffCount;
}
