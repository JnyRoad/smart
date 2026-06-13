package com.tce.smart.platform.core.dto;


import com.tce.smart.platform.core.entity.*;
import lombok.Data;

/**
 * 后台应聘人员查询条件
 */
@Data
public class ApplicationDTO extends SmtApplication {
	/**
	 * 岗位id
	 */
	private String jobId;
	/**
	 * 开始年龄
	 */
	private Integer startAge;
	/**
	 * 结束年龄
	 */
	private  Integer endAge;
	/**
	 * 开始时间
	 */
	private String startTime;
	/**
	 * 结束时间
	 */
	private String endTime;
	/**
	 * 简历状态
	 */
	private Integer status;
	/**
	 * 园区ID
	 */
	private Integer parkId;
	/**
	 * buId
	 */
	private String compId;
	/**
	 * 部门id
	 */
	private String depId;
	/**
	 * 岗位id
	 */
	private String jobName;
}
