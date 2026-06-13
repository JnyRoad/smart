package com.tce.smart.app.emun;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.tce.smart.common.core.util.StringUtils;

/**
 * 启用状态枚举
 *
 * @author mkwu
 * @date 2019-08-23
 */
public enum EnabledState {
	ENABLED("1", "可用"),
	DISENABLE("0", "不可用");

	private final String code;
	private final String desc;

	EnabledState(String code, String desc) {
		this.code = code;
		this.desc = desc;
	}

	public static EnabledState code(String code) {
		if (Objects.nonNull(code)) {
			for (EnabledState t : EnabledState.values()) {
				if (StringUtils.isNotEmpty(t.code) && t.code.equals(code)) {
					return t;
				}
			}
		}
		return null;
	}

	public static List<Map<String, Object>> list() {
		List<Map<String, Object>> list = new ArrayList<>();
		for (EnabledState t : EnabledState.values()) {
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
