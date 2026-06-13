package com.tce.smart.platform.api.dto.resp;

import lombok.Data;

/**
 * 查看社保详情
 * @author 齐佩
 *
 */
@Data
public class SearchSocialSecurityRespDTO {



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
