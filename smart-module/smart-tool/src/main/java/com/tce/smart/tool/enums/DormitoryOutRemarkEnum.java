package com.tce.smart.tool.enums;

import com.tce.smart.common.core.util.StringUtils;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.*;

/**
 * @description: 住宿备注枚举
 * @date: 2020-07-09 17:52
 * @author: fushiping
 * @version: 1.0
 */
@Getter
@AllArgsConstructor
public enum DormitoryOutRemarkEnum {

    NOT_SIGN(1, "出差"),

	SIGN(2, "请假"),

	AUTO_SIGN(3, "其他");


    private Integer code;
    private String desc;

    public static DormitoryOutRemarkEnum getEnum(Integer code) {
        if (Objects.nonNull(code)) {
            for (DormitoryOutRemarkEnum tempEnum : DormitoryOutRemarkEnum.values()) {
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

    public static Integer code(String desc) {
        if (StringUtils.isNotEmpty(desc)) {
            for (DormitoryOutRemarkEnum tempEnum : DormitoryOutRemarkEnum.values()) {
                if (tempEnum.getDesc().equals(desc)) {
                    return tempEnum.getCode();
                }
            }
        }
        return null;
    }

    public static List<Map<String, Object>> list() {
        List<Map<String, Object>> list = new ArrayList<>();
        for (DormitoryOutRemarkEnum t : DormitoryOutRemarkEnum.values()) {
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
