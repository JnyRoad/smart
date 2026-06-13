package com.tce.smart.tool.enums;

import java.util.Objects;

import com.tce.smart.common.core.util.StringUtils;

/**
 * 住宿状态 0-未住宿 1-内宿 2-外宿
 *
 * @author mckaywu
 * @date 2019-05-22 17:08:58
 */
public enum DormitoryStatusEnum {
	IS_INIT(0, "未住宿"), NOT_INNER(1, "内宿"), NOT_OUTER(2, "外宿");

	private final Integer code;
	private final String desc;

	DormitoryStatusEnum(Integer code, String desc) {
		this.code = code;
		this.desc = desc;
	}

	public static DormitoryStatusEnum desc(Integer code) {
		if (Objects.nonNull(code)) {
			for (DormitoryStatusEnum enmuType : DormitoryStatusEnum.values()) {
				if (enmuType.code.equals(code)) {
					return enmuType;
				}
			}
		}
		return null;
	}

	public static Integer code(String desc) {
		if (StringUtils.isNotEmpty(desc)) {
			for (DormitoryStatusEnum typeEnmu : DormitoryStatusEnum.values()) {
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
