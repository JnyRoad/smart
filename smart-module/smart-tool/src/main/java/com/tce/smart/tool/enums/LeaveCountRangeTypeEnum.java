package com.tce.smart.tool.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * @Author: Liu.jihong
 * @Date: 2020/9/27 16:12
 */
@AllArgsConstructor
@Getter
public enum LeaveCountRangeTypeEnum {

	BU(2,"BU"),

	ROOM(1,"房间");
	private Integer code;

	private String desc;

	public static LeaveCountRangeTypeEnum cameraEnum(Integer code){
		return Arrays.stream(LeaveCountRangeTypeEnum.values()).filter(e -> e.getCode().equals(code)).findFirst().orElse(null);
	}
	public static String desc(Integer code){
		return Arrays.stream(LeaveCountRangeTypeEnum.values()).filter(e -> e.getCode().equals(code)).findFirst().map(LeaveCountRangeTypeEnum::getDesc).orElse(null);
	}

	public static Integer code(String desc){
		return Arrays.stream(LeaveCountRangeTypeEnum.values()).filter(e -> e.getDesc().equals(desc)).findFirst().map(LeaveCountRangeTypeEnum::getCode).orElse(null);
	}
}
