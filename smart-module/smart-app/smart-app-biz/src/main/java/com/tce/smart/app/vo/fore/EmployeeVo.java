package com.tce.smart.app.vo.fore;

import java.util.Date;

import lombok.Data;

/**
 * 员工基本信息
 * @author qipei
 *
 */
@Data
public class EmployeeVo {

	/**
	 * 员工工号
	 */
	private String employeeBadge;

	/**
	 * 员工名称
	 */
	private String employeeName;
	/**
	 * 员工照片
	 */
	private String employeePhoto;

	/**
	 * 员工性别
	 */
	private Integer employeeSex;

	/**
	 * 员工身份证号
	 */
	private String employeeCardNo;

	/**
	 * 员工手机号
	 */
	private String mobile;

	/**
	 * bu名称
	 */
	private String buName;
	/**
	 * 部门名称
	 */
	private String deptName;

	/**
	 * 入职时间
	 */
	private Date entryDate;

	/**
	 * 内宿申请状态
	 */
	private Integer applyState;

	/**
	 * 内宿申请状态描述
	 */
	private String applyStateDesc;

	/**
	 * 宿舍状态
	 */
	private String dormitoryState;
	/**
	 * 宿舍状态描述
	 */
	private String dormitoryStateDesc;

	/**
	 * 车辆是否添加
	 */
	private String vehicleState;

	/**
	 * 描述
	 */
	private String vehicleStateDesc;

	/**
	 * 员工状态
	 */
	private Integer status;

	/**
	 * 员工状态描述
	 */
	private String statusDes;

	/**
	 * 员工类型
	 */
	private Integer empType;

	/**
	 * 员工类型描述
	 */
	private String empTypeDes;

	private String jobName;

	private String jcheName;

	private String parkName;

	/**
	 * 员工性质
	 */
	private String empAttribute;

	/**
	 * 福利
	 */
	private String welfareLevel;

	/**
	 * 是否为保安,0-是,1-否
	 */
	private Integer isSecurityGuard;
}
