package com.tce.smart.app.dto;

import lombok.Data;

@Data
public class AppPictureDto {
	/**
	 * 该图片ID
	 */
	private Integer id;
	/**
	 * 图片名称
	 */
	private String picName;
	/**
	 * 图片内容
	 */
	private String picBinary;
}
