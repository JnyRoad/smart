package com.tce.smart.platform.core.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum IscCardImportStatusEnum {
	INIT("INIT", "初始化"),
	RUNNING("RUNNING", "执行中"),
	SUCCESS("SUCCESS", "完成"),
	FAIL("FAIL", "失败");

	private final String code;
	private final String desc;
}
