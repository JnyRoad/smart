package com.tce.smart.tool.enums;

import com.tce.smart.common.core.util.StringUtils;

import java.util.*;

/**
 * OA入厂申请事由
 * @author fushiping
 * @date
 *
 *默认参观交流
 * 面试=0，洽工=1，交流参观=2，施工=3，维修设备=4，家属探望=5，送货=6，
 * 培训导师=8，动火作业施工=9，高空作业施工=10，吊装作业施工=11
 */

public enum AdmittanceCauseEnum {
	CAUSE_0(0, "面试"),
	CAUSE_1(1, "洽工"),
	CAUSE_2(2, "交流参观"),
	CAUSE_3(3, "施工"),
	CAUSE_4(4, "维修设备"),
	CAUSE_5(5, "家属探望"),
	CAUSE_6(6, "送货"),
	CAUSE_8(8, "培训导师"),
	CAUSE_9(9, "动火作业施工"),
	CAUSE_10(10, "高空作业施工"),
	CAUSE_11(11, "吊装作业施工"),
	CAUSE_13(13, "新厂装货"),
	CAUSE_14(14, "新厂卸货"),
	CAUSE_15(15, "老厂装货"),
	CAUSE_16(16, "老厂卸货");

	private final Integer code;

	private final String desc;

	AdmittanceCauseEnum(Integer code, String desc) {
		this.code = code;
		this.desc = desc;
	}

	public static String desc(Integer code) {
		if (Objects.nonNull(code)) {
			for (AdmittanceCauseEnum enmuType : AdmittanceCauseEnum.values()) {
				if (enmuType.code.equals(code)) {
					return enmuType.desc;
				}
			}
		}
		return null;
	}

	public static Integer code(String desc) {
		if (StringUtils.isNotEmpty(desc)) {
			for (AdmittanceCauseEnum typeEnmu : AdmittanceCauseEnum.values()) {
				if (typeEnmu.desc.equals(desc)) {
					return typeEnmu.code;
				}
			}
		}
		return null;
	}

	public static List<Map<String, Object>> getTypeList() {
		List<Map<String, Object>> list = new ArrayList<>();
		for (AdmittanceCauseEnum t : AdmittanceCauseEnum.values()) {
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
