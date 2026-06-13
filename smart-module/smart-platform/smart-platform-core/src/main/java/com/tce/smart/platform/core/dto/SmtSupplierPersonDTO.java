package com.tce.smart.platform.core.dto;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

/**
 * @description: SmtSupplierPersonDTO
 * @date: 2020-07-21 10:51
 * @author: wuling
 * @version: 1.0
 */
@Data
public class SmtSupplierPersonDTO {

	/**
	 * 主键ID
	 */
	private Long id;

	/**
	 * 供应商主键Id
	 */
	private Long supplierId;

	/**
	 * 单位名称
	 */
	private String companyName;

	/**
	 * 身份证
	 */
	private String idCard;

	/**
	 * 园区ID
	 */
	private Integer parkId;

	/**
	 * 园区名称
	 */
	private String parkName;

	/**
	 * 人员名称
	 */
	private String personName;

	/**
	 * 电话
	 */
	private String phone;

	/**
	 * 备注
	 */
	private String remark;
}
