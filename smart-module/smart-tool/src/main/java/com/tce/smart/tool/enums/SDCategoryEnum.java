package com.tce.smart.tool.enums;


import com.tce.smart.common.core.util.StringUtils;

import java.util.Objects;

/**
 * 水电收费项目
 * 这里的定义 必须和数据库的SMT_TEMPLATES_RULE表中的CATEGORY_ID字段定义一致
 * @author wuling
 *
 */
public enum SDCategoryEnum {

	HOT_WATER(1, "热水"),
	COLD_WATER(2, "冷水"),
	ELECTRIC(3, "电");


    private final Integer code;

    private final String desc;

    SDCategoryEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static SDCategoryEnum deviceAuthority(Integer code){
        if(Objects.nonNull(code)){
            for(SDCategoryEnum alarmType : SDCategoryEnum.values()){
                if(alarmType.code.equals(code)){
                    return alarmType;
                }
            }
        }
        return null;
    }

    public static String desc(Integer code){
	SDCategoryEnum alarmType = deviceAuthority(code);
        return alarmType == null ? null : alarmType.desc;
    }

    public static Integer code(String desc){
        if(StringUtils.isNotEmpty(desc)){
            for(SDCategoryEnum deviceAuthority : SDCategoryEnum.values()){
                if(deviceAuthority.desc.equals(desc)){
                    return deviceAuthority.code;
                }
            }
        }
        return null;
    }

    public static boolean existAuthority(Integer code){
	boolean result = false;
        if(Objects.nonNull(code)){
            for(SDCategoryEnum alarmType : SDCategoryEnum.values()){
	result = alarmType.code.equals(code);
	if(result) {
		return result;
	}
            }
        }
        return result;
    }

    public Integer getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

}
