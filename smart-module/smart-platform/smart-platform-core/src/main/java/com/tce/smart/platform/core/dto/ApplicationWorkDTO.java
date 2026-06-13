package com.tce.smart.platform.core.dto;

import lombok.Data;

@Data
public class ApplicationWorkDTO {

	private Integer id;

	 private String startTime;
	    /**
	   *
	   */
	    private String endTime;
	    /**
	   * 公司名称
	   */
	    private String company;
	    /**
	   * 职位
	   */
	    private String jobName;

	    /**
	   * 负责人名称
	   */
	    private String personLiable;
	    /**
	   * 负责人电话
	   */
	    private String phone;
	    /**
	   * 应聘者ID
	   */
	    private String applicationId;
}
