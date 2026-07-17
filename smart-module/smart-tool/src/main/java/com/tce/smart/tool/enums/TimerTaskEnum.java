package com.tce.smart.tool.enums;

/**
 * @Description: TODO
 * @EnumName TimerTaskEnum
 * @Author jinbo
 * @Date 2019/5/6
 */
public enum TimerTaskEnum {
    LEAVE_REASON("timer_task_leave_reason", "timer_task_leave_reason", "离职原因同步任务"),
    LEAVE_TYPE("timer_task_leave_type", "timer_task_leave_type", "离职类型同步任务"),
    LEAINTENT("timer_task_leaintent", "timer_task_leaintent", "离职分类同步任务"),
    YSJOB_TYPE("timer_task_ys_job", "ys_job", "岗位信息同步任务"),
    EMPHR_TYPE("timer_task_emphr_job", "emphr_job", "员工信息同步任务"),
	DHR_IMG_TYPE("timer_task_dhr_img_type", "dhr_img_type", "员工dhr人脸照片同步任务"),
	DHR_IMG_XC("timer_task_dhr_img_xc", "dhr_img_xc", "许昌员工dhr人脸照片同步任务"),
	STAFF_NO_PHOTO_XC("timer_task_staff_no_photo_xc", "staff_no_photo_xc", "许昌员工人脸照片补偿同步任务"),
	STAFF_NO_PHOTO("timer_task_staff_no_photo", "staff_no_photo", "员工人脸照片补偿同步任务"),
    EMPDHR_TYPE("timer_task_empdhr_job", "empdhr_job", "DHR员工信息同步任务"),
    VISITOR_TYPE("timer_task_visitor", "visitor_task", "访客定时任务"),
    VISITOR_NO_LEAVE("visitor_no_leave","visitor_no_leave","访客超时未离开"),
	NEW_STAFF_RECHARGE("new_staff_recharge", "new_staff_recharge", "新员工充值名单同步任务"),
	SENIOR_STAFF_RECHARGE("senior_staff_recharge", "senior_staff_recharge", "在职员工充值名单同步任务"),
	WAGE_SIGN_INFO("wage_sign_info", "wage_sign_info", "工资签收名单同步生成员工信息任务"),
	ATTENDANCE_SIGN_INFO("attendance_sign_info", "attendance_sign_info", "考勤确认名单同步生成员工信息任务"),
	EHR_SET_SEND_MSG("ehr_set_send_msg", "ehr_set_send_msg", "工资签收提醒与考勤汇总确认提醒任务"),

    DEVICE_TYPE_DOWN_CARD("timer_task_device_down_card", "device_task_down_card", "设备卡片定时下发任务"),
	DEVICE_TYPE_DOWN_CAR("timer_task_device_down_car", "device_task_down_car", "设备车辆定时下发任务"),
	DEVICE_TYPE_DEL_CARD("timer_task_device_del_card", "device_task_del_card", "设备卡片定时删除任务"),
	DEVICE_TYPE_DEL_CAR("timer_task_device_del_car", "device_task_del_car", "设备车辆定时删除任务"),
	DEVICE_TASK_REPEAT("timer_task_repeat_device_del", "device_task_repeat_del", "设备定时重发任务"),
    REPLACE_TYPE("timer_task_replace", "replace_task", "考勤异常定时任务"),
    LOGISTICS_APPOINTMENT_TYPE("timer_task_logistics_appointment", "logistics_appointment_task", "物流车预约定时任务"),
    LEAVE_APPLICATION_PROCESS_TYPE("timer_task_leave_application_processt", "leave_application_process_task", "离职流程定时任务"),
//    SYNC_EMP_PHOTO("timer_task_emp_photo", "timer_task_emp_photo", "员工人脸照片同步定时任务"),
    MOVE_PLATFORM_DATA("data_move_task_platform", "data_move_task_platform", "platform数据清理定时任务"),
	MOVE_IMAGE_FRROM_HBASE_TO_DB("data_move_image_from_hbase_to_db", "data_move_image_from_hbase_to_db", "迁移Hbase图片到数据库"),
    UNKNOWN_TYPE("timer_task_unknown", "timer_task_unknown", "未知任务"),
	OUTDORMITORY_TYPE("timer_task_outdormitory","outdormitory_task","清理外宿任务"),
	PUSH_VISITOR_EMAIL("timer_task_push_visitor_email","timer_task_push_visitor_email","推送访客给指定email"),
	REFRESH_RECRUIT("timer_task_refresh_recruit","timer_task_refresh_recruit","更新招聘岗位时间"),
	SYNC_PERSON("timer_task_sync_person","sync_person","同步下发的人员数据"),
	SYNC_VEHICLE("timer_task_sync_vehicle","sync_vehicle","同步下发的车辆数据"),
	REFRESH_COMP("timer_task_refresh_comp","timer_task_refresh_comp","更新组织信息"),
	PAPER_STATUS("timer_task_paper_status","timer_task_paper_status","更新调查问卷的状态"),
	DEVICE_STATUS("timer_task_device_status","timer_task_device_status","更新设备状态"),

