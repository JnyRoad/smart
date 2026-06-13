package com.tce.smart.tool.enums;

import com.tce.smart.common.core.util.StringUtils;

import java.util.Objects;

/**
 * 身份证照片枚举类
 *
 * @author mingkai.wu
 * @date 2019-05-14 20:40:40
 */
public enum IdCardImgTypeEnum {

	IMAGE_FRONT("2", "身份证正面照片"),
	IMAGE_BACK("3", "身份证背面照片");

	private final String code;

	private final String desc;

	IdCardImgTypeEnum(String code, String desc) {
		this.code = code;
		this.desc = desc;
	}

	public static IdCardImgTypeEnum desc(String code) {
		if (Objects.nonNull(code)) {
			for (IdCardImgTypeEnum enmuType : IdCardImgTypeEnum.values()) {
				if (enmuType.code.equals(code)) {
					return enmuType;
				}
			}
		}
		return null;
	}

	public static String code(String desc) {
		if (StringUtils.isNotEmpty(desc)) {
			for (IdCardImgTypeEnum typeEnmu : IdCardImgTypeEnum.values()) {
				if (typeEnmu.desc.equals(desc)) {
					return typeEnmu.code;
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
