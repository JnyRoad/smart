package com.tce.smart.tool.enums;

/**
 * @author 梁圆
 * @Description 所有业务异常的枚举
 * @date 2019年4月12日 下午5:04:51
 */
public enum ExceptionTypeEnum {

    CHECK_SUCCESS(200, "验证成功"),
    SERVER_ERROR(500, "服务器异常"),


    LACK_PAGE_PARAMETER(10000, "缺少分页参数"),

    /**
     * 访客相关异常
     */
    VISITOR_NAME_EMPTY(54001, "访客姓名为空"),
    VISITOR_PHOTO_ID_EMPTY(54002, "访客照片id为空"),
    VISITOR_PHOTO_ID_PLATFORM_EMPTY(54038, "访客照片和车牌号不能同时为空"),
    VISITOR_PHONE_EMPTY(54003, "访客手机号为空"),
    VISITOR_STATIS_EMPTY(54004, "访客来访状态为空"),
    VISITOR_CAUSE_EMPTY(54005, "访客来访事由为空"),
    VISITOR_STATUS_EMPTY(54006, "访客来访状态为空"),
    VISITOR_STARTTIME_EMPTY(54007, "访客预计来访开始时间为空"),
    VISITOR_ENDTIME_EMPTY(54008, "访客预计来访结束时间为空"),
    VISITOR_RECEPTIONIST_NAME_EMPTY(54009, "访客接待人姓名为空"),
    VISITOR_RECEPTIONIST_PHONE_EMPTY(54010, "访客接待人手机号为空"),


    FELLOW_NAME_EMPTY(54011, "访客随行人员姓名为空"),
    FELLOW_PHOTO_ID_EMPTY(54012, "访客随行人员照片id为空"),


    VISITOR_NAME_LENGTH_ERROR(54013, "访客姓名输入汉字、英文、数字及下划线1-30个字符"),
    RECEPTIONIST_NAME_LENGTH_ERROR(54014, "接待人姓名输入汉字、英文、数字及下划线1-30个字符"),
    FELLOW_NAME_LENGTH_ERROR(54015, "访客随行人员名称不能超过30个字符"),
    VISITOR_PHONE_ERROR(54016, "访客手机号不正确，只许输入1-20位字符"),
    VISITOR_CAUSE_ERROR(54017, "访客来访事由不正确，只许输入1-100位字符"),
    VISITOR_STATUS_ERROR(54018, "访客状态不正确，只许输入0-5中的数字"),
    RECEPTIONIST_PHONE_ERROR(54019, "接待人手机号不正确，输入正确手机号例：13565689574"),
    VISITOR_VEHICLE_PLATE_ERROR(54020, "访客车牌号不正确，输入正确车牌例：京A12345"),
    VISITOR_STARTTIME_FORMAT_ERROR(54021, "访客预计来访开始时间格式不正确(yyyy-MM-dd HH:mm:ss)"),
    VISITOR_ENDTIME_FORMAT_ERROR(54022, "访客预计来访结束时间格式不正确(yyyy-MM-dd HH:mm:ss)"),
    VISITOR_ENDTIME_CANT_BEFORE_STARTTIME(54023, "结束时间不能早于开始时间"),
	VISITOR_AREA_TYPE_EMPTY(54039, "请选择授权区域"),
	VISITOR_AREA_TYPE_ERROR(54040, "授权区域不正确，请重新选择"),

