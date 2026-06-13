package com.tce.smart.platform.api.dto.resp;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 请假申请列表返回值
 *
 * @author 梁圆
 * @date 2019-04-13 18:26:36
 */
@Data
public class SearchAskLeaveApplicationRespDTO implements Serializable {
	private static final long serialVersionUID = 3291746659395226408L;

	/**
	 * 申请id
	 */
	private Integer recordId;
	/**
	 * 员工姓名
	 */
	private String staffName;

	/**
	 * 记录类型备注
	 */
	private String recordDesc;

	/**
	 * 流程id
	 */

	private String processId;

	/**
	 * 申请开始时间
	 */
	private Date startDate;
	/**
	 * 申请结束时间
	 */
	private Date endDate;

	/**
	 * 申请记录时间
	 */
	private Date recordDate;
	/**
	 * 申请时长
	 */
	private String vacateCount;
	/**
	 * 申请时长单位
	 */
	private String unit;
	/**
	 * 请假类型
	 */
	private String type;

}
