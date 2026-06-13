package com.tce.smart.platform.emun.operateLog;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * @author Li.JiaJun
 * @since 2022/7/21 14:20
 */
@Getter
@AllArgsConstructor
public enum MeterOperateEnum {

	/**
	 * 操作日志操作动作：1、开；2、关
	 */
	OPEN(1, "开"),
	CLOSE(2, "关");

	private Integer action;
	private String desc;

	public static String desc(Integer action){
		return Arrays.stream(MeterOperateEnum.values()).filter(e -> e.getAction().equals(action)).findFirst().map(MeterOperateEnum::getDesc).orElse(null);
	}

	public static Integer action(String desc){
		return Arrays.stream(MeterOperateEnum.values()).filter(e -> e.getDesc().equals(desc)).findFirst().map(MeterOperateEnum::getAction).orElse(null);
	}
}
