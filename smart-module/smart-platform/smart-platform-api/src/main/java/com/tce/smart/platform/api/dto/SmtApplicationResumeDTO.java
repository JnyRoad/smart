package com.tce.smart.platform.api.dto;


import lombok.Data;

import java.io.Serializable;

/**
 * 应聘者简历表
 * @author 齐佩
 *
 */
@Data
public class SmtApplicationResumeDTO implements Serializable {

	   private Integer id;

	   /**
	    * 应聘id
	    */
	   private Long applicationId;

	   /**
	    * 二进制简历
	    */
	   private byte[] resume;

	   /**
	    * 简历名称
	    */
	   private String resumeName;

}
