package com.tce.smart.platform.core.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum IscCardImportModeEnum {
	DRY_RUN("DRY_RUN", "预检"),
	IMPORT("IMPORT", "导入");

	private final String code;
	private final String desc;

	public static boolean isImport(String code) {
		return IMPORT.code.equals(code);
	}
}
