package com.tce.smart.tool.enums;

import com.tce.smart.common.core.util.StringUtils;

import java.util.*;

/**
 * 访客的的来访事由
 * @author ly
 *
 */
public enum VisitorEnum {


	CAUSE_1(1,0,"业务会谈"),
	CAUSE_2(2,1,"面试"),
	CAUSE_3(3,2,"拜访"),
	CAUSE_5(5,3,"园区驻厂"),
	CAUSE_6(6,4,"入职"),
	CAUSE_7(7,5,"家属住宿"),
	CAUSE_4(4,6,"其他");

    private final Integer code;

    private final Integer oaCode;

    private final String desc;

    VisitorEnum(Integer code, Integer oaCode, String desc) {
	this.oaCode = oaCode;
        this.code = code;
        this.desc = desc;
    }

    public static VisitorEnum visitorAuthority(Integer code){
        if(Objects.nonNull(code)){
            for(VisitorEnum alarmType : VisitorEnum.values()){
                if(alarmType.code.equals(code)){
                    return alarmType;
                }
            }
        }
        return null;
    }

    public static String desc(Integer code){
	VisitorEnum alarmType = visitorAuthority(code);
        return alarmType == null ? null : visitorAuthority(code).desc;
    }

	public static Integer oaCode(Integer code){
		VisitorEnum alarmType = visitorAuthority(code);
		return alarmType == null ? null : visitorAuthority(code).oaCode;
	}

    public static Integer code(String desc){
        if(StringUtils.isNotEmpty(desc)){
            for(VisitorEnum deviceAuthority : VisitorEnum.values()){
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
            for(VisitorEnum alarmType : VisitorEnum.values()){
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

	public static List<Map<String, Object>> getTypeList() {
		List<Map<String, Object>> list = new ArrayList<>();
		for (VisitorEnum t : VisitorEnum.values()) {
			if (Objects.nonNull(t.code)) {
				Map<String, Object> map = new HashMap<>();
				map.put("code", t.code);
				map.put("desc", t.desc);
				list.add(map);
			}
		}
		return list;
	}

}
