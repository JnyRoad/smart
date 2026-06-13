package com.tce.smart.tool.enums;

import com.tce.smart.common.core.util.StringUtils;

import java.util.Objects;

/**
 * 出差交通
 * @author liangyuan
 *
 */
public enum TransportSubClassEnum {


	TRANSPORT_SUB_0(0,"硬座"),
	TRANSPORT_SUB_1(1,"软座"),
	TRANSPORT_SUB_2(2,"硬卧"),
	TRANSPORT_SUB_3(3,"软卧"),
	TRANSPORT_SUB_4(4,"头等"),
	TRANSPORT_SUB_5(5,"公务"),
	TRANSPORT_SUB_6(6,"经济"),
	TRANSPORT_SUB_7(7,"无"),
	TRANSPORT_SUB_8(8,"其他"),
	TRANSPORT_SUB_9(9,"商务"),
	TRANSPORT_SUB_10(10,"一等座"),
	TRANSPORT_LARGE_11(11,"二等座");

    private final Integer code;

    private final String desc;

    TransportSubClassEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static TransportSubClassEnum transportSubAuthority(Integer code){
        if(Objects.nonNull(code)){
            for(TransportSubClassEnum transportSubType : TransportSubClassEnum.values()){
                if(transportSubType.code.equals(code)){
                    return transportSubType;
                }
            }
        }
        return null;
    }

    public static String desc(Integer code){
	TransportSubClassEnum transportSubType = transportSubAuthority(code);
        return transportSubType == null ? null : transportSubAuthority(code).desc;
    }

    public static Integer code(String desc){
        if(StringUtils.isNotEmpty(desc)){
            for(TransportSubClassEnum transportSubAuthority : TransportSubClassEnum.values()){
                if(transportSubAuthority.desc.equals(desc)){
                    return transportSubAuthority.code;
                }
            }
        }
        return null;
    }

    public static boolean existAuthority(Integer code){
	boolean result = false;
        if(Objects.nonNull(code)){
            for(TransportSubClassEnum alarmType : TransportSubClassEnum.values()){
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
