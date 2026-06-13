package com.tce.smart.app.emun;

import java.util.Objects;

import com.tce.smart.common.core.util.StringUtils;

/**
 * 主题分类枚举
 *
 * @author mingkai.wu
 * @date 2019-05-12 18:09:37
 */
public enum SubjectCatalog {
	APP_BANNER("1", "banner"),
	NAVIGATE_MENU("2", "导航菜单"),
	PARK_NOTICE("3", "园区公告"),
	PARK_NEWS("4", "园区新闻"),
	PARK_GENERAL("5", "园区概况"),
	PARK_ACTIVITY("6", "园区活动"),
	PARK_INTRODUCE("7", "裕同简介"),
	PARK_CULTURE("8", "企业文化"),
	PARK_NAVIGATE("9", "园区导航"),
	PARK_VR("0", "园区VR"),
	EMPLOYEE_NOTICE("11", "新员工须知"),
	APP_LAUNCH("12", "引导启动"),
	DORM_AGREE("13", "外宿协议"),
	QUESTION("14", "常见问题"),
	MODULE_SETTING("15", "模块信息"),
	BOOT_PAGE("16", "引导页图片"),
	START_PAGE("17", "启动页图片"),
	ARREAL_AREA("18", "申诉专区");



	private final String type;

	private final String name;

	SubjectCatalog(String type, String name) {
		this.type = type;
		this.name = name;
	}

	public static SubjectCatalog type(String type) {
		if (Objects.nonNull(type)) {
			for (SubjectCatalog t : SubjectCatalog.values()) {
				if (StringUtils.isNotEmpty(t.type) && t.type.equals(type)) {
					return t;
				}
			}
		}
		return null;
	}

	public static String name(String type) {
		SubjectCatalog specialCrowdType = type(type);
		return Objects.nonNull(specialCrowdType) ? specialCrowdType.getName() : "";
	}

	public String type() {
		return type;
	}

	public String getName() {
		return name;
	}

}
