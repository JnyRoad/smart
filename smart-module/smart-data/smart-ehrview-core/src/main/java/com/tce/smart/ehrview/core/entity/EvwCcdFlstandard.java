package com.tce.smart.ehrview.core.entity;

import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;

/**
 * 职层信息
 * @author qipei
 *
 */

@Data
@TableName("evw_ccd_FLstandard")
public class EvwCcdFlstandard {

	/**
	 * 职层id
	 */
	private String jchenid;

	/**
	 * 福利层级
	 */
	private String code;

	/**
	 * 职层名称
	 */
	private String title;

	/**
	 * 食堂补贴金额
	 */
	private String standard;
	/**
	 * 外宿补贴金额
	 */
	private String standard1;

	 /**
	  * pzid
	  */
	 private Integer pzid;


}
