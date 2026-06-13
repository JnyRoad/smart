package com.tce.smart.platform.core.dto;

import lombok.Data;

/**
 * 完善员工信息
 *
 * @author mckaywu
 * @date 2019-06-02 17:08:43
 */
@Data
public class StaffPerfectDTO {

	/**
	 * 员工工号
	 */
	private String badge;

	/**
	 * 证件照片Base64字符串
	 */
	private String certnoPic;

	/**
	 * 人脸照片Base64字符串
	 */
	private String facePic;

	/**
	 * 设备编号
	 */
	private String deviceNo;

}
