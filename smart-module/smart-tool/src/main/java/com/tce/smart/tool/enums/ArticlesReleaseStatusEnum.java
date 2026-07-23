package com.tce.smart.tool.enums;

import com.tce.smart.common.core.util.StringUtils;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.*;

/**
 * @Auther: guohongtai
 * @Date: 2020-07-28 21:22
 */
@Getter
@AllArgsConstructor
public enum ArticlesReleaseStatusEnum {
	/** 仅用于办公区人员选择前的服务端归属锚点，不能进入审批或出厂流程。 */
	DRAFT(0, "草稿"),

	PENDING_APPROVAL(1, "待审批"),

	APPROVED(2, "已通过"),

	APPROVAL_FAILED(3, "已拒绝"),

	DEPARTURE(4, "已同意出厂"),

	REFUSE(5, "已拒绝出厂");

	private Integer code;
	private String desc;

	public static ArticlesReleaseStatusEnum getEnum(Integer code) {
		if (Objects.nonNull(code)) {
			for (ArticlesReleaseStatusEnum tempEnum : ArticlesReleaseStatusEnum.values()) {
				if (tempEnum.getCode().equals(code)) {
					return tempEnum;
				}
			}
		}
		return null;
	}

	public static String desc(Integer code) {
		return getEnum(code).getDesc();
	}

	public static Integer code(String desc) {
		if (StringUtils.isNotEmpty(desc)) {
			for (ArticlesReleaseStatusEnum tempEnum : ArticlesReleaseStatusEnum.values()) {
				if (tempEnum.getDesc().equals(desc)) {
					return tempEnum.getCode();
				}
			}
		}
		return null;
	}

	public static List<Map<String, Object>> list() {
		List<Map<String, Object>> list = new ArrayList<>();
		for (ArticlesReleaseStatusEnum t : ArticlesReleaseStatusEnum.values()) {
			if (t.code != null) {
				Map<String, Object> map = new HashMap<>();
				map.put("code", t.code);
				map.put("desc", t.desc);
				list.add(map);
			}
		}
		return list;
	}
}
