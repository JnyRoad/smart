package com.tce.smart.app.vo.fore;

import java.util.Date;

import com.tce.smart.common.core.vo.BaseVO;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 请假详情信息VO
 *
 * @author ly
 * @date 2019-05-10 16:11:13
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class EmployeeVacateDetailVo extends BaseVO {


	/**
	 * 员工id
	 */
	private String employeeId;
	/**
	 * 员工姓名
	 */
	private String employeeBadge;
	/**
	 * 员工姓名
	 */
	private String employeeName;
	/**
	 * BU
	 */
	private String buName;
    /**
     * 部门名称
     */
	private String deptName;

	/**
	 * 职位
	 */
	private String jobName;
	/**
	 * 请假类型描述
	 */
	private String vacateTypeDesc;

	/**
	 * 开始时间
	 */
	private Date startDate;
	/**
	 * 结束时间
	 */
	private Date endDate;

	/**
	 * 时长
	 */
	private String vacateCount;
	/**
	 * 请假原因
	 */
	private String vacateDesc;

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
	/**
	 * 附件图片
	 */
	private String photo;

}
