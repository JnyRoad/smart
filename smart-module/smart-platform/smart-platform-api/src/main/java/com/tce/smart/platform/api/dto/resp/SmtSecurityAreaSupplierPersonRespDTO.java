package com.tce.smart.platform.api.dto.resp;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * @description: 保密区供应商人员信息DTO
 * @date: 2020-09-07 15:58
 * @author: wuling
 * @version: 1.0
 */
@Data
public class SmtSecurityAreaSupplierPersonRespDTO implements Serializable {

	private static final long serialVersionUID = -1898831326122767819L;

	/**
	 * 人员名称
	 */
	@ApiModelProperty("人员名称")
	private String personName;

	/**
	 * 身份证
	 */
	@ApiModelProperty("身份证")
	private String visitCardId;

	/**
	 * 电话
	 */
	@ApiModelProperty("电话")
	private String phone;
}
