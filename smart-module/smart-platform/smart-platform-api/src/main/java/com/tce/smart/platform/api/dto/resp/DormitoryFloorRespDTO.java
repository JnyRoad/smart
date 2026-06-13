package com.tce.smart.platform.api.dto.resp;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * @Title: DormitoryFloorRespDTO
 * @Auther: guohongtai
 * @Date: 2020-10-20 15:07
 */
@NoArgsConstructor
@Data
public class DormitoryFloorRespDTO implements Serializable {
	private static final long serialVersionUID = -1;

	private Integer id;

	private String dormitoryName;

	private Integer floorNum;

	private Integer parkId;

	private List<Floor> floors;

	@Data
	public static class Floor{
		private Integer floorId;

		private String floorName;
	}
}
