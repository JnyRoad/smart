package com.tce.smart.platform.core.vo;

import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

/**
 * 请假申请列表返回值
 *
 * @author 梁圆
 * @date 2019-04-13 18:26:36
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SearchAskLeaveApplicationVO extends Model<SearchAskLeaveApplicationVO> {
	private static final long serialVersionUID = 1L;

	/**
	 * 申请id
	 */
	private Integer recordId;
	/**
	 * 员工姓名
	 */
	private String staffBadge;

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

	/**
	 *  请假类型描述
	 */
	private String typeDesc;

	/**
	 * 请假原因
	 */
	private String cause;

	/**
	 * bu名称
	 */
	private String compName;

	/**
	 * 部门名称
	 */
	private String depName;

	/**
	 * 岗位名称
	 */
	private String jobName;

}
