package com.tce.smart.platform.api.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 应聘者家庭成员
 * @author qipei
 *
 */
@Data
public class SmtApplicationFamilyDTO implements Serializable {
	private static final long serialVersionUID = 8419966047525175894L;
	/**
	   *
	   */
	    private Integer id;

	    /**
	     * 亲属关系
	     */
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
	    private Long applicationId;

	    /**
	     * 親屬電話
	     */
	    private String phone;

}
