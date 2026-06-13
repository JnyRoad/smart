package com.tce.smart.platform.core.dto;

import lombok.Data;

@Data
public class ApplicationEducationDTO {

	private Integer id;

	 private String startTime;
	    /**
	   * 教育结束时间
	   */
	    private String endTime;
	    /**
	   * 学校名称
	   */
	    private String schoolName;
	    /**
	   * 专业
	   */
	    private String major;
	    /**
	   * 学历
	   */
	    private String education;

	    /**
	     * 学位
	     */
	    private String degree;

	    /**
	   * 应聘者ID
	   */
	    private String applicationId;

	    /**
	     * 毕业类型
	     */
	    private Integer gradType;

	    /**
	     * 是否最高学历
	     */
	    private Integer isHighEduType;

	    /**
	     * 是否最高学位
	     */
	    private Integer isHighDegreeType;
}
