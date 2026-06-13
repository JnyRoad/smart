package com.tce.smart.platform.core.vo;

import java.util.Date;

import javax.validation.constraints.NotBlank;

import lombok.Data;

@Data
public class StaffListVO {

	/**
	*
	*/
	private String id;
	/**
	 * 员工所属园区id
	 */
	private Integer parkId;

	/**
	 * 中心
	 */
	private String depAbbr;
	/**
	 * 员工姓名
	 */
	private String name;

	/**
	 * 员工工号
	 */
	private String badge;

	/**
	 * 岗位ID
	 */
	private String jobId;
	/**
	 * 岗位名称
	 */
	private String jobName;
	/**
	 * buname
	 */
	private String compName;
	/**
	 * 部门名称
	 */
	private String depName;
	/**
	 * 职层名称
	 */
	private String jcheName;

	/**
	 * 电话
	 */
	private String phone;

	/**
	 * 员工状态 0-已离职 1-在职
	 */
	private Integer status;
	/**
	 * 入职时间
	 */
	private Date createTime;


	 /**
     * 住宿状态  0-未住宿  1-内宿  2-外宿
     */
    private Integer  dormitoryStatus;



	/**
	 * 通关权限策略
	 */
	private String deviceAuth;

	/**
	 * app通关权限策略
	 */
	private String appAuth;


	/**
	 * 人脸照片id
	 */
	private String facePicId;

	/**
	 * 所属园区名称
	 */
	private String parkName;

	/**
	 * bu
	 */
	private String compId;

	private Integer seqId;

	/**
	 * 身份证
	 */
	private String certno;
}
