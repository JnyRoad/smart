package com.tce.smart.platform.core.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

/**
 * app申请添加车辆
 * @author qipei
 *
 */
@Data
public class AddVehicleDTO {

	@NotBlank(message = "园区不能为空")
	private String parkId;
	/**
	 * 车牌号
	 */
	@NotBlank(message = "车牌号不能为空")
	@Pattern(regexp = "^(([京津沪渝冀豫云辽黑湘皖鲁新苏浙赣鄂桂甘晋蒙陕吉闽贵粤青藏川宁琼使领][A-Z](([0-9]{5}[DF])|([DF]([A-HJ-NP-Z0-9])[0-9]{4})))|([京津沪渝冀豫云辽黑湘皖鲁新苏浙赣鄂桂甘晋蒙陕吉闽贵粤青藏川宁琼使领][A-Z][A-HJ-NP-Z0-9]{4}[A-HJ-NP-Z0-9挂学警港澳使领]))$",message = "车牌号格式不正确")
	private String  plateNumber;

	/**
	 * 车辆类型
	 */
	@NotNull(message = "车辆类型不能为空")
	private  String vehicleType;

	/**
	 * 车辆品牌
	 */
	@NotBlank(message = "车辆品牌不能为空")
	private String vehicleBrand;

	/**
	 * 车辆颜色描述
	 */
	@NotNull(message = "车辆颜色不能为空")
	private String vehicleColor;

	/**
	 * 驾驶证（base64字符串）
	 */
	@NotBlank(message = "驾驶证图片不能为空")
	private String drivingLicence;

	/**
	 * 汽车行驶证（base64字符串）
	 */
	@NotBlank(message = "行驶证图片不能为空")
	private String carDrivingLicence;

	private String badge;



}
