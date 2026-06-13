package com.tce.smart.platform.emun;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * @author: Li.JiaJun
 * @since: 2021/8/20 11:02
 */
@AllArgsConstructor
@Getter
public enum EleDownChannelEnum {
	/**
	 * 电表
	 * 1， 2， 3， 4
	 */

	ONE(1, "1"),

	TWO(2, "2"),

	THREE(3, "3"),

	FOUR(4, "4");

	private Integer code;

	private String desc;

	public static String desc(Integer code){
		return Arrays.stream(EleDownChannelEnum.values()).filter(e -> e.getCode().equals(code)).findFirst().map(EleDownChannelEnum::getDesc).orElse(null);
	}

	public static Integer code(String desc){
		return Arrays.stream(EleDownChannelEnum.values()).filter(e -> e.getDesc().equals(desc)).findFirst().map(EleDownChannelEnum::getCode).orElse(null);
	}
}
