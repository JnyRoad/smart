package com.tce.smart.tool.enums;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 调查问卷，问题类型
 * @author 齐佩
 *
 */
public enum QuestionTypeEnum {


	/**
	 * 单选
	 */
	SINGLE_SELECT(0,"单选",0),

	/**
	 *多选
	 */
	MULTIPLE_SELECT(1, "多选",1),

	/**
	 * 问答
	 */
	ANSWER(2, "问答",2);



	private final Integer code;
	private final String desc;
	private final Integer order;

	QuestionTypeEnum(Integer code, String desc, Integer order) {
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
		for (QuestionTypeEnum t : QuestionTypeEnum.values()) {
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
