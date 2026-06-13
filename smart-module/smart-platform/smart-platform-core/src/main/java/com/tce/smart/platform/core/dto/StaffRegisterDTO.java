package com.tce.smart.platform.core.dto;

import java.util.List;

import com.tce.smart.platform.core.entity.SmtStaffRegister;

import lombok.Data;

@Data
public class StaffRegisterDTO {

	private List<SmtStaffRegister> staffList;
	/**
	 * 岗位ID
	 */
	private String jobId;
	/**
	 * 岗位名称
	 */
	private String jobName;
	/**
	 * BUId
	 */
	private String compId;
	/**
	 * buname
	 */
	private String compName;
	/**
	 * 部门ID
	 */
	private String depId;
	/**
	 * 部门名称
	 */
	private String depName;
	/**
	 * 职层ID
	 */
	private String jcheId;
	/**
	 * 职层名称
	 */
	private String jcheName;

	private String flcc;

	private Integer parkId;
}
