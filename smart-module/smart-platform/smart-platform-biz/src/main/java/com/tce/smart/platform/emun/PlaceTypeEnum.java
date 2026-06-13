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
public enum PlaceTypeEnum {
	/**
	 * 区域类型：0、宿舍；1、厂区
	 */
	DORMITORY(0,"宿舍"),

	FACTORY(1,"厂区");

	private Integer code;

	private String desc;

	public static String desc(Integer code){
		return Arrays.stream(PlaceTypeEnum.values()).filter(e -> e.getCode().equals(code)).findFirst().map(PlaceTypeEnum::getDesc).orElse(null);
	}

	public static Integer code(String desc){
		return Arrays.stream(PlaceTypeEnum.values()).filter(e -> e.getDesc().equals(desc)).findFirst().map(PlaceTypeEnum::getCode).orElse(null);
	}
}
