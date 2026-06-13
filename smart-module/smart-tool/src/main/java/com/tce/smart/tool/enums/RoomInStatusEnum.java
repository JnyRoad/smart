package com.tce.smart.tool.enums;

import com.tce.smart.common.core.util.StringUtils;

import java.util.Objects;

/**
 * 宿舍房间入住率状态 1-未满， 2-已满， 3-空房
 *
 * @author wuling
 * @date 2020-10-23 17:08:58
 */
public enum RoomInStatusEnum {
	NON_FULL(1, "未满"),
	FULL(2, "已满"),
	EMPTY(3, "空房");

	private final Integer code;
	private final String desc;

	RoomInStatusEnum(Integer code, String desc) {
		this.code = code;
		this.desc = desc;
	}

	public static RoomInStatusEnum desc(Integer code) {
		if (Objects.nonNull(code)) {
			for (RoomInStatusEnum enmuType : RoomInStatusEnum.values()) {
				if (enmuType.code.equals(code)) {
					return enmuType;
				}
			}
		}
		return null;
	}

	public static Integer code(String desc) {
		if (StringUtils.isNotEmpty(desc)) {
			for (RoomInStatusEnum typeEnmu : RoomInStatusEnum.values()) {
				if (typeEnmu.desc.equals(desc)) {
					return typeEnmu.code;
				}
			}
		}
		return null;
	}

	public Integer getCode() {
		return code;
	}

	public String getDesc() {
		return desc;
	}

}
