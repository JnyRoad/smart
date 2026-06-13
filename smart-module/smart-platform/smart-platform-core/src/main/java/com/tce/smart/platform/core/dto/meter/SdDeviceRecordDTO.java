package com.tce.smart.platform.core.dto.meter;

import com.tce.smart.common.core.dto.BaseDTO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class SdDeviceRecordDTO extends BaseDTO {

	/**
	 * 设备记录ID
	 */
	private Long id;

	/**
	 * 集中器ID
	 */
	private Long concentratorId;

	/**
	 * 区域类型
	 */
	private Integer placeType;

	/**
	 * 水电类型
	 */
	private Integer sdType;

	/**
	 * 区域名称
	 */
	private String areaName;

	/**
	 * 设备名称
	 */
	private String deviceName;

	/**
	 * 地址
	 */
	private String address;


	/**
	 * TAG_name
	 */
	private String deviceTag;
}
