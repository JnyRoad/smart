package com.tce.smart.app.vo.fore;

import lombok.Data;

/**
 * 员工车辆信息结果
 * @author qipei
 *
 */
@Data
public class VehicleVo {


	/**
	 * 车牌号
	 */
	private String plateNumber;

	/**
	 *  车辆类型描述
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

}
