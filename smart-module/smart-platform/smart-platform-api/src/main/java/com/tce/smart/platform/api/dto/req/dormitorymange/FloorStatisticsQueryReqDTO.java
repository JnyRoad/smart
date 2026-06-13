package com.tce.smart.platform.api.dto.req.dormitorymange;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.bytebuddy.description.field.FieldDescription;

import java.io.Serializable;
import java.util.List;

/**
 * @description: 楼层统计查询
 * @date: 2020/12/14 8:48
 * @author: fushiping
 * @version: 1.0
 */
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class FloorStatisticsQueryReqDTO implements Serializable {
	private static final long serialVersionUID = 5169986485299868256L;

	@ApiModelProperty(value = "园区列表")
	private Integer parkId;

	@ApiModelProperty(value = "楼栋列表")
	private List<Integer> dormitoryId;

	@ApiModelProperty(value = "职层名")
	private String jcheId;

	@ApiModelProperty(value = "房间性别")
	private Integer roomSex;

	private List<Integer> roomType;
}
