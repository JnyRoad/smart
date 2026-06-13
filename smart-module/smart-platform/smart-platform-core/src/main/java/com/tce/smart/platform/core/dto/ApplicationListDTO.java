package com.tce.smart.platform.core.dto;

import lombok.Data;

/**
 * app查询应聘条件
 */
@Data
public class ApplicationListDTO {


	/**
	 * 岗位id
	 */
	private String jobId;

	/**
	 * 应聘状态
	 */
	private Integer applyState;

	/**
	 * 筛选条件-起始年龄
	 */
	private Integer ageStart;

	//筛选条件-结束年龄
	private Integer ageEnd;

	//筛选条件-性别(0-男,1-女)
	private Integer gender;

	//年龄排序(0-升序,1-降序)
	private Integer ageOrder;

	//投递时间排序(0-升序,1-降序)
	private Integer deliverOrder;

	//应聘者名称
	private String applicantName;

	//应聘者联系电话
	private String applicantMobile;
	//园区id
	private Integer parkId;

	/**
	 * 员工工号
	 */
	private String staffBadge;

	/**
	 * 查看权限范围内得职层id集合
	 */
	/*private List<String> staffRecruitAuthLeve;*/
}
