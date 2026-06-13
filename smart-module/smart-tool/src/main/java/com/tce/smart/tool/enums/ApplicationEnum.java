package com.tce.smart.tool.enums;

import java.util.Objects;

import com.tce.smart.common.core.util.StringUtils;

/**
 * 审批状态
 * @author ly
 *
 */
public enum ApplicationEnum {


	RECORD_STATUS_0("0","批准"),
	RECORD_STATUS_1("1","保存"),
	RECORD_STATUS_2("2","提交"),
	RECORD_STATUS_3("3","退回"),
	RECORD_STATUS_4("4","重新打开"),
	RECORD_STATUS_5("5","删除"),
	RECORD_STATUS_6("6","激活"),
	RECORD_STATUS_7("7","转发"),
	RECORD_STATUS_9("9","批注"),
	RECORD_STATUS_i("i","流程干预"),
	RECORD_STATUS_e("e","强制归档"),
	RECORD_STATUS_t("t","抄送"),
	RECORD_STATUS_s("s","督办"),
	RECORD_STATUS_c("c","当前审批人"),
	RECORD_STATUS_10("10","已申请"),
	RECORD_STATUS_12("12","已回退"),
	RECORD_STATUS_11("11","已同意"),
	RECORD_STATUS_13("13","已撤销");

    private final String code;

    private final String desc;

    ApplicationEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static ApplicationEnum applicationAuthority(String code){
        if(Objects.nonNull(code)){
            for(ApplicationEnum alarmType : ApplicationEnum.values()){
                if(alarmType.code.equals(code)){
                    return alarmType;
                }
            }
        }
        return null;
    }

    public static String desc(String code){
	ApplicationEnum alarmType = applicationAuthority(code);
        return alarmType == null ? null : applicationAuthority(code).desc;
    }

    public static String code(String desc){
        if(StringUtils.isNotEmpty(desc)){
            for(ApplicationEnum applicationAuthority : ApplicationEnum.values()){
                if(applicationAuthority.desc.equals(desc)){
                    return applicationAuthority.code;
                }
            }
        }
        return null;
    }

    public static boolean existAuthority(String code){
	boolean result = false;
        if(Objects.nonNull(code)){
            for(ApplicationEnum alarmType : ApplicationEnum.values()){
	result = alarmType.code.equals(code);
	if(result) {
		return result;
	}
            }
        }
        return result;
    }

    public String getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }
}
