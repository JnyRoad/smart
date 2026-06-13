package com.tce.smart.tool.enums;

import com.tce.smart.common.core.util.StringUtils;

import java.util.Objects;

/**
 * 员工充值名单同步状态
 *
 * @author mckaywu
 * @date 2019-06-02 16:25:02
 */
public enum RechargeSyncEnum {

	INIT(1, "未同步"),
	SUCCESS(2, "同步成功"),
	FAILD(3, "同步失败");

	private final Integer code;
	private final String desc;

	RechargeSyncEnum(Integer code, String desc) {
		this.code = code;
		this.desc = desc;
	}

	public static RechargeSyncEnum code(Integer code) {
		if (Objects.nonNull(code)) {
			for (RechargeSyncEnum t : RechargeSyncEnum.values()) {
				if (Objects.nonNull(t.code) && t.code.equals(code)) {
					return t;
				}
			}
		}
		return null;
	}

	public Integer getCode() {
		return code;
	}

	public String getDesc() {
		return desc;
	}

}
