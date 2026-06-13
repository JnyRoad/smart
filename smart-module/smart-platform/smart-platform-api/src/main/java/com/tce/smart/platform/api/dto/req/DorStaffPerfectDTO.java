package com.tce.smart.platform.api.dto.req;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 宿舍人脸比对获取动态码
 *
 * @author mckaywu
 * @date 2019-06-02 17:08:43
 */
@Data
public class DorStaffPerfectDTO {

	/**
	 * 员工工号
	 */
	@NotBlank(message = "工号不可为空")
	private String badge;

	/**
	 * 人脸照片Base64字符串
	 */
	@NotBlank(message = "人脸图不可为空")
	private String facePic;

	/**
	 * 设备编号
	 */
	private String deviceNo;

}
