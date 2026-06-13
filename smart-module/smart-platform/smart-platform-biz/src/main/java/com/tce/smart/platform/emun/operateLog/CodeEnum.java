package com.tce.smart.platform.emun.operateLog;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * @author Li.JiaJun
 * @since 2022/7/21 14:18
 */
@Getter
@AllArgsConstructor
public enum CodeEnum {
	/**
	 * 操作日志功能类型：1、水电表开关操作
	 */
	METER(1, "水电表开关操作"),
	METER_REMOTE(2, "水电表远程本地开关操作");

	private Integer code;
	private String desc;

	public static String desc(Integer code){
		return Arrays.stream(CodeEnum.values()).filter(e -> e.getCode().equals(code)).findFirst().map(CodeEnum::getDesc).orElse(null);
	}

	public static Integer code(String desc){
		return Arrays.stream(CodeEnum.values()).filter(e -> e.getDesc().equals(desc)).findFirst().map(CodeEnum::getCode).orElse(null);
	}
}
