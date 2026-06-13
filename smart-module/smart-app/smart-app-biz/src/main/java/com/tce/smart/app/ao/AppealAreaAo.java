package com.tce.smart.app.ao;

import lombok.Data;

/**
 * @description: AppealAreaAo
 * @date: 2020-07-28 11:39
 * @author: wuling
 * @version: 1.0
 */
@Data
public class AppealAreaAo {

	/**
	 * 园区Id
	 */
	private Integer parkId;

	/**
	 * 文章标题
	 */
	private String subjectName;

	/**
	 * 发布类型
	 */
	private String publishFlag;

	/**
	 * 类别编号
	 */
	private String catalogCode;
}
