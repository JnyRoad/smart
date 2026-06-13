package com.tce.smart.platform.core.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author sunfujian
 * @date 2021/8/24 19:03
 */
@Getter
@AllArgsConstructor
public enum DormitoryEnum {
	/**
	 * 1、园区；2、楼栋；3、楼层；4、房间
	 */
	PARK(1, "园区"),
	DORMITORY(2, "楼栋"),
	FLOOR(3, "楼层"),
	ROOM(4, "房间");

	private Integer code;
	private String desc;
}
