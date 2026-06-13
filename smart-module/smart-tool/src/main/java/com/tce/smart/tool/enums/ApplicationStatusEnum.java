package com.tce.smart.tool.enums;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/***
 * 齐佩
 * 应聘职位的状态
 */
public enum ApplicationStatusEnum {

	/**
	 * 编辑中
	 */
	EDIT_ING(-1, "编辑中",-1),

	/**
	 * 已投递
	 */
	DELIVER_DONE(0, "已投递",1),

	/**
	 * 已拒绝
	 */
	REFUSE_DONE(1, "已拒绝",6),

	/**
	 * 已邀请
	 */
	INVITE_DONE(2, "已邀请",2),

	/**
	 * 待入职
	 */
	ENTRY_TO_DO(3, "待入职",4),

	/**
	 * 待复试
	 */
	REFACE_TO_DO(4, "待复试",3),

	/**
	 * 已入职
	 */
	ENTRY_DONE(5, "已入职",5),

	/**
	 * 已入库
	 */
	STORE_DONE(6, "已入库",7);

	private final Integer code;
	private final String desc;
	private final Integer order;

	ApplicationStatusEnum(Integer code, String desc, Integer order) {
		this.code = code;
		this.desc = desc;
		this.order = order;
	}

	/**
	 * 获取App页面状态列表
	 *
	 * @return
	 */
	public static List<Map<String, Object>> getAppDispaylist() {
		List<Map<String, Object>> list = new ArrayList<>();
		for (ApplicationStatusEnum t : ApplicationStatusEnum.values()) {
			if (Objects.nonNull(t.code)) {
				//app显示过滤掉编辑状态
				if(EDIT_ING.equals(t)) {
					continue;
				}

				Map<String, Object> map = new HashMap<>();
				map.put("code", t.code);
				map.put("desc", t.desc);
				map.put("order", t.order);
				list.add(map);


			}
		}
		return list;
	}

	public Integer getCode() {
		return code;
	}

	public String getDesc() {
		return desc;
	}

	public Integer getOrder() {
		return order;
	}
}
