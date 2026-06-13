package com.tce.smart.tool.enums;

import java.util.Objects;

import com.tce.smart.common.core.util.StringUtils;


public enum RecruitmentStatusEnum {

	Recruitment_END(0, "招聘结束"),
	Recruitment_ONGOING(1, "招聘中"),
	Recruitment_SUSPEND(2, "停止招聘");
	private final Integer code;
	private final String desc;

	 public Integer getCode() {
		return code;
	}
	public String getDesc() {
		return desc;
	}

	RecruitmentStatusEnum(Integer code, String desc) {
		this.code = code;
		this.desc = desc;
	}

	@SuppressWarnings("unlikely-arg-type")
	public static RecruitmentStatusEnum desc(String code) {
		if (Objects.nonNull(code)) {
			for (RecruitmentStatusEnum enmuType : RecruitmentStatusEnum.values()) {
				if (String.valueOf(enmuType.code).equals(code)) {
					return enmuType;
				}
			}
		}
		return null;
	}

	public static Integer code(String desc) {
		if (StringUtils.isNotEmpty(desc)) {
			for (RecruitmentStatusEnum typeEnmu : RecruitmentStatusEnum.values()) {
				if (typeEnmu.desc.equals(desc)) {
					return typeEnmu.code;
				}
			}
		}
		return null;
	}
}
