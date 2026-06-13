package com.tce.smart.tool.enums;

import com.tce.smart.common.core.util.StringUtils;

import java.util.*;

/**
 * 退宿原因枚举
 * @author fushiping
 * @date
 */

public enum DormitoryQuitReasonEnum {
	RESIGNATION(3, "离职"),
	SELF_QUIT(5, "自离"),
	OUT_DORMITORY(2, "外宿");

	private final Integer code;

	private final String desc;

	DormitoryQuitReasonEnum(Integer code, String desc) {
		this.code = code;
		this.desc = desc;
	}

	public static String desc(Integer code) {
		if (Objects.nonNull(code)) {
			for (DormitoryQuitReasonEnum enmuType : DormitoryQuitReasonEnum.values()) {
				if (enmuType.code.equals(code)) {
					return enmuType.desc;
				}
			}
		}
		return null;
	}

	public static Integer code(String desc) {
		if (StringUtils.isNotEmpty(desc)) {
			for (DormitoryQuitReasonEnum typeEnmu : DormitoryQuitReasonEnum.values()) {
				if (typeEnmu.desc.equals(desc)) {
					return typeEnmu.code;
				}
			}
		}
		return null;
	}

	public static List<Map<String, Object>> getTypeList() {
		List<Map<String, Object>> list = new ArrayList<>();
		for (DormitoryQuitReasonEnum t : DormitoryQuitReasonEnum.values()) {
			if (Objects.nonNull(t.code)) {
				Map<String, Object> map = new HashMap<>();
				map.put("code", t.code);
				map.put("desc", t.desc);
				list.add(map);
			}
		}
		return list;
	}

	public Integer getCode() {
		return code;
	}

	public String getDesc() {
		return desc;
	}
}
