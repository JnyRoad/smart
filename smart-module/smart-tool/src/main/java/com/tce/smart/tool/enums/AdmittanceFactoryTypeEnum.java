package com.tce.smart.tool.enums;

import com.tce.smart.common.core.util.StringUtils;

import java.util.*;

/**
 * OA入厂申请事由
 * @author fushiping
 * @date
 *贵宾来访 ：政府单位、VIP客户（产品客户）=2，
 * 普通来访：施工、维修调试、面试、送货、出货、子集团人员、业务洽谈=3
 *
 */

public enum AdmittanceFactoryTypeEnum {
	NEW(15, "新工厂"),
	OLD(16, "老工厂");
//	前端需求去掉 17 新老工厂。  前端已硬编码写死
//	NEW_AND_OLD(17, "新工厂+老工厂");

	private final Integer code;

	private final String desc;

	AdmittanceFactoryTypeEnum(Integer code, String desc) {
		this.code = code;
		this.desc = desc;
	}

	public static String desc(Integer code) {
		if (Objects.nonNull(code)) {
			for (AdmittanceFactoryTypeEnum enmuType : AdmittanceFactoryTypeEnum.values()) {
				if (enmuType.code.equals(code)) {
					return enmuType.desc;
				}
			}
		}
		return null;
	}

	public static Integer code(String desc) {
		if (StringUtils.isNotEmpty(desc)) {
			for (AdmittanceFactoryTypeEnum typeEnmu : AdmittanceFactoryTypeEnum.values()) {
				if (typeEnmu.desc.equals(desc)) {
					return typeEnmu.code;
				}
			}
		}
		return null;
	}

	public static List<Map<String, Object>> getTypeList() {
		List<Map<String, Object>> list = new ArrayList<>();
		for (AdmittanceFactoryTypeEnum t : AdmittanceFactoryTypeEnum.values()) {
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
