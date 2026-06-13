package com.tce.smart.platform.core.vo;

import lombok.Data;

@Data
public class GetDeviceVO {

	private String areaName;

	private Integer channelNo;

	private Integer areaId;
	/**
	 * 设备名称
	 */
	private String deviceName;


}
