package com.tce.smart.app.vo.fore;

import lombok.Data;

/**
 * 车辆通行权限详情
 * @author qipei
 *
 */
@Data
public class AuthDetailVo {

	/**
	 * 车牌号
	 */
	private String plateNumber;

	/**
	 * 车辆类型描述
	 */
	private String vehicleTypeDesc;

	/**
	 * 车辆品牌
	 */
	private String vehicleBrand;

	/**
	 * 车辆颜色描述
	 */
	private String vehicleColorDesc;

	/**
	 * 驾驶证（base64字符串）
	 */
	private String drivingLicence;

	/**
	 * 汽车行驶证（base64字符串）
	 */
	private String carDrivingLicence;

	private String reason;
}
