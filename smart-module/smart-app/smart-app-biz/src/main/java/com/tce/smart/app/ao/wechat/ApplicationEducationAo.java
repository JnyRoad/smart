package com.tce.smart.app.ao.wechat;

import lombok.Data;

/**
 * 应聘者提交教育经验
 * @author qipei
 *
 */
@Data
public class ApplicationEducationAo {


	private String schoolName;


	private String major;


	private String education;


	private String degree;


	private String startTime;


	private String endTime;

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
