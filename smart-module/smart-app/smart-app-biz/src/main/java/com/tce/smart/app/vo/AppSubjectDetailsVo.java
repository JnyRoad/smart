package com.tce.smart.app.vo;

import lombok.Data;

@Data
public class AppSubjectDetailsVo {

	/**
	 * 主题名称
	 */
	private String subjectName;
	/**
	 * 文本名称
	 */
	private String textName;
	/**
	 * 文本内容
	 */
	private String textDesc;
	/**
	 * 图片二进制
	 */
	private byte[] picBinary;
}
