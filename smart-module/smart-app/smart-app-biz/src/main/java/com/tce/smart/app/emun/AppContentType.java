package com.tce.smart.app.emun;

import java.util.Objects;

/**
 * App内容类型枚举
 *
 * @author mckaywu
 * @date 2019-06-04 10:29:43
 */
public enum AppContentType {
	/**
	 * 链接
	 */
	LINK(1, "自定义URL链接跳转"),

	/**
	 * 详情
	 */
	DESC(2, "内容详情页跳转"),

	/**
	 * 模块
	 */
	MODULE(3, "APP模块跳转"),

	/**
	 * PDF附件
	 */
	PDF(4, "PDF附件");

	private final Integer type;
	private final String desc;

	AppContentType(Integer type, String desc) {
		this.type = type;
		this.desc = desc;
	}

	public static AppContentType type(Integer type) {
		if (Objects.nonNull(type)) {
			for (AppContentType t : AppContentType.values()) {
				if (null != t.type && t.type.equals(type)) {
					return t;
				}
			}
		}
		return null;
	}

	public Integer getType() {
		return type;
	}

	public String getDesc() {
		return desc;
	}
}
