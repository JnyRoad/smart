package com.tce.smart.app.emun;

import com.tce.smart.common.core.util.StringUtils;

import java.util.*;

/**
 * description: AppAdverInfoEnums <br>
 * date: 2019/12/31 9:19 <br>
 * author: mckaywu <br>
 * version: 1.0 <br>
 */
public enum AdverPositionEnum {
	HOME_CENTER("1", "首页中部1"),
	MESSAGE_TOP("2", "消息界面顶部");

	private final String code;
	private final String desc;

	AdverPositionEnum(String code, String desc) {
		this.code = code;
		this.desc = desc;
	}


	public static AdverPositionEnum code(String code) {
		if (Objects.nonNull(code)) {
			for (AdverPositionEnum t : AdverPositionEnum.values()) {
				if (StringUtils.isNotEmpty(t.code) && t.code.equals(code)) {
					return t;
				}
			}
		}

		return null;
	}

	public static List<Map<String, Object>> list() {
		List<Map<String, Object>> list = new ArrayList<>();
		for (AdverPositionEnum t : AdverPositionEnum.values()) {
			if (t.code != null) {
				Map<String, Object> map = new HashMap<>();
				map.put("code", t.code);
				map.put("desc", t.desc);
				list.add(map);
			}
		}
		return list;
	}

	public String getCode() {
		return code;
	}

	public String getDesc() {
		return desc;
	}
}
