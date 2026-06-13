package com.tce.smart.platform.api.dto.resp;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * @Title: RoomBedRespDTO
 * @Auther: guohongtai
 * @Date: 2020-10-20 15:21
 */
@NoArgsConstructor
@Data
public class RoomBedRespDTO implements Serializable {
	private static final long serialVersionUID = -1;

	private Integer id;
	private Integer roomName;
	private Integer roomNum;
	private Integer bedTotal;
	private Integer roomSex;
	private Integer roomType;
	private Integer isDormitoryRoom;
	private Integer floorId;
	private Integer parkId;
	private Integer dormitoryId;

	private List<RoomBedRespDTO.Bed> beds;

	@Data
	public static class Bed{
		private Integer bedId;

		private Integer bedName;
	}
}
