package com.tce.smart.platform.api.dto.resp.admittance;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * 入厂申请预约车辆表
 *
 * @author fushiping
 * @date 2021-08-17 17:45:05
 */
@Data
public class AdmittanceVehicleRespDTO implements Serializable {
private static final long serialVersionUID = 1L;

    /**
   * ID
   */
	@ApiModelProperty("ID")
	@JsonFormat(shape= JsonFormat.Shape.STRING)
    private Long id;

	/**
	 * 预约ID
	 */
	@ApiModelProperty("预约ID")
	private Long visitorId;
	/**
	 * 车牌号
	 */
	@ApiModelProperty("车牌号")
	private String plate;
	/**
	 * 司机姓名
	 */
	@ApiModelProperty("司机姓名")
	private String name;
	/**
	 * 司机籍贯
	 */
	@ApiModelProperty("司机籍贯")
	private String nativePlace;
	/**
	 * 驾驶证号
	 */
	@ApiModelProperty("驾驶证号")
	private String licenseNo;
	/**
	 * 紧急联系人
	 */
	@ApiModelProperty("紧急联系人")
	private String emergencyName;
	/**
	 * 紧急联系人联络方式
	 */
	@ApiModelProperty("紧急联系人联络方式")
	private String emergencyPhone;
	/**
	 * 车型
	 */
	@ApiModelProperty("车型")
	private String modle;
	/**
	 * 颜色
	 */
	@ApiModelProperty("颜色")
	private Integer colour;

	/**
	 * 颜色
	 */
	@ApiModelProperty("颜色")
	private String colourDesc;
	/**
	 * 相关证件图片
	 */
	@ApiModelProperty("相关证件图片")
	private String certImg;

	/**
	 * 相关证件图片
	 */
	@ApiModelProperty("相关证件图片")
	private String certImgUrl;
	/**
	 * 证件类型
	 */
	@ApiModelProperty("证件类型")
	private Integer certType;

	/**
	 * 证件类型
	 */
	@ApiModelProperty("证件类型")
	private String certTypeDesc;

	/**
	 * 车辆类型
	 */
	@ApiModelProperty("车辆类型")
	private Integer vehicleType;

	/**
	 * 车辆类型
	 */
	@ApiModelProperty("车辆类型")
	private String vehicleTypeDesc;
}
