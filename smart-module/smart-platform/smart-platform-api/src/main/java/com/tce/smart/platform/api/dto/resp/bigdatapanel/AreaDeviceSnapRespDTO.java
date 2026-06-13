package com.tce.smart.platform.api.dto.resp.bigdatapanel;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * @description: 区域设备抓拍数据
 * @date: 2020-08-06 9:56
 * @author: wuling
 * @version: 1.0
 */
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class AreaDeviceSnapRespDTO implements Serializable {
	private static final long serialVersionUID = 8716990029918264499L;

	/**
	 * 区域名称
	 */
	@ApiModelProperty("区域名称")
	private String areaName;

	/**
	 * 抓拍数据列表
	 */
	@ApiModelProperty("抓拍数据列表")
	private List<SnapData> snapDataList;


	/**
	 * 抓拍数据-已设备区分
	 */
	@Builder
	@AllArgsConstructor
	@NoArgsConstructor
	@Data
	public static class SnapData{

		/**
		 * 设备id
		 */
		@ApiModelProperty("设备id")
		private String deviceId;

		/**
		 * 设备名称
		 */
		@ApiModelProperty("设备名称")
		private String deviceName;

		/**
		 * 人员姓名
		 */
		@ApiModelProperty("人员姓名")
		private String personName;

		/**
		 * 人员图片的url
		 */
		@ApiModelProperty("人员图片的url")
		private String personUrl;

		/**
		 * 抓拍图片的url
		 */
		@ApiModelProperty("抓拍图片的url")
		private String snapPhotoUrl;

		/**
		 * 单位名称
		 */
		@ApiModelProperty("单位名称")
		private String company;

		/**
		 * 身份
		 */
		@ApiModelProperty("身份")
		private Integer personType;

		/**
		 * 身份描述
		 */
		@ApiModelProperty("身份描述")
		private String personTypeDesc;

		/**
		 * 进出门类型
		 */
		@ApiModelProperty("进出门类型")
		private Integer eventType;

		/**
		 * 进厂描述
		 */
		@ApiModelProperty("进厂描述")
		private String eventTypeDesc;

		/**
		 * 抓拍时间
		 */
		@ApiModelProperty("抓拍时间")
		private Date snapTime;
	}
}
