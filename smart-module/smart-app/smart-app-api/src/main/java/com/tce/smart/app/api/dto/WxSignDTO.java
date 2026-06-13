package com.tce.smart.app.api.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 微信签名
 * @author sunfujian
 * @since 2021/9/17 16:24
 */
@Data
public class WxSignDTO implements Serializable {
	private static final long serialVersionUID = -7081658217000293311L;

	/**
	 * 微信APPID
	 */
	private String appId;
	/**
	 * 随机字符串
	 */
	private String nonceStr;

	/**
	 * 时间戳
	 */
	private Long timestamp;

	/**
	 * 签名数据
	 */
	private String signature;
}
