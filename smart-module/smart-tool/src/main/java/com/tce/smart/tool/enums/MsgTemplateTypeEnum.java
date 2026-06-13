package com.tce.smart.tool.enums;

import com.tce.smart.common.core.util.StringUtils;

import java.util.Objects;

/**
 * 消息模板类型枚举
 * 模板分类 1-访客预约通知，2-招聘通知，3-离职通知  4-手机验证通知，5-邮件通知
 *
 * @author mingkai.wu
 * @date 2019-05-15 20:12:18
 */
public enum MsgTemplateTypeEnum {
	VISITOR(1, "访客预约通知"),
	RECRUIT(2, "招聘通知"),
	DIMISSION(3, "离职通知"),
	SMS_CODE(4, "手机验证通知"),
	EMAIL(5, "邮件通知"),
	ROOM_CODE(6, "住宿通知"),
	VACATE_CODE(7, "请假通知"),
	REST_CODE(8, "调休通知"),
	EXTRAWORK_CODE(9, "加班通知"),
	ATTENDANCE_CODE(10, "考勤通知"),
	TRAVEL_CODE(11, "出差通知"),
	BADGE_CODE(12, "厂牌通知"),
	APPEAL_CODE(13, "申诉通知"),
	ARTICLES_CODE(14, "物品放行通知"),
	SIGN_CODE(15, "签收通知"),
	VISITOR_PROXY_CODE(16, "访客审批代理人通知"),
	PARK_WARRANTY(17, "园区报修通知");

	private final Integer code;

	private final String desc;

	MsgTemplateTypeEnum(Integer code, String desc) {
		this.code = code;
		this.desc = desc;
	}

	public static MsgTemplateTypeEnum desc(Integer code) {
		if (Objects.nonNull(code)) {
			for (MsgTemplateTypeEnum enmuType : MsgTemplateTypeEnum.values()) {
				if (enmuType.code.equals(code)) {
					return enmuType;
				}
			}
		}
		return null;
	}

	public static Integer code(String desc) {
		if (StringUtils.isNotEmpty(desc)) {
			for (MsgTemplateTypeEnum typeEnmu : MsgTemplateTypeEnum.values()) {
				if (typeEnmu.desc.equals(desc)) {
					return typeEnmu.code;
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
