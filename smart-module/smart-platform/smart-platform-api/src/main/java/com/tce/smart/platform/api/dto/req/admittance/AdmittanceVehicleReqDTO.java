package com.tce.smart.platform.api.dto.req.admittance;

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
public class AdmittanceVehicleReqDTO implements Serializable {

private static final long serialVersionUID = 1L;

	@ApiModelProperty("车牌号")
    private String plate;

	@ApiModelProperty("司机姓名")
    private String name;

	@ApiModelProperty("司机籍贯")
    private String nativePlace;

	@ApiModelProperty("驾驶证号")
    private String licenseNo;

	@ApiModelProperty("紧急联系人")
    private String emergencyName;

	@ApiModelProperty("紧急联系人联络方式")
    private String emergencyPhone;

	@ApiModelProperty("车型")
	private String modle;

	@ApiModelProperty("车辆类型")
	private Integer vehicleType;

	@ApiModelProperty("颜色")
    private Integer colour;

	@ApiModelProperty("相关证件图片")
    private String certImg;

	@ApiModelProperty("证件类型")
    private Integer certType;

}
