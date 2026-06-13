package com.tce.smart.tool.enums;

import com.tce.smart.common.core.util.StringUtils;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.*;

/**
 * @description: 厂牌操作状态枚举
 * @date: 2020-07-09 17:52
 * @author: fushiping
 * @version: 1.0
 */
@Getter
@AllArgsConstructor
public enum BadgeOperaStatusEnum {

    APPLY(1, "已申请", "已提交申请", "提交申请"),

	AGREE(2, "已同意", "已同意领取", "人资审核"),

	CONFIRM(3, "已领取", "已确认领取", "领取厂牌"),

	REFUSE(4, "已拒绝", "已拒绝申请", "人资审核");


    private Integer code;
    private String desc;
    private String remark;
    private String title;

    public static BadgeOperaStatusEnum getEnum(Integer code) {
        if (Objects.nonNull(code)) {
            for (BadgeOperaStatusEnum tempEnum : BadgeOperaStatusEnum.values()) {
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

	public static String remark(Integer code) {
		return getEnum(code).getRemark();
	}

	public static String title(Integer code) {
		return getEnum(code).getTitle();
	}

    public static Integer code(String desc) {
        if (StringUtils.isNotEmpty(desc)) {
            for (BadgeOperaStatusEnum tempEnum : BadgeOperaStatusEnum.values()) {
                if (tempEnum.getDesc().equals(desc)) {
                    return tempEnum.getCode();
                }
            }
        }
        return null;
    }

    public static List<Map<String, Object>> list() {
        List<Map<String, Object>> list = new ArrayList<>();
        for (BadgeOperaStatusEnum t : BadgeOperaStatusEnum.values()) {
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
