package com.tce.smart.app.dto.fore;

import javax.validation.constraints.NotBlank;

import lombok.Data;

/**
 * 工资签单
 *
 * @author mingkai.wu
 * @date 2019-05-09 15:17:02
 */
@Data
public class WageSignDto {

	/**
	 * 员工号
	 */
	private String badge;

	/**
	 * 工资月份
	 */
	private String wageDate;

	/**
	 * 签名照
	 */
	private String signImg;

}
