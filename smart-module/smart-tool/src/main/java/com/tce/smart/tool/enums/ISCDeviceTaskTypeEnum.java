package com.tce.smart.tool.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author sunfujian
 * @date 2021/8/25 9:28
 */
@Getter
@AllArgsConstructor
public enum ISCDeviceTaskTypeEnum {
	PERSON(1, "人员"),
	FACE(2, "人脸"),
	ACCESS(3, "人员权限"),
	VEHICLE(4, "车辆");

	private Integer code;
	private String desc;
}
