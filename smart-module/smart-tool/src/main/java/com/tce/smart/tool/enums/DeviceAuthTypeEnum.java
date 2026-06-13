package com.tce.smart.tool.enums;

import com.tce.smart.common.core.util.StringUtils;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.*;

/**
 * @description: 设备权限类型枚举
 * @date: 2020-07-09 17:52
 * @author: fushiping
 * @version: 1.0
 */
@Getter
@AllArgsConstructor
public enum DeviceAuthTypeEnum {

    PERSON(1, "人员权限"),
    VISITOR(2, "访客权限"),
	VEHICLE(3, "车辆权限");


    private Integer code;
    private String desc;

    public static DeviceAuthTypeEnum getEnum(Integer code) {
        if (Objects.nonNull(code)) {
            for (DeviceAuthTypeEnum tempEnum : DeviceAuthTypeEnum.values()) {
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
            for (DeviceAuthTypeEnum tempEnum : DeviceAuthTypeEnum.values()) {
                if (tempEnum.getDesc().equals(desc)) {
                    return tempEnum.getCode();
                }
            }
        }
        return null;
    }

    public static List<Map<String, Object>> list() {
        List<Map<String, Object>> list = new ArrayList<>();
        for (DeviceAuthTypeEnum t : DeviceAuthTypeEnum.values()) {
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
