package com.tce.smart.data.api.enums;

import com.tce.smart.common.core.util.StringUtils;

import java.util.Objects;

/**
 * @program: smart-module
 * @description:
 * @author: Wuling
 * @create: 2021-07-03 13:43
 **/

public enum DHREmpJChenEnum {
	// 101-总裁层，102-副总裁层，103-总监层，104-总经理层，105-经理层，
	//	 * 	 * 106-课长，107-班组长，108-职层，109-员工层，110-技工层

	JCHEN_101(101, "总裁层"),

	JCHEN_102(102, "副总裁层"),

	JCHEN_103(103, "总监层"),

	JCHEN_104(104, "总经理层"),

	JCHEN_105(105, "经理层"),

	JCHEN_106(106, "课长"),

	JCHEN_107(107, "班组长"),

	JCHEN_108(108, "职层"),

	JCHEN_109(109, "员工层"),

	JCHEN_110(110, "技工层");

	private final Integer code;

	private final String desc;

	DHREmpJChenEnum(Integer code, String desc) {
		this.code = code;
		this.desc = desc;
	}

	public static DHREmpJChenEnum getEnmu(Integer code) {
		if (Objects.nonNull(code)) {
			for (DHREmpJChenEnum enmuTemp : DHREmpJChenEnum.values()) {
				if (enmuTemp.code.equals(code)) {
					return enmuTemp;
				}
			}
		}
		return null;
	}

	public static String desc(Integer code) {
		DHREmpJChenEnum enmuTemp = getEnmu(code);
		return enmuTemp == null ? null : enmuTemp.desc;
	}

	public static Integer code(String desc) {
		if (StringUtils.isNotEmpty(desc)) {
			for (DHREmpJChenEnum enmuTemp : DHREmpJChenEnum.values()) {
				if (enmuTemp.desc.equals(desc)) {
					return enmuTemp.code;
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
