package com.tce.smart.platform.core.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum IscCardImportResultEnum {
	READY_IMPORT("READY_IMPORT", "可导入"),
	IMPORTED("IMPORTED", "已导入"),
	REMOVED("REMOVED", "已清理"),
	SKIP_SAME("SKIP_SAME", "已一致"),
	CONFLICT("CONFLICT", "冲突"),
	LOCAL_ONLY("LOCAL_ONLY", "本地多出"),
	ISC_EMPTY("ISC_EMPTY", "ISC无卡"),
	STAFF_NOT_FOUND("STAFF_NOT_FOUND", "ISC无人员"),
	INVALID("INVALID", "无效卡"),
	FAIL("FAIL", "失败");

	private final String code;
	private final String desc;
}
