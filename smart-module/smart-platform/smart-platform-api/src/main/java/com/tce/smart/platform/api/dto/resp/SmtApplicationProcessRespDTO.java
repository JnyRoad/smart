package com.tce.smart.platform.api.dto.resp;

import com.tce.smart.common.core.vo.BaseVO;
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
@EqualsAndHashCode(callSuper = true)
public class SmtApplicationProcessRespDTO extends BaseVO {

	private static final long serialVersionUID = 6426480069824144863L;


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
