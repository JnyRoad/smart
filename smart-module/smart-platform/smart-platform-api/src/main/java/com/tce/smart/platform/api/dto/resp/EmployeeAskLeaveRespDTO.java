package com.tce.smart.platform.api.dto.resp;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 请假返回实体类
 *
 * @author 梁圆
 * @date 2019-04-13 18:19:00
 */
@Data
public class EmployeeAskLeaveRespDTO implements Serializable {
	private static final long serialVersionUID = -575617174109498405L;


	/**
	 * 员工id
	 */
	private Long employeeId;
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
	 * 请假类型
	 */
	private Integer vacateType;
	/**
	 * 请假类型描述
	 */
	private String vacateTypeDesc;
	/**
	 * 时长单位
	 */
	private String unit;
	/**
	 * 时长
	 */
	private String vacateCount;
	/**
	 * 开始时间
	 */
	private Date startDate;
	/**
	 * 结束时间
	 */
	private Date endDate;
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
