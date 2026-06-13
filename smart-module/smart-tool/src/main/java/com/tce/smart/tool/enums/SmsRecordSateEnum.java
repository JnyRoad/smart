package com.tce.smart.tool.enums;

import com.tce.smart.common.core.util.StringUtils;

import java.util.Objects;

/**
 * 短信模板枚举
 *
 * @author mingkai.wu
 * @date 2019-05-15 20:12:18
 */
public enum SmsRecordSateEnum {
	INIT(0, "初始化"),
	SUCCESS(1, "发送成功"),
	FAILD(2, "发送失败");

	private final Integer code;

	private final String desc;

	SmsRecordSateEnum(Integer code, String desc) {
		this.code = code;
		this.desc = desc;
	}

	public static SmsRecordSateEnum desc(Integer code) {
		if (Objects.nonNull(code)) {
			for (SmsRecordSateEnum enmuType : SmsRecordSateEnum.values()) {
				if (enmuType.code.equals(code)) {
					return enmuType;
				}
			}
		}
		return null;
	}

	public static Integer code(String desc) {
		if (StringUtils.isNotEmpty(desc)) {
			for (SmsRecordSateEnum typeEnmu : SmsRecordSateEnum.values()) {
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
