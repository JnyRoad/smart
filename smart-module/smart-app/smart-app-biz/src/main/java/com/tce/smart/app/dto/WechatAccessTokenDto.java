package com.tce.smart.app.dto;

import lombok.Data;

/**
 * 网页授权信息
 *
 * @author mckaywu
 *
 */
@Data
public class WechatAccessTokenDto {
	/**
	 * 网页授权接口调用凭证
	 */
	private String accessToken;

	/**
	 * 凭证有效时长
	 */
	private long expiresIn;

	/**
	 * 用于刷新凭证
	 */
	private String refreshToken;

	/**
	 * 用户标识
	 */
	private String openId;

	/**
	 * 用户授权作用域
	 */
	private String scope;
}