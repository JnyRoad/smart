package com.tce.smart.app.emun;

import java.util.Objects;

/**
 * 简历塞选状态枚举
 *
 * @author mckaywu
 * @date 2019-05-23 16:45:44
 */
public enum ApplicationOPType {
	/**
	 * 面试邀请
	 */
	INVITE(1, "面试邀请"),

	/**
	 * 拒绝
	 */
	REFUSE(2, "拒绝"),

	/**
	 * 复试
	 */
	RE_FACE(3, "复试"),

	/**
	 * 加入人才库
	 */
	STORE(4, "加入人才库"),

	/**
	 * 重新邀请
	 */
	RE_INVITE(5, "重新邀请"),

	/**
	 * 录取
	 */
	ENROLL(6, "录取"),

	/**
	 * 入职
	 */
	ENTRY(7, "入职");

	private final Integer type;
	private final String desc;

	ApplicationOPType(Integer type, String desc) {
		this.type = type;
		this.desc = desc;
	}

	public static ApplicationOPType type(Integer type) {
		if (Objects.nonNull(type)) {
			for (ApplicationOPType t : ApplicationOPType.values()) {
				if (null != t.type && t.type.equals(type)) {
					return t;
				}
			}
		}
		return null;
	}

	public Integer getType() {
		return type;
	}

	public String getDesc() {
		return desc;
	}
}
