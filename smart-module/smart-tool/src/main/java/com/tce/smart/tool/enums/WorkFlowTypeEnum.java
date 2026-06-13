package com.tce.smart.tool.enums;

import com.tce.smart.common.core.util.StringUtils;

import java.util.Objects;

public enum WorkFlowTypeEnum {
	VACATE_WORKFLOW_ID("34901","请假申请"),
	EXTRAWORK_WORKFLOW_ID("35021","加班申请"),
	REST_WORKFLOW_ID("35141","调休申请"),
	ATTENDANCE_PATCH_WORKFLOW_ID("35101","补卡申请"),
	DIMISSION_WORKFLOW_ID("34461","离职申请"),
	OUTDORMITORY_WORKFLOW_ID("36301","外宿申请"),
	/**
	 * 外餐申请CODE实际上是36301，为了与外宿申请进行区分写作363011
	 */
	OUTDORMITORY_FOOD_WORKFLOW_ID("363011","外餐申请"),
	CALLOWANCE_CANCEL_ID("10601","外宿补贴撤销"),
	SECURITY_AREA_VISIT_ID("743","保密区预约申请"),
	SECURITY_AUTH_APPLY_ID("31901", "保密权限申请"),
	ENTRY_FACTORY_APPLY_ID("28661", "入厂申请"),
	RELEASE_APPLY_ID("29061", "放行条申请"),
	HF_VISIT_ID("179521", "访客申请");


	private final String code;

    private final String desc;

    WorkFlowTypeEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static WorkFlowTypeEnum workFlowAuthority(String code){
        if(Objects.nonNull(code)){
            for(WorkFlowTypeEnum alarmType : WorkFlowTypeEnum.values()){
                if(alarmType.code.equals(code)){
                    return alarmType;
                }
            }
        }
        return null;
    }

    public static String desc(String code){
	WorkFlowTypeEnum alarmType = workFlowAuthority(code);
        return alarmType == null ? null : workFlowAuthority(code).desc;
    }

    public static String code(String desc){
        if(StringUtils.isNotEmpty(desc)){
            for(WorkFlowTypeEnum deviceAuthority : WorkFlowTypeEnum.values()){
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
            for(WorkFlowTypeEnum alarmType : WorkFlowTypeEnum.values()){
	result = alarmType.code.equals(String.valueOf(code));
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
