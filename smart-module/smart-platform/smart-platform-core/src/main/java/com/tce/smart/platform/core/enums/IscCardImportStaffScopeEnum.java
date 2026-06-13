package com.tce.smart.platform.core.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum IscCardImportStaffScopeEnum {
	ALL("ALL", "全部人员"),
	ACTIVE("ACTIVE", "在职人员"),
	RESIGNED("RESIGNED", "离职人员");

	private final String code;
	private final String desc;

	public static IscCardImportStaffScopeEnum getByCode(String code) {
		if (code == null || code.trim().isEmpty()) {
			return ALL;
		}
		for (IscCardImportStaffScopeEnum scope : values()) {
			if (scope.code.equalsIgnoreCase(code.trim())) {
				return scope;
			}
		}
		return null;
	}
}
