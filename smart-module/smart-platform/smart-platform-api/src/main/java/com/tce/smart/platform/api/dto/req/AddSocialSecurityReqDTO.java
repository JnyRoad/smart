package com.tce.smart.platform.api.dto.req;

import lombok.Data;

/**
 * 添加社保请求
 * @author 齐佩
 *
 */
@Data
public class AddSocialSecurityReqDTO {



	private Integer id;
	/**
	 * 标题
	 */
	private String title;

	/**
	 * url地址
	 */
	private String url;

	/**
	 * 图片base64
	 */
	private String image;

}
