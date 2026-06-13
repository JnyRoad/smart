package com.tce.smart.ehrview.core.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;

/**
 * 补贴计算规则
 * @author qipei
 *
 */
@Data
@TableName("cvw_cCD_AllowRule")
public class CvwCcdAllowRule {


		@TableField("Id")
	    private Integer Id;

	    @TableField("Title")
	    private String Title;

	    @TableField("Type")
	    private String Type;
}
