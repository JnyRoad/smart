package com.tce.smart.tool.enums;

import com.tce.smart.common.core.util.StringUtils;

import java.util.Objects;

/**
 * 出差交通
 * @author liangyuan
 *
 */
public enum TransportLargeClassEnum {


	TRANSPORT_LARGE_0(0,"火车"),
	TRANSPORT_LARGE_1(1,"飞机"),
	TRANSPORT_LARGE_2(2,"汽车"),
	TRANSPORT_LARGE_3(3,"轮船"),
	TRANSPORT_LARGE_4(4,"其他"),
	TRANSPORT_LARGE_5(5,"高铁"),
	TRANSPORT_LARGE_6(6,"动车");

    private final Integer code;

    private final String desc;

    TransportLargeClassEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static TransportLargeClassEnum transportLargeClassAuthority(Integer code){
        if(Objects.nonNull(code)){
            for(TransportLargeClassEnum transportLargeType : TransportLargeClassEnum.values()){
                if(transportLargeType.code.equals(code)){
                    return transportLargeType;
                }
            }
        }
        return null;
    }

    public static String desc(Integer code){
	TransportLargeClassEnum transportLargeType = transportLargeClassAuthority(code);
        return transportLargeType == null ? null : transportLargeClassAuthority(code).desc;
    }

    public static Integer code(String desc){
        if(StringUtils.isNotEmpty(desc)){
            for(TransportLargeClassEnum tripTypeAuthority : TransportLargeClassEnum.values()){
                if(tripTypeAuthority.desc.equals(desc)){
                    return tripTypeAuthority.code;
                }
            }
        }
        return null;
    }

    public static boolean existAuthority(Integer code){
	boolean result = false;
        if(Objects.nonNull(code)){
            for(TransportLargeClassEnum alarmType : TransportLargeClassEnum.values()){
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
