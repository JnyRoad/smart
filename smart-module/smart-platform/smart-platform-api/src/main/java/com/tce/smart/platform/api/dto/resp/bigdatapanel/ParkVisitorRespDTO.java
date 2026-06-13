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
 * @description: 大数据面板-访客动态实体类
 * @date: 2020-08-04 17:02
 * @author: wuling
 * @version: 1.0
 */
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class ParkVisitorRespDTO implements Serializable {

	private static final long serialVersionUID = -4039940513551541527L;

	/**
	 * 今天进厂人数
	 */
	@ApiModelProperty("今天进厂人数")
	private Integer inCount;

	/**
	 * 今日出厂人数
	 */
	@ApiModelProperty("今日出厂人数")
	private Integer outCount;

	/**
     * 各位置进出统计
	 */
	@ApiModelProperty("各位置进出统计")
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
		 * 进入数量
		 */
		@ApiModelProperty("进入数量")
		private Integer inCount;

		/**
		 * 离开数量
		 */
		@ApiModelProperty("离开数量")
		private Integer outCount;
	}
}
