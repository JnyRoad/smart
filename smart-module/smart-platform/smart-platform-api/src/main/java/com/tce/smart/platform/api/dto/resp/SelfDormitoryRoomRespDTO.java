package com.tce.smart.platform.api.dto.resp;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 当前认证员工可读取的最小住宿位置投影。
 *
 * 不包含工号、姓名、部门、门锁动态码或任何门锁详情；动态码只能从专用本人门锁接口取得。
 */
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class SelfDormitoryRoomRespDTO implements Serializable {
	private static final long serialVersionUID = 731654462734198342L;

	@ApiModelProperty("床位编号")
	private Integer id;

	@ApiModelProperty("床位名称")
	private String bedNumber;

	@ApiModelProperty("园区ID")
	private Integer parkId;

	@ApiModelProperty("园区名称")
	private String parkName;

	@ApiModelProperty("楼栋Id")
	private Integer dormitoryId;

	@ApiModelProperty("楼栋名称")
	private String dormitoryName;

	@ApiModelProperty("楼层Id")
	private Integer floorId;

	@ApiModelProperty("楼层名称")
	private String floorName;

	@ApiModelProperty("房间Id")
	private Integer roomId;

	@ApiModelProperty("房间名称")
	private String roomName;

	@ApiModelProperty("入住记录ID")
	private Integer inRecordId;
}
