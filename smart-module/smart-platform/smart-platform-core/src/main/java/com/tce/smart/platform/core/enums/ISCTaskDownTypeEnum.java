package com.tce.smart.platform.core.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author sunfujian
 * @date 2021/8/26 13:48
 */
@Getter
@AllArgsConstructor
public enum ISCTaskDownTypeEnum {
	TYPE_1(1, "卡片"),
	TYPE_2(2, "指纹"),
	TYPE_3(3, "卡片+指纹（组合）"),
	TYPE_4(4, "人脸"),
	TYPE_5(5, "卡片+人脸（组合）"),
	TYPE_6(6, "人脸+指纹（组合）"),
	TYPE_7(7, "卡片+指纹+人脸（组合）");

	private Integer code;
	private String desc;
}
