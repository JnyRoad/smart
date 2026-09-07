package com.tce.smart.platform.api.dto.resp.admittance;

import com.fasterxml.jackson.annotation.JsonFormat;
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
public class AdmittanceFellowRespDTO implements Serializable {
private static final long serialVersionUID = 1L;

	/**
	 * ID
	 */
	@ApiModelProperty("ID")
	@JsonFormat(shape= JsonFormat.Shape.STRING)
	private Long id;

	/**
	 * 使用人员记录ID生成的厂牌二维码PNG Base64
	 */
	@ApiModelProperty("人员记录ID厂牌二维码PNG Base64")
	private String recordQrCode;

	/**
	 * 预约ID
	 */
	@ApiModelProperty("预约ID")
	private Long visitorId;
	/**
	 * 姓名
	 */
	@ApiModelProperty("姓名")
	private String fellowName;
	/**
	 * 照片
	 */
	@ApiModelProperty("照片")
	private String fellowPhotoId;

	/**
	 * 照片
	 */
	@ApiModelProperty("照片")
	private String fellowPhotoIdUrl;

	@ApiModelProperty("人脸图片二进制")
	private byte[] fellowPhotoByte;
	/**
	 *
	 */
	@ApiModelProperty("证件号")
	private String certNo;
	/**
	 * 证件号
	 */
	@ApiModelProperty("证件类型")
	private Integer certType;

	/**
	 * 证件号
	 */
	@ApiModelProperty("证件类型")
	private String certTypeDesc;

	/**
	 * 访客身份证正面照片
	 */
	private String frontPhotoId;

	/**
	 * 籍贯
	 */
	private String nativePlace;

	/**
	 * 性别
	 */
	private String gender;


	/**
	 * 是否为主访客
	 */
	private Integer isMain;

}
