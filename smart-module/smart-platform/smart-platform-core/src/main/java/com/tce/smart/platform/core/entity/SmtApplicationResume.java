package com.tce.smart.platform.core.entity;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 应聘者简历表
 * @author 齐佩
 *
 */
@Data
@TableName("smt_application_resume")
@EqualsAndHashCode(callSuper = true)
public class SmtApplicationResume  extends Model<SmtApplicationResume> {



	  @TableId(value = "id", type = IdType.AUTO)
	   private Integer id;

	   /**
	    * 应聘id
	    */
	   @TableField("APPLICATION_ID")
	   private Long applicationId;

	   /**
	    * 二进制简历
	    */
	   private byte[] resume;

	   /**
	    * 简历名称
	    */
	   @TableField("RESUME_NAME")
	   private String resumeName;



}
