package com.tce.smart.common.core.constant.enums;

/**
 * @author 梁圆
 * @Description 所有业务异常的枚举
 * @date 2019年4月12日 下午5:04:51
 */
public enum ExceptionType {

    CHECK_SUCCESS(200, "验证成功"),
    SERVER_ERROR(500, "服务器异常"),
    SAVE_SUCCESS(200, "保存成功"),
    SAVE_FAILD(500, "保存失败"),

    DICT_EXISTS(51001, "字典数据重复"),

    /**
     * Excel
     */
    EXCEL_UPLOAD_FAIL_TITLE_ERROR(20001,"上传失败，模板不正确"),
    EXCEL_CONTENT_EMPTY(20002,"Excel中内容为空"),
    /**
     * 访客相关异常
     */
    VISITOR_NAME_EMPTY(54001, "访客姓名为空"),
    VISITOR_PHOTO_ID_EMPTY(54002, "访客照片id为空"),
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
    VISITOR_VEHICLE_PLATE_ERROR(54020, "访客车牌号不正确，输入正确车牌例：京12345"),
    VISITOR_STARTTIME_FORMAT_ERROR(54021, "访客预计来访开始时间格式不正确(yyyy-MM-dd HH:mm:ss)"),
    VISITOR_ENDTIME_FORMAT_ERROR(54022, "访客预计来访结束时间格式不正确(yyyy-MM-dd HH:mm:ss)"),
    VISITOR_ENDTIME_CANT_BEFORE_STARTTIME(54023, "结束时间不能早于开始时间"),

	VISITOR_RECEPTIONIST_ERROR(54024, "被访人不存在"),

	/**
	 * 抓拍人员的信息
	 */
	SNAP_PERSON_ID_EMPTY(54025, "抓拍人员的id不能为空"),
	SNAP_PERSON_EVENT_TYPE_EMPTY(54026, "事件类型不能为空"),
	SNAP_PERSON_EVENT_TYPE_ERROR(54027, "事件类型只能为1或者2"),
	SNAP_PHOTO_ID_EMPTY(54028, "抓拍人员的图片id不能为空"),
    SNAP_TIME_ERROR(54029, "抓拍时间格式不正确(yyyy-MM-dd HH:mm:ss)"),
	SNAP_DEVICE_ID_ERROR(54030, "抓拍的设备id不存在"),

    /**
     * 权限相关
     */
    ROLE_HAVE_NOT(54000, "未拥有该数据权限");


    ExceptionType(int code, String message) {
        this.code = code;
        this.message = message;
    }

    private Integer code;

    private String message;

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
