package com.tce.smart.tool.enums;

import com.tce.smart.common.core.util.StringUtils;

import java.util.Objects;

/**
 * 员工类型
 * @author QIPEI
 *
 */
public enum EmpTypeEnum {



	TYPE1(1,"正式工","正式工"),
	TYPE1_1(1,"裕备生","裕备生"),
	TYPE2(2,"实习生","实习生"),
	TYPE3(9,"派遣工","劳务派遣工"),
	TYPE3_1(9,"派遣工","派遣工"),
	TYPE4(10,"临时工A类","劳务用工"),
	TYPE5(13,"临时工B类","临时工B类"),
	TYPE6(14,"自招挂安联","自招挂派遣"),
	TYPE7(15,"兼职","兼职"),
	TYPE8(16,"退休返聘","退休返聘"),
	TYPE_OTHER(-1,"其他","其他");

	EmpTypeEnum(Integer code, String desc, String dhrDesc) {
		this.code = code;
		this.desc = desc;
		this.dhrDesc = dhrDesc;
	}

	private final Integer code;
	private final String desc;
	private final String dhrDesc;

	public Integer getCode() {
		return this.code;
	}
	public String getDesc() {
		return this.desc;
	}

    public static EmpTypeEnum staffAuthority(Integer code){
        if(Objects.nonNull(code)){
            for(EmpTypeEnum alarmType : EmpTypeEnum.values()){
                if(alarmType.code.equals(code)){
                    return alarmType;
                }
            }
        }
		return TYPE_OTHER;
    }

    public static String desc(Integer code){
	EmpTypeEnum alarmType = staffAuthority(code);
        return alarmType == null ? null : staffAuthority(code).desc;
    }

    public static Integer code(String desc){
        if(StringUtils.isNotEmpty(desc)){
            for(EmpTypeEnum deviceAuthority : EmpTypeEnum.values()){
                if(deviceAuthority.desc.equals(desc)){
                    return deviceAuthority.code;
                }
            }
        }
		return TYPE_OTHER.code;
    }

	public static Integer codeByDHR(String dhrDesc){
		if(StringUtils.isNotEmpty(dhrDesc)){
			for(EmpTypeEnum deviceAuthority : EmpTypeEnum.values()){
				if(deviceAuthority.dhrDesc.equals(dhrDesc)){
					return deviceAuthority.code;
				}
			}
		}
		return TYPE_OTHER.code;
	}

    public static boolean existAuthority(Integer code){
	boolean result = false;
        if(Objects.nonNull(code)){
            for(EmpTypeEnum alarmType : EmpTypeEnum.values()){
	result = alarmType.code.equals(code);
	if(result) {
		return result;
	}
            }
        }
        return result;
    }
}
