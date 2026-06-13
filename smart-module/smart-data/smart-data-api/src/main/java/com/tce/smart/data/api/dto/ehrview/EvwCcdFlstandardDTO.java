package com.tce.smart.data.api.dto.ehrview;

import lombok.Data;

import java.io.Serializable;

/**
 * 职层信息
 * @author qipei
 *
 */

@Data
public class EvwCcdFlstandardDTO implements Serializable {

	private static final long serialVersionUID = 2137081429896342153L;

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
