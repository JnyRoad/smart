package com.tce.smart.platform.emun;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * @author: Li.JiaJun
 * @since: 2021/8/19 20:14
 */
@AllArgsConstructor
@Getter
public enum MeterStatusEnum {
	/**
	 * 0-未连接,1-离线，2-在线
	 */
	UNCONNECTED(0,"未连接"),

	OUTLINE(1,"离线"),

	ONLINE(2,"在线");
	private Integer code;

	private String desc;

	public static MeterStatusEnum cameraEnum(Integer code){
		return Arrays.stream(MeterStatusEnum.values()).filter(e -> e.getCode().equals(code)).findFirst().orElse(null);
	}
	public static String desc(Integer code){
		return Arrays.stream(MeterStatusEnum.values()).filter(e -> e.getCode().equals(code)).findFirst().map(MeterStatusEnum::getDesc).orElse(null);
	}

	public static Integer code(String desc){
		return Arrays.stream(MeterStatusEnum.values()).filter(e -> e.getDesc().equals(desc)).findFirst().map(MeterStatusEnum::getCode).orElse(null);
	}
}
