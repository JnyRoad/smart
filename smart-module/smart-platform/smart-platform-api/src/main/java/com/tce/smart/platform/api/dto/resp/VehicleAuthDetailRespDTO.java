package com.tce.smart.platform.api.dto.resp;

import lombok.Data;

/**
 * App 车辆入园申请的受控详情。
 *
 * 仅承载 App 展示已授权车辆证照所需字段，禁止复用包含园区、归属、状态等管理信息的通用车辆对象。
 */
@Data
public class VehicleAuthDetailRespDTO {

	/** 车牌号。 */
	private String vehiclePlate;

	/** 车辆品牌。 */
	private String vehicleBrand;

	/** 车辆颜色。 */
	private Integer vehicleColor;

	/** 车辆类型。 */
	private Integer vehicleType;

	/** 已完成授权后返回的驾驶证 Base64 内容。 */
	private String driverLicenseBase64;

	/** 已完成授权后返回的行驶证 Base64 内容。 */
	private String drivingLicenseBase64;

	/** 入园申请原因。 */
	private String reason;
}
