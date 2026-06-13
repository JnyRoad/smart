package com.tce.smart.platform.emun;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * @Author: Liu.jihong
 * @Date: 2020/9/27 16:12
 */
@AllArgsConstructor
@Getter
public enum StatusEmun {
	/**
	 * 0-初始化，1-发送成功，2-发送失败
	 */
	INITIALIZE(0,"初始化"),

	SUCCESS(1,"发送成功"),

	FAIL(2,"发送失败");
	private Integer code;

	private String desc;

	public static StatusEmun cameraEnum(Integer code){
		return Arrays.stream(StatusEmun.values()).filter(e -> e.getCode().equals(code)).findFirst().orElse(null);
	}
	public static String desc(Integer code){
		return Arrays.stream(StatusEmun.values()).filter(e -> e.getCode().equals(code)).findFirst().map(StatusEmun::getDesc).orElse(null);
	}

	public static Integer code(String desc){
		return Arrays.stream(StatusEmun.values()).filter(e -> e.getDesc().equals(desc)).findFirst().map(StatusEmun::getCode).orElse(null);
	}
}
