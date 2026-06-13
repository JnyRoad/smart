package com.tce.smart.common.core.constant.enums;

/**
 * 定时任务
 *
 * @author mkwu
 * @date 2019-08-07
 */
public enum TimerTaskEnum {
	MOVE_ADMIN_DATA("data_move_task_admin", "data_move_task_admin", "admin数据清理定时任务");
	private String type;
	private String key;
	private String desc;

	TimerTaskEnum(String key, String type, String desc) {
		this.type = type;
		this.key = key;
		this.desc = desc;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getDesc() {
		return desc;
	}

	public void setDesc(String desc) {
		this.desc = desc;
	}
}
