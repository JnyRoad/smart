package com.tce.smart.tool.enums;

import com.tce.smart.common.core.util.StringUtils;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.*;

/**
 * @description: 考勤汇总确认签收状态枚举
 * @date: 2020-07-09 17:52
 * @author: fushiping
 * @version: 1.0
 */
@Getter
@AllArgsConstructor
public enum AttendanceSignStatusEnum {

    NOT_SIGN(1, "未确认", false),

	SIGN(2, "已确认", true),

	AUTO_SIGN(3, "自动确认", true);


    private Integer code;
    private String desc;
    private Boolean status;

    public static AttendanceSignStatusEnum getEnum(Integer code) {
        if (Objects.nonNull(code)) {
            for (AttendanceSignStatusEnum tempEnum : AttendanceSignStatusEnum.values()) {
                if (tempEnum.getCode().equals(code)) {
                    return tempEnum;
                }
            }
        }
        return null;
    }

    public static String desc(Integer code) {
		return getEnum(code).getDesc();
	}

	public static Boolean status(Integer code) {
		return getEnum(code).getStatus();
	}

    public static Integer code(String desc) {
        if (StringUtils.isNotEmpty(desc)) {
            for (AttendanceSignStatusEnum tempEnum : AttendanceSignStatusEnum.values()) {
                if (tempEnum.getDesc().equals(desc)) {
                    return tempEnum.getCode();
                }
            }
        }
        return null;
    }

    public static List<Map<String, Object>> list() {
        List<Map<String, Object>> list = new ArrayList<>();
        for (AttendanceSignStatusEnum t : AttendanceSignStatusEnum.values()) {
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
