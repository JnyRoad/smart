package com.tce.smart.app.vo.fore;

import java.util.Date;

import lombok.Data;

@Data
public class EmployeeInfoVo {

	/**
	 * 员工工号
	 */
	private String employeeId;

	/**
	 * 员工名称
	 */
	private String employeeName;

	/**
	 * 身份证号
	 */
	private String identification;

	/**
	 * 员工照片
	 */
	private String employeePhoto;

	/**
	 * bu名称
	 */
	private String buName;
	/**
	 * 部门名称
	 */
	private String deptName;

	/**
	 * 岗位名称
	 */
	private String jobName;

	/**
	 * 员工职层
	 */
	private String jobLeve;

	/**
	 * 员工福利层次
	 */
	private String jobLevelflag;

	/**
	 * 员工手机号
	 */
	private String mobile;


	private String email;

	private String relation;

	private String emergencyName;

	private String emergencyPhone;

	/**
	 * 入职时间
	 */
	private Date entryDate;

	private Integer empType;

	private String empTypeDes;

	private String parkName;

}
