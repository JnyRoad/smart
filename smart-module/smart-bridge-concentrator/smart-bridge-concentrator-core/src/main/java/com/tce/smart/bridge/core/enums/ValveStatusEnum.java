package com.tce.smart.bridge.core.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * @author: Li.JiaJun
 * @since: 2021/8/20 11:02
 */
@AllArgsConstructor
@Getter
public enum ValveStatusEnum {
	/**
	 * 0-关闭，1-开启，2-未关联阀门
	 */
	CLOSE(0,"关闭"),

	OPEN(1,"开启"),

	NO_RELATION(2, "未关联阀门");

	private Integer code;

	private String desc;

	public static ValveStatusEnum cameraEnum(Integer code){
		return Arrays.stream(ValveStatusEnum.values()).filter(e -> e.getCode().equals(code)).findFirst().orElse(null);
	}
	public static String desc(Integer code){
		return Arrays.stream(ValveStatusEnum.values()).filter(e -> e.getCode().equals(code)).findFirst().map(ValveStatusEnum::getDesc).orElse(null);
	}

	public static Integer code(String desc){
		return Arrays.stream(ValveStatusEnum.values()).filter(e -> e.getDesc().equals(desc)).findFirst().map(ValveStatusEnum::getCode).orElse(null);
	}
}
