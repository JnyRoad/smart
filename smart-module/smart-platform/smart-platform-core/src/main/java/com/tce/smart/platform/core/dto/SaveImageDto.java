package com.tce.smart.platform.core.dto;

import lombok.Data;

import java.io.Serializable;

/***
 * description: 图片存储Dto <br>
 * date: 2019/12/11 9:23 <br>
 * author: mckaywu <br>
 * version: 1.0 <br>
 */
@Data
public class SaveImageDto implements Serializable {
	private static final long serialVersionUID = -3920007649945647782L;

	/**
	 * 园区ID
	 */
	private Integer parkId;
	/**
	 * 图片编码
	 */
	private String imageCode;

	/**
	 * 图片Base64字符串
	 */
	private String base64String;
	/**
	 * 图片类型
	 */
	private Integer imageType;
}
