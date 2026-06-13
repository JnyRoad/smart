package com.tce.smart.bridge.netty.dto;

import lombok.Data;

/**
 * 水表读数
 * @author Li.JiaJun
 * @since 2021/12/17 16:36
 */
@Data
public class EleReadingDTO {
	/**
	 * 采集时间
	 */
	private String collectTime;
	/**
	 * 当前读数
	 */
	private String currentReading;
	/**
	 * 设备状态：0、离线；1、在线
	 */
	private Integer isOnline;
}
