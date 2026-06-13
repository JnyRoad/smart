package com.tce.smart.tool.enums;

import com.tce.smart.common.core.util.StringUtils;

import java.util.*;

/**
 * OA状态枚举
 * @author fushiping
 * @date
 *
 */

public enum OaFinalStatusEnum {
	CAUSE_0(0, "创建/退回节点"),
	CAUSE_1(1, "提交"),
	CAUSE_2(2, "审批节点"),
	CAUSE_3(3, "归档节点");

	private final Integer code;

	private final String desc;

	OaFinalStatusEnum(Integer code, String desc) {
		this.code = code;
		this.desc = desc;
	}

	public static String desc(Integer code) {
		if (Objects.nonNull(code)) {
			for (OaFinalStatusEnum enmuType : OaFinalStatusEnum.values()) {
				if (enmuType.code.equals(code)) {
					return enmuType.desc;
				}
			}
		}
		return null;
	}

	public static Integer code(String desc) {
		if (StringUtils.isNotEmpty(desc)) {
			for (OaFinalStatusEnum typeEnmu : OaFinalStatusEnum.values()) {
				if (typeEnmu.desc.equals(desc)) {
					return typeEnmu.code;
				}
			}
		}
		return null;
	}

	public static List<Map<String, Object>> getTypeList() {
		List<Map<String, Object>> list = new ArrayList<>();
		for (OaFinalStatusEnum t : OaFinalStatusEnum.values()) {
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
