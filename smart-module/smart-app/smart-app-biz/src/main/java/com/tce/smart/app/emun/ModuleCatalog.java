package com.tce.smart.app.emun;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 模块分类编码枚举
 * @author fushiping
 * @date 2019/5/21 11:27
 **/
public enum ModuleCatalog {
	BISINE(1, "业务模块"),
	PARENT(0, "顶级模块"),
	CUSTOM(2, "自定义模块");


	private final Integer type;
	private final String desc;

	ModuleCatalog(Integer type, String desc) {
		this.type = type;
		this.desc = desc;
	}

	public static ModuleCatalog type(Integer type) {
		if (Objects.nonNull(type)) {
			for (ModuleCatalog t : ModuleCatalog.values()) {
				if (null != t.type && t.type.equals(type)) {
					return t;
				}
			}
		}
		return null;
	}

	public static List<Map<String, Object>> list() {
		List<Map<String, Object>> list = new ArrayList<>();
		for (ModuleCatalog t : ModuleCatalog.values()) {
			if (t.type != null) {
				Map<String, Object> map = new HashMap<>();
				map.put("type", t.type);
				map.put("desc", t.desc);
				list.add(map);
			}
		}
		return list;
	}

	public Integer getType() {
		return type;
	}

	public String getDesc() {
		return desc;
	}
}
