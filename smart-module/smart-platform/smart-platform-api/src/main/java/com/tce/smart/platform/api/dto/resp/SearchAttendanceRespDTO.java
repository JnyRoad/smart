package com.tce.smart.platform.api.dto.resp;

import lombok.Data;

import java.io.Serializable;

/**
 * 出勤列表返回值
 *
 * @author 梁圆
 * @date 2019-04-13 18:26:36
 */
@Data
public class SearchAttendanceRespDTO implements Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 * 日期-号数
	 */
	private String monthlyDay;
	/**
	 * 完整日期信息
	 */
	private String fullDate;

	/**
	 *考勤状态 0-正常，1-异常
	 */
	private String checkState;
	/**
	 *考勤是否补卡 0-是，1-否
	 */
	private String isRecord;

}
