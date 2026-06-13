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
public enum LargeClassEnum {
	/**
	 * 0、冷水表；1、热水表；2、直饮水水表；3、中水水表；4、大口径水表
	 */
	COLD(0,"冷水水表"),

	HOT(1,"热水水表"),

	DIRECT(2, "直饮水水表"),

	MIDDLE(3, "中水水表"),

	LARGE(4, "大口径水表");

	private Integer code;

	private String desc;

	public static String desc(Integer code){
		return Arrays.stream(LargeClassEnum.values()).filter(e -> e.getCode().equals(code)).findFirst().map(LargeClassEnum::getDesc).orElse(null);
	}

	public static Integer code(String desc){
		return Arrays.stream(LargeClassEnum.values()).filter(e -> e.getDesc().equals(desc)).findFirst().map(LargeClassEnum::getCode).orElse(null);
	}
}
