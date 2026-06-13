package com.tce.smart.platform.core.vo;

import java.util.List;

import lombok.Data;

@Data
public class RoomVisualVO {


	private Integer floorId;

	private Integer floorName;

	private List<RoomInfoVisualVO> roomInfoList;

}
