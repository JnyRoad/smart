package com.tce.smart.platform.core.vo;


import java.util.Date;

import lombok.Data;

/**
 * 查询为住宿的员工
 * @author tce
 *
 */
@Data
public class StaffNODormitoryVO {


	/**
	*
	*/
	private String id;

	/**
	 * 员工姓名
	 */
	private String name;

	/**
	 * 员工工号
	 */
	private String badge;

	private String jobName;

	private String compId;

    private String compName;

    private String depName;

	private String jcheName;

	private String certno;

	private String phone;

	private Date createTime;

	private String welfareLevel;

	private String parkName;



}
