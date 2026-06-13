package com.tce.smart.platform.emun;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 *
 * @author sunfujian
 * @date 2021/4/26 14:08
 */
@Getter
@AllArgsConstructor
public enum RoomSexEnum {
	MAN(0,"男"),

	WOMAN(1,"女");

	private Integer code;

	private String desc;

	public static RoomSexEnum cameraEnum(Integer code){
		return Arrays.stream(RoomSexEnum.values()).filter(e -> e.getCode().equals(code)).findFirst().orElse(null);
	}
	public static String desc(Integer code){
		return Arrays.stream(RoomSexEnum.values()).filter(e -> e.getCode().equals(code)).findFirst().map(RoomSexEnum::getDesc).orElse(null);
	}

	public static Integer code(String desc){
		return Arrays.stream(RoomSexEnum.values()).filter(e -> e.getDesc().equals(desc)).findFirst().map(RoomSexEnum::getCode).orElse(null);
	}
}
