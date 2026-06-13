package com.tce.smart.admin.api.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * @program: smart
 * @description:
 * @author: Wuling
 * @create: 2021-07-27 18:00
 **/

@Data
public class StaffPerfectReqDTO implements Serializable {

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
