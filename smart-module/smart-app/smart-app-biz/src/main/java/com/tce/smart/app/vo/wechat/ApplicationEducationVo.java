package com.tce.smart.app.vo.wechat;


import lombok.Data;

/**
 * 教育经验
 * @author qipei
 *
 */
@Data
public class ApplicationEducationVo {


	    private String educationHisId;
	    /**
	   * 教育开始时间
	   */
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

	    private String degreeDesc;

	    private String educationDesc;


}
