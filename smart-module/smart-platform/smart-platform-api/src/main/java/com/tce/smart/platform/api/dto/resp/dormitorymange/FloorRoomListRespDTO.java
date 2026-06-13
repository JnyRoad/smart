package com.tce.smart.platform.api.dto.resp.dormitorymange;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.tce.smart.platform.api.dto.resp.staffmange.StaffRespDTO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * @description: 楼层-房间对应列表
 * @date: 2020/9/29 8:48
 * @author: wuling
 * @version: 1.0
 */
@Data
public class FloorRoomListRespDTO implements Serializable {
	private static final long serialVersionUID = -6101787450080822957L;

	@ApiModelProperty(value = "楼层ID")
	private Integer floorId;

	@ApiModelProperty(value = "楼层名称")
	private String floorName;

	@ApiModelProperty(value = "房间列表")
	private List<Room> roomList;

	/**
	 * 房间列表
	 */
	@Data
	public static class Room{
		@ApiModelProperty(value = "房间标识ID")
		private Integer roomId;

		@ApiModelProperty(value = "房间编号")
		private Integer roomName;
	}

}
