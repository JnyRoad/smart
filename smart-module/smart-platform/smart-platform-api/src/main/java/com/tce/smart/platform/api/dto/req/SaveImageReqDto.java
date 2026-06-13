package com.tce.smart.platform.api.dto.req;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @description: SaveImageReqDto
 * @date: 2020-07-07 11:49
 * @author: wuling
 * @version: 1.0
 */
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class SaveImageReqDto implements Serializable {
	private static final long serialVersionUID = 7735503247724624395L;

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
