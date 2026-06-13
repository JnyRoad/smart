package com.tce.smart.platform.api.dto.req.admittance;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * 入厂申请预约随行人员表
 *
 * @author fushiping
 * @date 2021-08-17 17:45:13
 */
@Data
public class AdmittanceFellowReqDTO implements Serializable {

private static final long serialVersionUID = 1L;

	@ApiModelProperty("姓名")
    private String fellowName;

	@ApiModelProperty("照片")
    private String fellowPhotoId;

	@ApiModelProperty("证件号")
    private String certNo;

	@ApiModelProperty("证件类型")
    private Integer certType;

	@ApiModelProperty("籍贯")
	private String nativePlace;

	@ApiModelProperty("访客身份证正面照片")
	private String frontPhotoId;

	@ApiModelProperty("是否为主访客")
	private Integer isMain;

}
