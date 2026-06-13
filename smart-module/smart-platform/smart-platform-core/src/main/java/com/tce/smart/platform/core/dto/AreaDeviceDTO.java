package com.tce.smart.platform.core.dto;

import lombok.Data;

/**
 * @description: 区域-设备信息
 * @date: 2020-08-06 9:52
 * @author: wuling
 * @version: 1.0
 */
@Data
public class AreaDeviceDTO {
	/**
	 * 区域编号
	 */
	private Integer id;

	/**
	 * 区域名称
	 */
	private String areaName;

	/**
	 * 设备Id
	 */
	private String deviceId;
}
