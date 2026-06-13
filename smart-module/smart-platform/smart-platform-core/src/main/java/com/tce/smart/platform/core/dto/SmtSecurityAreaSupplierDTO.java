package com.tce.smart.platform.core.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * @description: SmtSecurityAreaSupplierReqDTO
 * @date: 2020-07-21 9:26
 * @author: wuling
 * @version: 1.0
 */
@Data
public class SmtSecurityAreaSupplierDTO{

	/**
	 * 记录ID
	 */
	private Long id;

	/**
	 * 园区ID
	 */
	private Integer parkId;

	/**
	 * 园区名称
	 */
	private String parkName;

	/**
	 * 单位名称
	 */
	private String companyName;

	/**
	 * 状态 1.启用 2.停用
	 */
	private Integer status;

	/**
	 * 开始生效时间
	 */
	private Date beginEffectTime;

	/**
	 * 结束生效时间
	 */
	private Date endEffectTime;

	/**
	 * 备注
	 */
	private String remark;
	/**
	 * 携带物品项
	 */
	private String carryItem;
}
