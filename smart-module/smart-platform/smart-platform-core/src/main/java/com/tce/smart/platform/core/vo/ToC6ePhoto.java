package com.tce.smart.platform.core.vo;

import lombok.Data;

/**
 * c6提供查询员工图片参数
 * @author QIPEI
 *
 */
@Data
public class ToC6ePhoto {

	/**
	 * 员工号
	 */
	private String empNo;

	/**
	 * 员工base64
	 */
	private String photo;
}
