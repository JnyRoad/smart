package com.tce.smart.platform.api.dto.req.sddto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @description: 添加公共水电记录DTO
 * @date: 2020/9/29 8:48
 * @author: wuling
 * @version: 1.0
 */
@Data
public class AddCommonSDReqDTO implements Serializable {
	private static final long serialVersionUID = -5907833108804195451L;

	@ApiModelProperty(value = "记录ID")
	private Long id;

	@ApiModelProperty(value = "水电表名称")
	private String sdName;

	@ApiModelProperty(value = "收费项目ID 1.热水 2.冷水 3.电")
	private Integer categoryId;

	@ApiModelProperty(value = "园区ID")
	private Integer parkId;

	@ApiModelProperty(value = "楼栋ID")
	private Integer dormitoryId;

	@ApiModelProperty(value = "公摊房间列表")
	private List<Integer> roomIds;
}
