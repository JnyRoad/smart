package com.tce.smart.common.core.constant.enums;

public enum RecruitmentStatusEnum {

	Recruitment_END(0, "招聘结束"),
	Recruitment_ONGOING(1, "招聘中"),
	Recruitment_SUSPEND(2, "停止招聘");
	private Integer code;
	private String desc;

	private RecruitmentStatusEnum(Integer code, String desc) {
		this.code = code;
		this.desc = desc;
	}
	public void setCode(Integer code) {
		this.code = code;
	}
	public void setDesc(String desc) {
		this.desc = desc;
	}

	public Integer getCode(String desc) {
		return this.code;
	}
	public String getDesc(Integer code) {
		return this.desc;
	}
}
