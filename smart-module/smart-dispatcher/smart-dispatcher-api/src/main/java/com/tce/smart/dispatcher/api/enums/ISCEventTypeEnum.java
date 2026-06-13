package com.tce.smart.dispatcher.api.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author sunfujian
 * @date 2021/8/29 15:15
 */
@Getter
@AllArgsConstructor
public enum ISCEventTypeEnum {
	TYPE_1(196893, EventEnum.PERSON_ACCESS_RECORD, "人脸认证通过"),
	TYPE_2(197151, EventEnum.PERSON_ACCESS_RECORD, "人脸认证失败"),
	TYPE_3(198913, EventEnum.PERSON_ACCESS_RECORD, "正常开门"),
	TYPE_4(199169, EventEnum.PERSON_ACCESS_RECORD, "正常关门"),
	TYPE_5(199941, EventEnum.PERSON_ACCESS_RECORD, "门锁打开"),
	TYPE_6(199942, EventEnum.PERSON_ACCESS_RECORD, "门锁关闭"),
	TYPE_7(131614, EventEnum.PERSON_ACCESS_RECORD, "人脸抓拍事件"),
	UNKNOWN(-1, EventEnum.UNKNOWN, "未知事件类型");

	private Integer code;
	private EventEnum eventEnum;
	private String desc;

	public static ISCEventTypeEnum getByCode(Integer code) {
		if (code == null) return UNKNOWN;
		for (ISCEventTypeEnum typeEnum : ISCEventTypeEnum.values()) {
			if (typeEnum.getCode().equals(code)) {
				return typeEnum;
			}
		}
		return UNKNOWN;
	}
}
