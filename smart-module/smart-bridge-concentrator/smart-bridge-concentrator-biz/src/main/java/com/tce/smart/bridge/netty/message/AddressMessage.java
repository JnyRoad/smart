package com.tce.smart.bridge.netty.message;

import lombok.Data;

/**
 * 地址域
 * @author Li.JiaJun
 * @since 2021/12/16 10:15
 */
@Data
public class AddressMessage {

	/**
	 * 区县码
	 */
	private String countyCode;
	/**
	 * 地址
	 */
	private String address;
	/**
	 * 备用地址
	 */
	private String standbyAddress;
}
