package com.tce.smart.tool.enums;

import com.tce.smart.common.core.util.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/***
 * description: 园区消息通知开关类型枚举<br>
 * date: 2019/11/22 8:57 <br>
 * author: mckaywu <br>
 * version: 1.0 <br>
 */
public enum ParkNoticeTypeEnum {
	VISITOR_ARRIVE("visitor_arrive", "来访到时通知"),
	VISIT_APPLY("visit_apply", "预约申请通知"),
	VISIT_APPLY_SUCCESS("visit_apply_success", "预约成功通知"),
	VISIT_APPLY_SUCCESS_NOTICE_HOST("visit_apply_success_notice_host", "预约成功通知被访人"),
	VISIT_APPROVE_BY_MANAGER("visit_approve_by_manager ", "主管审批访客通知"),
	VISIT_APPLY_FAILD("visit_apply_faild", "预约失败通知"),
	VISITOR_ARRIVE_REAL("visitor_arrive_real", "访客到访通知"),
	VISIT_OVERTIME_STAY("visit_overtime_stay", "访客超时未离通知"),
	SMS_SEND_FAILD("sms_send_faild", "短信失败反馈通知"),
	VISITOR_LEAVE("visitor_leave", "访客离开通知");

	private final String code;

	private final String desc;

	ParkNoticeTypeEnum(String code, String desc) {
		this.code = code;
		this.desc = desc;
	}

	public static ParkNoticeTypeEnum desc(String code) {
		if (StringUtils.isNotEmpty(code)) {
			for (ParkNoticeTypeEnum enmuType : ParkNoticeTypeEnum.values()) {
				if (enmuType.code.equals(code)) {
					return enmuType;
				}
			}
		}
		return null;
	}

	public static String code(String desc) {
		if (StringUtils.isNotEmpty(desc)) {
			for (ParkNoticeTypeEnum typeEnmu : ParkNoticeTypeEnum.values()) {
				if (typeEnmu.desc.equals(desc)) {
					return typeEnmu.code;
				}
			}
		}
		return null;
	}

	public static List<Map<String, String>> list() {
		List<Map<String, String>> list = new ArrayList<>();
		for (ParkNoticeTypeEnum t : ParkNoticeTypeEnum.values()) {
			if (t.code != null) {
				Map<String, String> map = new HashMap<>();
				map.put("code", t.code);
				map.put("desc", t.desc);
				list.add(map);
			}
		}
		return list;
	}

	public String getCode() {
		return code;
	}

	public String getDesc() {
		return desc;
	}
}