	VISITOR_RECEPTIONIST_ERROR(54024, "被访人有误，请核对被访人信息后重新添加！"),
	VISITOR_ID_ERROR(54025, "访客不存在"),
	VISITOR_STAFF_ID_ERROR(54026, "员工号不能为空"),
	VISITOR_TYPE_ERROR(54027, "预约类型不能为空"),
	VISITOR_ID_NULL(54028, "访客的id为空"),
	VISITOR_REMOTE_CARD_NULL(54029, "下发闸机失败，请检查"),
	VISITOR_REMOTE_CAR_CARD_NULL(54030, "下发道闸失败，请检查"),
	VISITOR_PROMOTERBADGE_NULL(54031, "预约发起人的员工号不存在"),
	VISITOR_APPROVE_STATUS_ERROE(54032, "修改审核状态失败"),
	VISITOR_DELETE_CARD_NULL(54033, "删除闸机数据失败，请检查"),
	VISITOR_DELETE_CAR_CARD_NULL(54034, "删除道闸数据失败，请检查"),
	VISITOR_JCHE_ID_ERROR(54035, "该员工没有权限添加访客"),
	VISITOR_RECEPTIONIST_JCHE_ERROR(54036, "被访人职层无法被预约"),

    VISITOR_PHOTO_NULL(54036, "请上传人脸照片"),
    VISITOR_PHOTO_ERROR(54037, "未检测到人脸，请重新上传"),
	VISITOR_CODE_ERROR(54038, "访客码已失效"),
	/**
	 * 抓拍人员的信息
	 */
	SNAP_PERSON_ID_EMPTY(51001, "抓拍人员的id不能为空"),
	SNAP_PERSON_EVENT_TYPE_EMPTY(51001, "事件类型不能为空"),
	SNAP_PERSON_EVENT_TYPE_ERROR(51003, "事件类型只能为1或者2"),
	SNAP_PHOTO_ID_EMPTY(51004, "抓拍人员的图片id不能为空"),
    SNAP_TIME_ERROR(51005, "抓拍时间格式不正确(yyyy-MM-dd HH:mm:ss)"),
	SNAP_DEVICE_ID_ERROR(51006, "抓拍的设备id不存在"),


	/**
	 * 调休的信息
	 * @param code
	 * @param message
	 */
	BREAK_OFF_PARAMETER(52001,"调休的参数不能为空"),
	BREAK_OFF_REST_COUNT(52002,"调休时长只允许为数字"),
	BREAK_OFF_VACAT_EDESC_NULL(52003,"调休原因不能为空"),
	BREAK_OFF__WORK_TIME_ERROR(52004,"调休日期时间格式不正确(yyyy-MM-dd HH:mm:ss)"),
	BREAK_OFF__REST_TIME_ERROR(52005,"考勤日期时间格式不正确(yyyy-MM-dd HH:mm:ss)"),
	BREAK_OFF_ID_PARAMETER(52006,"该调休id不存在"),
	BREAK_OFF_TYPE_PARAMETER(52007,"该调休类型不能为空"),
	BREAK_OFF_REST_COUNT_NULL(52008,"要调休的天数不能为空"),
	BREAK_OFF_STAFF_BADGE_PARAMETER(52009,"员工号不存在"),
	BREAK_OFF_STAFF_BADGE_ERROE(52010,"员工号不能为空"),
	BREAK_OFF_TYPE_TERMID(52011,"该调休出勤id不能为空"),
	BREAK_OFF_TYPE_WORK(52012,"该调休出勤日期不能为空"),

	/**
	 * 出差的信息
	 * @param code
	 * @param message
	 */
	TRAVEL_PARAMETER_ERROR(53001,"出差的参数不能为空"),
	TRAVEL_COUNT_ERROR(53002,"出差时长只允许为数字"),
	TRAVEL_CITY_ERROR(53003,"出差城市名称只允许汉字、字母与数字的组合，最长为30个字符"),
	TRAVEL_DESC_ERROR(53004,"出差原因不能为空"),
	TRAVLE_TIME_ERROR(53005,"开始时间不能小于结束时间"),
	TRAVEL_COUNT_ERROR_NULL(53006,"出差时长不能为空"),
	TRAVEL_CITY_ERROR_NULL(53007,"出差城市名称不能为空"),
	TRAVEL_ID_PARAMETER(53008,"该出差id不存在"),
	TRAVEL_STAFF_BADGE_PARAMETER(53009,"员工号不存在"),
	TRAVEL_STAFF_BADGE_ERROE(53009,"员工号不能为空"),
	TRAVEL_ERROE(53010,"出差数据查询失败"),



