package com.tce.smart.platform.api.dto.resp;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * @description: DormitoryTypeRespDTO
 * @date: 2020/9/28 0028 18:12
 * @author: wuling
 * @version: 1.0
 */
@Data
public class DormitoryTypeRespDTO implements Serializable {
	private static final long serialVersionUID = 8555850449881356740L;

	/**
	 * 分类ID
	 */
	@ApiModelProperty("分类ID")
	private Integer id;
	/**
	 * 分类名称
	 */
	@ApiModelProperty("分类名称")
	private String typeName;

	/**
	 * 每个类型房间中床位的默认个数
	 */
	@ApiModelProperty("床位默认个数")
	private Integer bedTotal;

	/**
	 * 所属园区
	 */
	@ApiModelProperty("所属园区")
	private Integer parkId;
}
