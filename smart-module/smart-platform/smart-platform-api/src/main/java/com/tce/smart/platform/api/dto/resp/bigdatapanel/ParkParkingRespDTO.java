package com.tce.smart.platform.api.dto.resp.bigdatapanel;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * @description: 大数据面板-车位动态实体类
 * @date: 2020-08-04 15:02
 * @author: wuling
 * @version: 1.0
 */
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class ParkParkingRespDTO implements Serializable {

	private static final long serialVersionUID = 2365869844320523264L;

	/**
	 * 车位数量
	 */
	@ApiModelProperty("车位数量")
	private Integer parkingCount;

	/**
	 * 空闲车位数量
	 */
	@ApiModelProperty("空闲车位数量")
	private Integer parkingFreeCount;

	/**
	 * 进出记录
	 */
	@ApiModelProperty("进出记录")
	private List<InOutRecord> inOutRecords;

	@Builder
	@AllArgsConstructor
	@NoArgsConstructor
	@Data
	public static class InOutRecord{

		/**
		 * 位置
		 */
		@ApiModelProperty("位置")
		private String areaName;

		/**
		 * 时间
		 */
		@ApiModelProperty("时间")
		@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
		private Date snapTime;

		/**
		 * 车牌号
		 */
		@ApiModelProperty("车牌号")
		private String vehiclePlate;

		/**
		 * 类型
		 */
		@ApiModelProperty("类型")
		private String typeDes;
	}
}