	/**
	 * 请假的信息
	 * @param code
	 * @param message
	 */
	ASK_LEAVE_PARAMETER__ERROR(55001,"请假的参数不能为空"),
	ASK_LEAVE_COUNT__ERROR(55002,"请假时长只允许为数字"),
	ASK_LEAVE_DESC_ERROR(55003,"请假原因不能为空"),
	ASK_LEAVE_TIME_ERROR(55004,"开始时间不能小于结束时间"),

	ASK_LEAVE_PHOTO_ERROR(55005,"附件图片不能为空"),
	ASK_LEAVE_VACATE_COUNT_ERROR(55006,"请假时长不能为空"),
	ASK_LEAVE_TYPE_ERROR(55007,"请假类型不能为空"),
	ASK_LEAVE_CLASS_NAME_NULL(55008,"班次名称不能为空"),
	ASK_LEAVE_CLASS_NAME_ERROR(55009,"班次名称最长为50个字符"),
	ASK_LEAVE_STAFF_BADGE_NULL(55010,"员工号不能为空"),
	ASK_LEAVE_STAFF_BADGE_ERROR(55011,"员工号不存在"),
	ASK_LEAVE_ID_PARAMETER(55012,"该请假id不存在"),
	ASK_UNIT_NULL(55013,"该请假时长单位不能为空"),




	/**
	 * app主题管理相关异常
	 * @param code
	 * @param message
	 */
	APP_SUBJECT_ID_NULL(56001,"主题id不能为空"),
	APP_SUBJECT_ID_ERROR(56002,"主题id不存在"),
	APP_SUBJECT_NAME_NULL(56003,"主题名不能为空"),
	APP_SUBJECT_NULL(56003,"主题为空"),
	APP_SUBJECT_NAME_ERROR(56004,"主题名格式错误，长度应在3至30个字符"),
	APP_SUBJECT_PICTURE_NULL(56005,"标题图片不能为空"),
	APP_SUBJECT_TEXT_NULL(56005,"文本内容不能为空"),
	APP_SUBJECT_FLAG_ERROR(56006,"该主题状态无法执行此操作"),
	APP_SUBJECT_CONTENT_NULL(56007,"主题内容不能为空"),
	APP_SUBJECT_PIC_NULL(56008,"封面图片不能为空"),
	APP_SBUJECT_TOP_ERROR(56010,"已为列表展示第一个，无法上移"),
	APP_TOP_NUMBER_ERROR(56011,"存在两个及以上置顶主题！请先取消置顶"),
	APP_ORDER_LAST_ERROR(56012,"已在最底部，无法下移"),
	APP_SUBJECT_BATCH_NULL(56013,"尚未勾选需操作的主题"),
	APP_SBUJECT_UPPER_ERROR(56014,"该主题已置顶，无法移动"),
	APP_BANNER_ERROR(56021,"轮播图片已达上限"),
	APP_BANNER_TOP(56022,"该图片为第一个，无法上移"),
	APP_BANNER_LAST(56023,"该图片为最后一个，无法下移"),
	APP_MODULE_URL_NULL(56025,"跳转地址不能为空"),
	APP_PICTURE_NULL(56026,"轮播图片不能为空"),
	APP_PARK_NULL(56031,"所属园区不能为空"),
	APP_MODULE_NAME_ERROR(56035,"模块名只允许汉字、字母与数字的组合，最长为30个字符"),
	APP_MODULE_NAME_NULL(56037,"模块名不能为空"),
	APP_AGREE_NAME_ERROR(56039,"协议名只允许汉字、字母与数字的组合，最长为30个字符"),
	APP_AGREE_NAME_NULL(56041,"协议名不能为空"),
	APP_MODULE_NAME_EXIST(56043,"模块名已存在"),
	APP_SUBJECT_URL_ERROR(56058,"链接格式不正确"),


