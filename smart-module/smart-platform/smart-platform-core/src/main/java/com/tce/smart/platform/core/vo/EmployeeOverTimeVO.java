package com.tce.smart.platform.core.vo;

import java.util.Date;

import com.baomidou.mybatisplus.extension.activerecord.Model;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 请假返回实体类
 *
 * @author 梁圆
 * @date 2019-04-13 18:19:00
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class EmployeeOverTimeVO extends Model<EmployeeOverTimeVO> {
	private static final long serialVersionUID = 1L;


	/**
	 * 员工id
	 */
	private String employeeId;
	/**
	 * 员工工号
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
	 * 加班时间
	 */
	private String extraworkDate;
	/**
	 * 加班类型描述
	 */
	private String extraworkTypeDesc;
	/**
	 * 加班班别描述
	 */
	private String extraworkClassDesc;
	/**
	 * 时长
	 */
	private String extraworkCount;
	/**
	 * 是否出差加班
	 */
	private String isTravelExtrawork;

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

	/**
	 * 流程id
	 */
	private String processId;

	/**
	 * 创建时间
	 */
	private Date createDate;


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
	 * 班次名称
	 */
	private String classDesc;

}
