package com.tce.smart.tool.enums;

import com.tce.smart.common.core.util.StringUtils;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.*;

/**
 * @Title: ExportTypeEnum
 * @Descripition:
 * @Auther: fushiping
 * @Date: 2020-07-10 14:26
 */
@Getter
@AllArgsConstructor
public enum ExportTypeEnum {
    BADGE_LOSS(1, "xls", "厂牌挂失记录"),

    BADGE_REPLY(2, "xls", "厂牌补领记录"),

	ARTICLES_RELEASE(3, "xls", "物品放行记录"),

	SECURITY_SUPPLIER(4, "xls", "保密区供应商记录");

    private Integer code;
    private String fileSuffix;
    private String desc;

    public static ExportTypeEnum exportType(Integer code) {
        if (Objects.nonNull(code)) {
            for (ExportTypeEnum tempEnum : ExportTypeEnum.values()) {
                if (tempEnum.getCode().equals(code)) {
                    return tempEnum;
                }
            }
        }
        return null;
    }

    public static String desc(Integer code) {
        return exportType(code).getDesc();
    }

    public static Integer code(String desc) {
        if (StringUtils.isNotEmpty(desc)) {
            for (ExportTypeEnum tempEnum : ExportTypeEnum.values()) {
                if (tempEnum.getDesc().equals(desc)) {
                    return tempEnum.getCode();
                }
            }
        }
        return null;
    }

}
