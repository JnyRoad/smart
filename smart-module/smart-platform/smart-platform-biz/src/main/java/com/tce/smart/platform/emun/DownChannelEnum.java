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
public enum DownChannelEnum {
	/**
	 * 水表
	 * 2、M-BUS-1；3、M-BUS-2；4、M-BUS-3；5、M-BUS-4
	 */

	M_BUS_1(2, "M-BUS-1"),

	M_BUS_2(3, "M-BUS-2"),

	M_BUS_3(4, "M-BUS-3"),

	M_BUS_4(5, "M-BUS-4");

	private Integer code;

	private String desc;

	public static String desc(Integer code){
		return Arrays.stream(DownChannelEnum.values()).filter(e -> e.getCode().equals(code)).findFirst().map(DownChannelEnum::getDesc).orElse(null);
	}

	public static Integer code(String desc){
		return Arrays.stream(DownChannelEnum.values()).filter(e -> e.getDesc().equals(desc)).findFirst().map(DownChannelEnum::getCode).orElse(null);
	}
}
