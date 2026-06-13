package com.tce.smart.platform.api.dto.req;

import lombok.Data;

import java.io.Serializable;

/**
 * 补卡查询参数
 *
 * @author 梁圆
 * @date 2019-05-08 18:18:30
 */
@Data
public class SearchAttendanceReqDTO implements Serializable {
	private static final long serialVersionUID = -5986873005537276225L;

	/**
	 * 员工工号
	 */
	private String staffBadge;
	/**
	 * 年月
	 */
	private String queryDay;
}
