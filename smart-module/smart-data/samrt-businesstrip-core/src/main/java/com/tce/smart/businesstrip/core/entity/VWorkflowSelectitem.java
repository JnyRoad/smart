package com.tce.smart.businesstrip.core.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * @description: OA区域字典表
 * @date:
 * @author: fushiping
 * @version: 1.0
 */
@Data
@TableName("V_WORKFLOW_SELECTITEM")
public class VWorkflowSelectitem {

	/**
	 *
	 */
	@TableField("ID")
	private String ID;

	/**
	 * 表单对应的元素,OA区域固定为10254
	 */
	@TableField("FIELDID")
	private String FIELDID;

	/**
	 * 选定选项，OA区域固定为  (0,5,8,11,18,14,15,20,17)
	 */
	@TableField("SELECTVALUE")
	private Integer SELECTVALUE;

	/**
	 * 区域名
	 */
	@TableField("SELECTNAME")
	private String SELECTNAME;


	/**
	 *
	 */
	@TableField("CANCEL")
	private Integer CANCEL;

}
