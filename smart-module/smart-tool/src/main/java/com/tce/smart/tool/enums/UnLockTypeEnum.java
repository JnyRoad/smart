package com.tce.smart.tool.enums;

import com.tce.smart.common.core.util.StringUtils;

import java.util.*;

/**
 * 宿舍申请开锁状态枚举
 * @author fushiping
 * @date
 */

public enum UnLockTypeEnum {
	CAUSE_0(0, "设备不支持"),
	CAUSE_1(1, "已录入"),
	CAUSE_2(2, "未录入"),
	CAUSE_3(3, "已生成"),
	CAUSE_4(4, "未生成");

	private final Integer code;

	private final String desc;

	UnLockTypeEnum(Integer code, String desc) {
		this.code = code;
		this.desc = desc;
	}

	public static String desc(Integer code) {
		if (Objects.nonNull(code)) {
			for (UnLockTypeEnum enmuType : UnLockTypeEnum.values()) {
				if (enmuType.code.equals(code)) {
					return enmuType.desc;
				}
			}
		}
		return null;
	}

	public static Integer code(String desc) {
		if (StringUtils.isNotEmpty(desc)) {
			for (UnLockTypeEnum typeEnmu : UnLockTypeEnum.values()) {
				if (typeEnmu.desc.equals(desc)) {
					return typeEnmu.code;
				}
			}
		}
		return null;
	}

	public static List<Map<String, Object>> getTypeList() {
		List<Map<String, Object>> list = new ArrayList<>();
		for (UnLockTypeEnum t : UnLockTypeEnum.values()) {
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
