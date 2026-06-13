package com.tce.smart.platform.api.dto.resp;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * @description: 保密区供应商实体类
 * @date: 2020-07-31 16:47
 * @author: wuling
 * @version: 1.0
 */
@Data
public class SmtSecurityAreaSupplierRespDTO implements Serializable {

	private static final long serialVersionUID = 7819901263941052697L;

	/**
	 * 记录id
	 */
	@ApiModelProperty("记录id")
	private Long id;

	/**
	 * 园区ID
	 */
	@ApiModelProperty("园区ID")
	private Integer parkId;

	/**
	 * 单位名称
	 */
	@ApiModelProperty("单位名称")
	private String companyName;
}
