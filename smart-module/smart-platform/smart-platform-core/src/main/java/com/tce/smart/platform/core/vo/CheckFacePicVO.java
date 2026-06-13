package com.tce.smart.platform.core.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 员工批量上传图片，审核结果
 * @author QIPEI
 *
 */
@Data
public class CheckFacePicVO {

	@ApiModelProperty("员工id")
	private String staffId;

	@ApiModelProperty("员工工号")
	private String staffBadge;

	@ApiModelProperty("员工姓名")
	private String staffName;

	/**
	 * 人脸图片是否存在
	 * 0-有人脸图 1-无人脸图  3-员工号错误
	 */
	@ApiModelProperty("下发状态")
	private Integer status;

	@ApiModelProperty("人脸图")
	private String facePic;


}
