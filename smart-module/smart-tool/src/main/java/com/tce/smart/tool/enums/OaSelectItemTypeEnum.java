package com.tce.smart.tool.enums;

import com.tce.smart.common.core.util.StringUtils;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.*;

/**
 * @description: oa菜单字段类型表
 * @date: 2020-07-09 17:52
 * @author: fushiping
 * @version: 1.0
 */
@Getter
@AllArgsConstructor
public enum OaSelectItemTypeEnum {

    SECURITY_AREA(1, "保密区区域"),

	ADMITTANCE_AREA_TYPE(2, "入厂申请区域类型"),

	ADMITTANCE_FACTORY_TYPE(3, "入厂申请工厂区域类型"),

	SECURITY_FACTORY_TYPE(4, "保密区预约工厂区域类型");


    private Integer code;
    private String desc;

    public static OaSelectItemTypeEnum getEnum(Integer code) {
        if (Objects.nonNull(code)) {
            for (OaSelectItemTypeEnum tempEnum : OaSelectItemTypeEnum.values()) {
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
            for (OaSelectItemTypeEnum tempEnum : OaSelectItemTypeEnum.values()) {
                if (tempEnum.getDesc().equals(desc)) {
                    return tempEnum.getCode();
                }
            }
        }
        return null;
    }

    public static List<Map<String, Object>> list() {
        List<Map<String, Object>> list = new ArrayList<>();
        for (OaSelectItemTypeEnum t : OaSelectItemTypeEnum.values()) {
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
