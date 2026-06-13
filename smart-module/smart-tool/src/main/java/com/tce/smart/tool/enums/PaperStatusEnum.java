package com.tce.smart.tool.enums;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 调查问卷的状态
 * @author 齐佩
 *
 */
public enum PaperStatusEnum {

	/**
	 * 未开始
	 */
	NO_START(0,"未开始",0),

	/**
	 *进行中
	 */
	STARTING(1, "进行中",1),

	/**
	 * 已结束
	 */
	END(2, "已结束",2);



	private final Integer code;
	private final String desc;
	private final Integer order;

	PaperStatusEnum(Integer code, String desc, Integer order) {
		this.code = code;
		this.desc = desc;
		this.order = order;
	}

	/**
	 * 获取App页面状态列表
	 *
	 * @return
	 */
	public static List<Map<String, Object>> getTypeList() {
		List<Map<String, Object>> list = new ArrayList<>();
		for (PaperStatusEnum t : PaperStatusEnum.values()) {
			if (Objects.nonNull(t.code)) {
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
