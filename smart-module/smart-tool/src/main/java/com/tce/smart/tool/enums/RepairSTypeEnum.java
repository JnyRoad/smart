package com.tce.smart.tool.enums;

import com.tce.smart.common.core.util.StringUtils;

import java.util.*;

/**
 * @description: 宿舍维修类型
 * @date: 2020-07-21 14:03
 * @author: wuling
 * @version: 1.0
 */
public enum RepairSTypeEnum {

	LAMP(1, "灯"),

	PLUG_BASE(2,"插座"),

	WATER_TAP(3,"水龙头"),

	WATER_PIPE(4,"水管"),

	DOOR(5,"门窗"),

	LOCK(6,"锁"),

	AIR_COND(7,"空调"),

	BED(9,"床"),

	CABINET(10,"柜子"),

	GLASS(11,"玻璃"),

	WASHING_TABLE(12,"洗手台"),

	TABLES(13,"桌椅"),

	DRAIN(14, "地漏"),

	OTHER(8, "其他");



	private final Integer code;

	private final String desc;

	RepairSTypeEnum(Integer code, String desc) {
		this.code = code;
		this.desc = desc;
	}

	public static RepairSTypeEnum getEnmu(Integer code) {
		if (Objects.nonNull(code)) {
			for (RepairSTypeEnum enmuTemp : RepairSTypeEnum.values()) {
				if (enmuTemp.code.equals(code)) {
					return enmuTemp;
				}
			}
		}
		return null;
	}

	public static String desc(Integer code) {
		RepairSTypeEnum enmuTemp = getEnmu(code);
		return enmuTemp == null ? null : enmuTemp.desc;
	}

	public static Integer code(String desc) {
		if (StringUtils.isNotEmpty(desc)) {
			for (RepairSTypeEnum enmuTemp : RepairSTypeEnum.values()) {
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

	public static List<Map<String, Object>> list() {
		List<Map<String, Object>> list = new ArrayList<>();
		for (RepairSTypeEnum t : RepairSTypeEnum.values()) {
			if (t.code != null) {
				Map<String, Object> map = new HashMap<>();
				map.put("code", t.code);
				map.put("desc", t.desc);
				list.add(map);
			}
		}
		return list;
	}
}
