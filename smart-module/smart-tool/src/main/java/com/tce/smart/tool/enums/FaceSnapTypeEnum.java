package com.tce.smart.tool.enums;

import com.tce.smart.common.core.util.StringUtils;

import java.util.Objects;

/**
 * ES人脸抓拍库人员类型枚举
 *
 * @author mckaywu
 * @date 2019-06-03 09:58:08
 */
public enum FaceSnapTypeEnum {
	// 短信验证码
	STAFF(1, "员工"),
	VISITOR(2, "访客"),
	Application(3, "应聘人员");

	private final Integer code;

	private final String desc;

	FaceSnapTypeEnum(Integer code, String desc) {
		this.code = code;
		this.desc = desc;
	}

	public static FaceSnapTypeEnum desc(Integer code) {
		if (Objects.nonNull(code)) {
			for (FaceSnapTypeEnum enmuType : FaceSnapTypeEnum.values()) {
				if (enmuType.code.equals(code)) {
					return enmuType;
				}
			}
		}
		return null;
	}

	public static Integer code(String desc) {
		if (StringUtils.isNotEmpty(desc)) {
			for (FaceSnapTypeEnum typeEnmu : FaceSnapTypeEnum.values()) {
				if (typeEnmu.desc.equals(desc)) {
					return typeEnmu.code;
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