	SUPPLIER_NOTIFY("timer_task_supplier_notify","timer_task_supplier_notify","保密区供应商协议到期通知"),
	SUPPLIER_OA_AREA("timer_task_supplier_oa_area","timer_task_supplier_oa_area","OA保密区区域同步任务"),
	SUPPLIER_AUTO_AUTH_DELETE("timer_task_supplier_auto_auth_delete","timer_task_supplier_auto_auth_delete","保密区区域权限自动删除任务"),
	SUPPLIER_AUTH_MSG("timer_task_supplier_auth_msg","timer_task_supplier_auth_msg","保密区权限下发提示信息推送"),

	ADMITTANCE_OA_AREA_TYPE("timer_admittance_oa_area_type","timer_admittance_oa_area_type","OA区域类型同步任务"),
	ADMITTANCE_OVERTIME("timer_admittance_overtime","timer_admittance_overtime","入厂申请访客超时"),
	ADMITTANCE_NOT_LEAVE("timer_admittance_not_leave","timer_admittance_not_leave","入厂申请超时未离开"),
	ADMITTANCE_COME_ONTIME("timer_admittance_come_ontime","timer_admittance_come_ontime","入厂申请来访后删除权限"),
	ADMITTANCE_REMIND("timer_admittance_remind","timer_admittance_remind","来访前提示"),
	ADMITTANCE_UPDATE_OA("timer_admittance_update_oa","timer_admittance_update_oa","入厂申请OA审批状态更新"),
	SECURITY_AUTH_UPDATE_OA("timer_security_auth_update_oa","timer_security_auth_update_oa","保密门禁OA状态对账"),
	SECURITY_AUTH_DISPATCH_PROCESS("timer_security_auth_dispatch_process", "timer_security_auth_dispatch_process",
			"保密门禁权限异步下发"),
	OA_CALLBACK_LOG_CLEAN("timer_oa_callback_log_clean","timer_oa_callback_log_clean","OA回调日志过期清理任务"),

	NEWS_TERMINAL_CHECK("timer_news_terminal_check", "timer_news_terminal_check", "终端消息发布检查"),

	ISC_DEVICE_TYPE_DOWN_PERSON("timer_task_isc_device_down_person", "isc_device_task_down_person", "ISC设备人员定时下发任务"),
	ISC_DEVICE_TYPE_DEL_PERSON("timer_task_isc_device_del_person", "isc_device_task_del_person", "ISC设备人员定时删除任务"),
	ISC_DEVICE_TYPE_DOWN_CARD("timer_task_isc_device_down_card", "isc_device_task_down_card", "ISC设备卡片定时下发任务"),
	ISC_DEVICE_TYPE_DEL_CARD("timer_task_isc_device_del_card", "isc_device_task_del_card", "ISC设备卡片定时删除任务"),
	ISC_AUTH_PROCESS_HANDLE("timer_task_isc_auth_process_handle", "isc_auth_process_handle", "ISC设备权限配置进度处理任务"),
	ISC_AUTH_RESULT_HANDLE("timer_task_isc_auth_result_handle", "isc_auth_result_handle", "ISC设备下载权限结果处理任务"),
	ISC_AUTO_AUTH_RESULT_HANDLE("timer_task_isc_auto_auth_result_handle", "isc_auto_auth_result_handle", "ISC设备下载权限结果处理任务"),
	ISC_DEVICE_TYPE_DOWN_CAR("timer_task_isc_device_down_car", "isc_device_task_down_car", "ISC设备车辆定时下发任务"),
	ISC_DEVICE_TYPE_DEL_CAR("timer_task_isc_device_del_car", "isc_device_task_del_car", "ISC设备车辆定时删除任务"),
	ISC_DEVICE_TASK_REPEAT("timer_task_isc_repeat_device_del", "isc_device_task_repeat_del", "ISC设备定时重发任务"),
	ISC_DEVICE_SYNC("timer_task_isc_device_sync", "timer_task_isc_device_sync", "ISC设备同步重发任务"),
	ISC_PERSON_FACE_RETRY("timer_task_isc_person_face_retry", "timer_task_isc_person_face_retry", "ISC人员照片同步失败重试任务"),
	ISC_CARD_TASK_SYNC("timer_task_isc_card_task_sync", "timer_task_isc_card_task_sync", "ISC卡片资源同步任务"),
	ISC_TEMPERATURE_GET("timer_task_isc_temperature_get", "timer_task_isc_temperature_get", "ISC查询测温登记记录"),

	ISC_DEVICE_OFFLINE_HANDLER("timer_task_isc_device_offline_handler", "timer_task_isc_device_offline_handler", "ISC离线设备任务处理"),

	SMART_METER_STATUS("timer_smart_meter_status", "timer_smart_meter_status", "智能水电表集中器状态查询任务"),
	WATER_METER_READING("timer_water_meter_reading", "timer_water_meter_reading", "智能水表读数查询任务"),
	ELE_METER_READING("timer_ele_meter_reading", "timer_ele_meter_reading", "智能电表读数查询任务"),
	GEN_SETTLEMENT_DAILY("timer_gen_settlement_daily", "timer_gen_settlement_daily", "智能电表水电日结算"),

	DORMITORY_DEALY_QUIT("timer_dormitory_dealy_quit", "timer_dormitory_dealy_quit", "退宿申请延迟退宿");

	private final String type;
    private final String key;
    private final String desc;
    TimerTaskEnum(String key,String type,String desc){
        this.type = type;
        this.key = key;
        this.desc = desc;
    }

    public String getType() {
        return type;
    }

    public String getKey() {
        return key;
    }


    public String getDesc() {
        return desc;
    }

}
