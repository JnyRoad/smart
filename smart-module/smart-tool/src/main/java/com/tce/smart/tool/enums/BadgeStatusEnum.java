package com.tce.smart.tool.enums;

import com.tce.smart.common.core.util.StringUtils;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.*;

/**
 * @description: 厂牌状态枚举
 * @date: 2020-07-09 17:52
 * @author: fushiping
 * @version: 1.0
 */
@Getter
@AllArgsConstructor
public enum BadgeStatusEnum {

    NOT_ISSUED(10, "未发卡"),

	NORMAL(20, "正常"),

	REPORT_THE_LOSS(40, "挂失"),

	REVERT(60, "退卡");


    private Integer code;
    private String desc;

    public static BadgeStatusEnum getEnum(Integer code) {
        if (Objects.nonNull(code)) {
            for (BadgeStatusEnum tempEnum : BadgeStatusEnum.values()) {
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
            for (BadgeStatusEnum tempEnum : BadgeStatusEnum.values()) {
                if (tempEnum.getDesc().equals(desc)) {
                    return tempEnum.getCode();
                }
            }
        }
        return null;
    }

    public static List<Map<String, Object>> list() {
        List<Map<String, Object>> list = new ArrayList<>();
        for (BadgeStatusEnum t : BadgeStatusEnum.values()) {
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
