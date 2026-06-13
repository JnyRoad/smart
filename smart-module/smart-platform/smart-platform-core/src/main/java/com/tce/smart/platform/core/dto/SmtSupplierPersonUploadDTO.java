package com.tce.smart.platform.core.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @description: SmtSupplierPersonUploadDTO
 * @date: 2020-07-23 10:55
 * @author: wuling
 * @version: 1.0
 */
@Data
public class SmtSupplierPersonUploadDTO {

	/**
	 * 保密区供应商记录标识
	 */
	private Long supplierId;

	/**
	 * 人员信息列表
	 */
	private List<PersonDetail> personDetails;

	@Data
	@NoArgsConstructor
	public static class PersonDetail{
		/**
		 * 名称
		 */
		private String name;

		/**
		 * 身份证
		 */
		private String idCard;

		/**
		 * 电话
		 */
		private String phone;
	}
}
