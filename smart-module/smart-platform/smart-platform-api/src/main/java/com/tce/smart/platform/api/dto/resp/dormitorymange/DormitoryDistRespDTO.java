package com.tce.smart.platform.api.dto.resp.dormitorymange;

import io.swagger.annotations.ApiModelProperty;
import io.swagger.models.auth.In;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @description: 内宿自动分配结果DTO
 * @date: 2020/12/29
 * @author: wuling
 * @version: 1.0
 */
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class DormitoryDistRespDTO implements Serializable {
	private static final long serialVersionUID = -2765788873360377801L;

	@ApiModelProperty(value = "园区Id")
	private Integer parkId;

	@ApiModelProperty(value = "园区名称")
	private String parkName;

	@ApiModelProperty(value = "楼栋Id")
	private Integer dormitoryId;

	@ApiModelProperty(value = "楼栋名称")
	private String dormitoryName;

	@ApiModelProperty(value = "楼层Id")
	private Integer floorId;

	@ApiModelProperty(value = "楼层名称")
	private Integer floorName;

	@ApiModelProperty(value = "房间Id")
	private Integer roomId;

	@ApiModelProperty(value = "房间名称")
	private Integer roomName;

	@ApiModelProperty(value = "房间类型Id")
	private Integer roomTypeId;

	@ApiModelProperty(value = "房间类型名称")
	private String roomTypeName;

	@ApiModelProperty(value = "床位Id")
	private Integer bedId;

	@ApiModelProperty(value = "床位名称")
	private String bedName;

	@ApiModelProperty(value = "员工工号")
	private String staffBadge;

	@ApiModelProperty(value = "员工姓名")
	private String staffName;
}
