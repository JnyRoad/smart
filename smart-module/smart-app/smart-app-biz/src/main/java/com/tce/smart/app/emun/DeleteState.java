package com.tce.smart.app.emun;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.tce.smart.common.core.util.StringUtils;

/**
 * 主题发布状态
 *
 * @author mingkai.wu
 * @date 2019-05-13 15:51:29
 */
public enum DeleteState {
	NORMOL("1", "正常"),
	DELETE("0", "删除");

	private final String code;
	private final String desc;

	DeleteState(String code, String desc) {
		this.code = code;
		this.desc = desc;
	}

	public static DeleteState code(String code) {
		if (Objects.nonNull(code)) {
			for (DeleteState t : DeleteState.values()) {
				if (StringUtils.isNotEmpty(t.code) && t.code.equals(code)) {
					return t;
				}
			}
		}
		return null;
	}

	public static List<Map<String, Object>> list() {
		List<Map<String, Object>> list = new ArrayList<>();
		for (DeleteState t : DeleteState.values()) {
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
