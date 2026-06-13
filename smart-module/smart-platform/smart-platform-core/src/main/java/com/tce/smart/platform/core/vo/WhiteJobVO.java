package com.tce.smart.platform.core.vo;

import java.util.Date;

import lombok.Data;

@Data
public class WhiteJobVO {

private Integer id;


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
	 * 创建时间
	 */
	private Date createTime;

	/**
	 * 园区ID
	 */
	private String parkName;
}
