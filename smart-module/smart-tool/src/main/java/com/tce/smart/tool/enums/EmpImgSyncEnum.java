package com.tce.smart.tool.enums;

import java.util.Objects;

import com.tce.smart.common.core.util.StringUtils;

/**
 * 员工人脸照片同步状态
 *
 * @author mckaywu
 * @date 2019-06-02 16:25:02
 */
public enum EmpImgSyncEnum {
	FAILD("-1", "同步失败"),
	INIT("0", "未同步"),
	SUCCESS("1", "同步成功");

	private final String code;
	private final String desc;

	EmpImgSyncEnum(String code, String desc) {
		this.code = code;
		this.desc = desc;
	}

	public static EmpImgSyncEnum code(String code) {
		if (Objects.nonNull(code)) {
			for (EmpImgSyncEnum t : EmpImgSyncEnum.values()) {
				if (StringUtils.isNotEmpty(t.code) && t.code.equals(code)) {
					return t;
				}
			}
		}
		return null;
	}

	public String getCode() {
		return code;
	}

	public String getDesc() {
		return desc;
	}

}
