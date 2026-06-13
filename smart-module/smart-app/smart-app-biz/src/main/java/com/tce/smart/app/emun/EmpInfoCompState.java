package com.tce.smart.app.emun;

import java.util.Objects;

import com.tce.smart.common.core.util.StringUtils;

/**
 * 身份证、人脸采集信息完善状态
 * @author mckaywu
 * @date 2019-06-02 16:25:02
 */
public enum EmpInfoCompState {
	UN_USE("0", "未使用"),
	USED("1", "已使用");

	private final String code;
	private final String desc;

	EmpInfoCompState(String code, String desc) {
		this.code = code;
		this.desc = desc;
	}

	public static EmpInfoCompState code(String code) {
		if (Objects.nonNull(code)) {
			for (EmpInfoCompState t : EmpInfoCompState.values()) {
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
