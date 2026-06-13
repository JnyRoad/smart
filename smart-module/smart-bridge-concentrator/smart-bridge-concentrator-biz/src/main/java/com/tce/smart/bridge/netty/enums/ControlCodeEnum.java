package com.tce.smart.bridge.netty.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author Li.JiaJun
 * @since 2021/12/16 14:10
 */
@Getter
@AllArgsConstructor
public enum ControlCodeEnum {

	/**
	 * 控制码
	 */
	REGISTER_HEARTBEAT_RECEIVE("C9", "水电表注册|心跳接收"),

	REGISTER_HEARTBEAT_RESPONSE("0B", "水电表注册|心跳响应"),

	READING_QUERY_REQUEST("4B", "水电表集中器查询请求"),
	;

	private String code;

	private String desc;
}
