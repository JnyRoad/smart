package com.tce.smart.tool.enums;

import com.tce.smart.common.core.util.StringUtils;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.*;

/**
 * @description: 厂牌补领原因
 * @date: 2020-07-09 17:52
 * @author: fushiping
 * @version: 1.0
 */
@Getter
@AllArgsConstructor
public enum BadgeApplyReasonEnum {

    LOSS(1, "卡遗失"),

	DAMAGE(2, "卡损坏"),

	NOT_CARRY(3, "卡未携带"),

	UNAVAILABLE(4, "卡无法使用"),

	OTHERS(5, "其他原因");


    private Integer code;
    private String desc;

    public static BadgeApplyReasonEnum getEnum(Integer code) {
        if (Objects.nonNull(code)) {
            for (BadgeApplyReasonEnum tempEnum : BadgeApplyReasonEnum.values()) {
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
            for (BadgeApplyReasonEnum tempEnum : BadgeApplyReasonEnum.values()) {
                if (tempEnum.getDesc().equals(desc)) {
                    return tempEnum.getCode();
                }
            }
        }
        return null;
    }

    public static List<Map<String, Object>> list() {
        List<Map<String, Object>> list = new ArrayList<>();
        for (BadgeApplyReasonEnum t : BadgeApplyReasonEnum.values()) {
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