	/*
	 * 加班管理相关异常
	 */
	OVER_TIME_NULL(57001,"加班的参数不能为空"),
	OVER_TIME_EXTRA_WORK_CLASS_CODE_NULL(57001,"加班的班别编码不能为空"),
	OVER_TIME_EXTRA_WORK_TYPE_NULL(57002,"加班的加班类型不能为空"),
	OVER_TIME_IS_TRAVEL_WORK_NULL(57003,"是否出差不能为空"),
	OVER_TIME_EXTA_WORK_COUNT_NULL(57004,"加班时长不能为空"),
	OVER_TIME_STAFF_BADGE_PARAMETER(57005,"员工号不存在"),
	OVER_TIME_ID_PARAMETER(57006,"该请假id不存在"),
	OVER_TIME_STAFF_BADGE_ERROR(57007,"员工号不能为空"),
	OVER_TIME_WORK_DATE_ERROR(57008,"加班日期不能为空"),
	OVER_TIME_YEAR_MONTH_DAY_PARAMETER(57008,"时间类型为yyyy-MM-dd"),



	/**
	 * 补卡管理相关异常
	 */
	REPLACE_NULL(58001,"补卡的参数不能为空"),
	REPLACE_PATCH_MONTH_NULL(58002,"考勤月份不能为空"),
	REPLACE_PATCH_DATE_NULL(58003,"补卡开始时间不能为空"),
	REPLACE_PATCH_REASON_NULL(58004,"补卡原因不能为空"),
	REPLACE_PHOTO_NULL(58005,"补卡附件图片不能为空"),
	REPLACE_SECOND_ENTER_NULL(58005,"补卡2入不能为空"),
	REPLACE_SECOND_OUT_NULL(58006,"补卡2出不能为空"),
	REPLACE_SECOND_ENTER_COVER_NULL(58007,"补卡2入是否跨天不能为空"),
	REPLACE_SECOND_OUT_COVER_NULL(58008,"补卡2出是否跨天不能为空"),

	REPLACE_FOURTH_ENTER_NULL(58010,"补卡4入不能为空"),
	REPLACE_FOURTH_OUT_NULL(58011,"补卡4出不能为空"),
	REPLACE_FOURTH_ENTER_COVER_NULL(58012,"补卡4入是否跨天不能为空"),
	REPLACE_FOURTH_OUT_COVER_NULL(58013,"补卡4出是否跨天不能为空"),

	REPLACE_FIFTH_ENTER_NULL(58014,"补卡5入不能为空"),
	REPLACE_FIFTH_OUT_NULL(58015,"补卡5出不能为空"),
	REPLACE_FIFTH_ENTER_COVER_NULL(58016,"补卡5入是否跨天不能为空"),
	REPLACE_FIFTH_OUT_COVER_NULL(58017,"补卡5出是否跨天不能为空"),

	REPLACE_STAFF_BADGE_PARAMETER(58018,"员工号不存在"),
	REPLACE_STAFF_BADGE_NULL(58019,"员工号不能为空"),
	REPLACE_YEAR_MONTH_PARAMETER(58020,"时间类型为yyyy-MM"),
	REPLACE_YEAR_MONTH_DAY_PARAMETER(58021,"时间类型为yyyy-MM-dd"),
	REPLACE_YEAR_MONTH_NULL(58022,"时间不能为空"),
	REPLACE_BADGE_ERROR(58023,"该员工不能重复补卡"),

	/**
	 * 人员设备注册相关异常
	 */
	DIVICE_REGISTER_NULL(59001,"设备信息为空"),
	DIVICE_NAME_ERROR(59002,"设备名为空"),
	DIVICE_NO_ERROR(59003,"设备编号为空"),
	DIVICE_OS_TYPE_ERROR(59004,"设备系统类型为空"),
	DIVICE_PUSH_ID_ERROR(59005,"设备消息推送标识为空"),


	NOT_STAFF(6001, "未找到员工信息"),
	VEHICLE_NOT_PART(6000, "C级以下不能申请入园");
	ExceptionTypeEnum(int code, String message) {
        this.code = code;
        this.message = message;
    }

    private final Integer code;

    private final String message;

    public Integer getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
