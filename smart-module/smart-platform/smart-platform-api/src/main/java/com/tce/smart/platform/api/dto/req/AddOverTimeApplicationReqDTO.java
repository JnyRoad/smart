package com.tce.smart.platform.api.dto.req;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 添加加班申请
 *
 * @author 梁圆
 * @date 2019-05-05 18:19:00
 */
@Data
public class AddOverTimeApplicationReqDTO implements Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 * 员工号
	 */
	private String staffBadge;
	/**
	 * 加班日期
	 */
	private String extraworkDate;
	/**
	 *班别编码
	 */
	private String extraworkClassCode;
	/**
	 *加班类型
	 */
	private String extraworkType;
	/**
	 *是否出差
	 */
	private String isTravelExtrawork;
	/**
	 *加班时长
	 */
	private String extraworkCount;

	/**
	 * 原因
	 */
	private String extraworkDesc;

	/**
	 * 2入
	 */
	private String startDate2;
	/**
	 * 2出
	 */
	private String endDate2;
	/**
	 * 4入
	 */
	private String startDate4;
	/**
	 * 4出
	 */
	private String endDate4;
	/**
	 * 5入
	 */
	private String startDate5;
	/**
	 * 5出
	 */
	private String endDate5;

}
