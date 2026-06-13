package com.tce.smart.platform.api.dto.resp.dormitorymange;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @description: 宿舍楼层统计
 * @date: 2020/9/29 8:48
 * @author: fushiping
 * @version: 1.0
 */
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class DormitoryStatisticsRespDTO implements Serializable {
	private static final long serialVersionUID = 1470561687953010349L;

	@ApiModelProperty(value = "园区Id")
	private Integer parkId;

	@ApiModelProperty(value = "房间性别类型 0-男，1-女，2-夫妻，3-其他")
	private Integer roomSex;

	@ApiModelProperty(value = "房间性别类型Desc")
	private String roomSexDesc;

	@ApiModelProperty(value = "宿舍名称")
	private String dormitoryDesc;

	@ApiModelProperty(value = "宿舍分类")
	private String roomTypeDesc;

	@ApiModelProperty(value = "可用房间")
	private Integer roomNum;

	@ApiModelProperty(value = "标配人数")
	private Integer typeBedTotal;

	@ApiModelProperty(value = "可住人数")
	private Integer roomBedTotal;

	@ApiModelProperty(value = "已住人数")
	private Integer alreadyUse;

	@ApiModelProperty(value = "空床位")
	private Integer freeBedNum;

	@ApiModelProperty(value = "单独空房间")
	private Integer freeRoomNum;

	@ApiModelProperty(value = "标配床位")
	private Integer standardBedNum;

	@ApiModelProperty(value = "锁定床位")
	private Integer lockBedNum;
}
