package com.tce.smart.platform.core.dto;

import com.tce.smart.platform.core.entity.*;
import lombok.Data;

import java.util.List;

import javax.validation.constraints.NotBlank;

/**
 * 添加或修改应聘消息
 */
@Data
public class AddOrUpApplicationDTO     {

	private SmtApplication smtApplication;

	private String createUserName;


	/**
	 * 证件照片base64
	 */
	@NotBlank(message = "证件照片不能为空")
	private String certnoPicture;


	/**
	 * 人脸照片base64
	 */
	@NotBlank(message = "人脸照片不能为空")
	private String facePicture;


}
