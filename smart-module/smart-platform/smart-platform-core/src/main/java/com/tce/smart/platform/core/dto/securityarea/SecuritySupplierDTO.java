package com.tce.smart.platform.core.dto.securityarea;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

/**
 * @description: 保密区供应商
 * @date: 2020-07-31 9:13
 * @author: wuling
 * @version: 1.0
 */
@Data
public class SecuritySupplierDTO{

	/**
	 * 标识Id
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
	 * 单位编号
	 */
	private String companyCode;

	/**
	 * 单位名称
	 */
	private String companyName;

	/**
	 * 供应商类型 1.A类 2.非A类
	 */
	private Integer supplierType;

	/**
	 * 协议到期时间
	 */
	private Date endEffectTime;

	/**
	 * 协议Code
	 */
	private String protocolCode;
	/**
	 * 携带物品项
	 */
	private String carryItem;

	private String authorizedArea;

	private Integer authPersonNum;

	private String certNo;

	private String personName;
}
