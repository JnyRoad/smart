package com.tce.smart.platform.api.dto.req;

import lombok.Data;

import java.io.Serializable;

/**
 * 添加请假申请
 *
 * @author 梁圆
 * @date 2019-05-05 18:19:00
 */
@Data
public class AddAskLeavelApplicationReqDTO implements Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 * 请假类型
	 */
	private String vacateType;
	/**
	 * 员工号
	 */
	private String staffBadge;

	/**
	 * 请假开始时间
	 */
	private String startDate;
	/**
	 * 请假结束时间
	 */
	private String endDate;
	/**
	 *请假时长单位
	 */
	private String unit;
	/**
	 *请假时长
	 */
	private String vacateCount;

	/**
	 * 原因
	 */
	private String vacateDesc;
	/**
	 * 附件图片base位
	 */
	private String photo;
	/**
	 * 班次
	 */
	private String className;
	/**
	 * 2入
	 */
	private String secondEnter;
	/**
	 * 2出
	 */
	private String secondOut;
	/**
	 * 4入
	 */
	private String fourthEnter;
	/**
	 * 4出
	 */
	private String fourthOut;
	/**
	 * 5入
	 */
	private String fifthEnter;
	/**
	 * 5出
	 */
	private String fifthOut;

}
