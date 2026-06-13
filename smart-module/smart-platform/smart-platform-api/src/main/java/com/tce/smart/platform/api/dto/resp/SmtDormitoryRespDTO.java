package com.tce.smart.platform.api.dto.resp;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * @description: SmtDormitoryRespDTO
 * @date: 2020/9/28 0028 17:58
 * @author: wuling
 * @version: 1.0
 */
@Data
public class SmtDormitoryRespDTO implements Serializable {
	private static final long serialVersionUID = -4812420491094537332L;

	/**
	 * 宿舍楼ID
	 */
	@ApiModelProperty("宿舍楼ID")
	private Integer id;
	/**
	 * 宿舍楼名称
	 */
	@ApiModelProperty("宿舍楼名称")
	private String dormitoryName;

	/**
	 * 宿舍楼楼层
	 */
	@ApiModelProperty("宿舍楼楼层数")
	private Integer floorNum;

	/**
	 * 所属园区ID
	 */
	@ApiModelProperty("园区ID")
	private Integer parkId;
}
