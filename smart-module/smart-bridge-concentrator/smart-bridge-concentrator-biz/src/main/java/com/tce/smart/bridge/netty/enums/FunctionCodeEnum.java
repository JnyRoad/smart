package com.tce.smart.bridge.netty.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author Li.JiaJun
 * @since 2021/12/16 14:11
 */
@Getter
@AllArgsConstructor
public enum FunctionCodeEnum {
	/**
	 * 功能码
	 */
	REGISTER_HEARTBEAT_RECEIVE("02", "水表注册|心跳接收"),

	REGISTER_HEARTBEAT_RESPONSE("00", "水电表注册|心跳响应"),

	DOWNLOAD_FILE_RESPONSE("00", "水电表集中器下载|删除档案响应"),
	;

	private String code;

	private String desc;
}
