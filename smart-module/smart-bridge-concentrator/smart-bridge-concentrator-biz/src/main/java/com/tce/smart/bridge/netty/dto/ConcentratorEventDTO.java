package com.tce.smart.bridge.netty.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * @author Li.JiaJun
 * @since 2021/12/17 16:28
 */
@Data
public class ConcentratorEventDTO implements Serializable {

	private static final long serialVersionUID = -1L;

	/**
	 * IP:port =>ip地址:端口号
	 */
	private String clientId;
	/**
	 * 报文帧
	 */
	private String messageFrame;
	/**
	 * 时间标签tp
	 */
	private String tp;
}
