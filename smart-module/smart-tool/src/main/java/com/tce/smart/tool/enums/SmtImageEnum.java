package com.tce.smart.tool.enums;

import com.tce.smart.common.core.util.StringUtils;

import java.util.Objects;

/***
 * description: 图片业务类型 <br>
 * date: 2019/12/11 9:39 <br>
 * author: mckaywu <br>
 * version: 1.0 <br>
 */
public enum SmtImageEnum {
	/**
	 * 未知
	 */
	TYPE_UNKNOWN(0, "未知"),

	/**
	 * 员工人脸图片
	 */
	TYPE_STAFF_FACE(11, "员工人脸图片"),
	/**
	 * "访客人脸图片"
	 */
	TYPE_VISITOR_FACE(12, "访客人脸图片"),
	/**
	 * 应聘人脸图片
	 */
	TYPE_JOB_APPLY_FACE(13, "应聘人脸图片"),

	/**
	 * 员工身份证正面照
	 */
	TYPE_STAFF_IDCARD_FRONT(211, "员工身份证正面照"),
	/**
	 * 员工身份证反面照
	 */
	TYPE_STAFF_IDCARD_BACK(212, "员工身份证反面照"),

	/**
	 * 访客身份证正面照
	 */
	TYPE_VISITOR_IDCARD_FRONT(221, "访客身份证正面照"),
	/**
	 * 访客身份证反面照
	 */
	TYPE_VISITOR_IDCARD_BACK(222, "访客身份证反面照"),

	/**
	 * 应聘身份证正面照
	 */
	TYPE_JOB_APPLY_IDCARD_FRONT(231, "应聘身份证正面照"),
	/**
	 * 应聘身份证反面照
	 */
	TYPE_JOB_APPLY_IDCARD_BACK(232, "应聘身份证反面照"),

	/**
	 * 驾驶证正面照片
	 */
	TYPE_DRIVER_CARD_FRONT(31, "驾驶证正面照片"),
	/**
	 * 行驶证图片
	 */
	TYPE_DRIVING_CARD_FRONT(32, "行驶证图片"),

	/**
	 * 工资签单
	 */
	TYPE_SALAR_SIGN(41, "工资签单"),

	/**
	 * 请假证明附件
	 */
	TYPE_ASK_LEAVW_ATTACHMENT(51, "请假证明附件"),

	/**
	 * 补卡申请附件
	 */
	TYPE_REPLACE_APPLICATION(61, "补卡申请附件"),

	DORMITORY_REPAIRS(71,"宿舍报修故障图片"),

	STAFF_APPEAL(81,"员工申诉"),

	ARTICLES_RELEASE(91,"物品放行"),

	SECURITY_AREA_ORDER_CON(101,"保密区预约附件"),

	CAR_CAPTURE_IMG(102,"车辆抓拍图片"),

	SECURITY_AREA_PRO(701,"保密区协议内容"),

	ADMITTANCE_FACE_IMG(801,"入厂申请人脸图片"),

	ADMITTANCE_IDCARD_FRONT(802,"入厂申请身份证正面照");

	private final Integer code;

	private final String desc;

	SmtImageEnum(Integer code, String desc) {
		this.code = code;
		this.desc = desc;
	}

	public static SmtImageEnum getEnmu(Integer code) {
		if (Objects.nonNull(code)) {
			for (SmtImageEnum enmuTemp : SmtImageEnum.values()) {
				if (enmuTemp.code.equals(code)) {
					return enmuTemp;
				}
			}
		}
		return null;
	}

	public static String desc(Integer code) {
		SmtImageEnum enmuTemp = getEnmu(code);
		return enmuTemp == null ? null : getEnmu(code).desc;
	}

	public static Integer code(String desc) {
		if (StringUtils.isNotEmpty(desc)) {
			for (SmtImageEnum enmuTemp : SmtImageEnum.values()) {
				if (enmuTemp.desc.equals(desc)) {
					return enmuTemp.code;
				}
			}
		}
		return null;
	}

	public Integer getCode() {
		return code;
	}

	public String getDesc() {
		return desc;
	}

}
