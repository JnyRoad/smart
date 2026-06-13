package com.tce.smart.platform.core.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotBlank;
import java.util.Date;

/**
 * 应聘路程表
 * @author 齐佩
 *
 */
@Data
@TableName("smt_application_process")
@EqualsAndHashCode(callSuper = true)
public class SmtApplicationProcess extends Model<SmtApplicationProcess>{

	private static final long serialVersionUID = 1L;


	  @TableId(value = "id", type = IdType.AUTO)
	   private Integer id;

	   /**
	    * 应聘id
	    */
	   private Long applicationId;

	   /**
	    * 应聘状态
	    */
	   //0-已投递1-已拒绝 2-已邀请3-待入职/4待复试，暂定/5-已入职
	   @NotBlank(message = "应聘状态不能为空")
	   private Integer status;

	   /**
	    * 园区id
	    */

	   private Integer parkId;

	   /**
	    * 操作时间
	    */
	   private Date createTime;

	   /**
	    * 操作人
	    */
	   private String createUserName;

	   private String remark;

}
