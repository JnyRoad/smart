package com.tce.smart.platform.core.dto;

import lombok.Data;

@Data
public class ApplicationFamilyDTO {

		private Integer id;

		private String relation;

	    /**
	     * 亲属姓名
	     */
	    private String name;

	    /**
	     *亲属性别
	     */
	    private Integer sex;

	    /**
	     * 亲属生日
	     */
	    private String birth;

	    /**
	     * 亲属所在公司
	     */
	    private String company;

	    /**
	     * 担任职务
	     */
	    private String job;

	    /**
	     * 应聘者id
	     */
	    private String applicationId;

	    /**
	     * 親屬電話
	     */
	    private String phone;
}
