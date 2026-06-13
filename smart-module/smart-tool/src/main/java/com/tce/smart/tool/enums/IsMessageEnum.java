package com.tce.smart.tool.enums;

import com.tce.smart.common.core.util.StringUtils;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.*;

/**
 * @description: 考勤汇总确认与工资签收短信通知开关
 * @date: 2020-07-09 17:52
 * @author: fushiping
 * @version: 1.0
 */
@Getter
@AllArgsConstructor
public enum IsMessageEnum {

    ON(1, "开启短信提醒"),

	OFF(2, "关闭短信提醒");


    private Integer code;
    private String desc;

    public static IsMessageEnum getEnum(Integer code) {
        if (Objects.nonNull(code)) {
            for (IsMessageEnum tempEnum : IsMessageEnum.values()) {
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
            for (IsMessageEnum tempEnum : IsMessageEnum.values()) {
                if (tempEnum.getDesc().equals(desc)) {
                    return tempEnum.getCode();
                }
            }
        }
        return null;
    }

    public static List<Map<String, Object>> list() {
        List<Map<String, Object>> list = new ArrayList<>();
        for (IsMessageEnum t : IsMessageEnum.values()) {
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
