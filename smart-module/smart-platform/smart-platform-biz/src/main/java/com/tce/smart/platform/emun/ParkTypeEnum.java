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
public enum ParkTypeEnum {
	/**
	 * 园区类型
	 * 0、石岩；1、龙岗；2、大岭山；3、许昌；4、合肥；5、塘厦
	 */

	PARK_0("0", "石岩"),
	PARK_1("1", "龙岗"),
	PARK_2("2", "大岭山"),
	PARK_3("3", "许昌"),
	PARK_4("4", "合肥"),
	PARK_5("5", "塘厦");

	private String code;

	private String desc;

	public static String desc(String code){
		return Arrays.stream(ParkTypeEnum.values()).filter(e -> e.getCode().equals(code)).findFirst().map(ParkTypeEnum::getDesc).orElse(null);
	}

	public static String code(String desc){
		return Arrays.stream(ParkTypeEnum.values()).filter(e -> desc.contains(e.getDesc())).findFirst().map(ParkTypeEnum::getCode).orElse(null);
	}
}
