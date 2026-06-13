package com.tce.smart.platform.core.dto.securityarea;

import lombok.*;

import java.util.Date;

/**
 * @description: 保密区预约详情实体类
 * @date: 2020-07-31 9:13
 * @author: wuling
 * @version: 1.0
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
@EqualsAndHashCode(callSuper = true)
public class SecurityAreaOrderDTO extends SecurityAreaOrderPartDTO {

	/**
	 * 申请人
	 */
	private String applicant;

	/**
	 * 申请部门
	 */
	private String applyDep;

	/**
	 * 访问事由类型
	 */
	private String visitType;

	/**
	 * 来访单位标识
	 */
	private Long supplierId;

	/**
	 * 来访单位名称
	 */
	private String supplierName;

	/**
	 * 受访者名称
	 */
	private String interViewName;

	/**
	 * 受访者电话
	 */
	private String interViewPhone;

	/**
	 * 陪同者名称
	 */
	private String escortName;

	/**
	 * 陪同者电话
	 */
	private String escortPhone;

	/**
	 * 备注
	 */
	private String remark;

	/**
	 * OA流程ID
	 */
	private String processId;

	/**
	 * 携带物品 多个物品已、号分隔
	 */
	private String carryGoods;

	/**
	 * 附件名称
	 */
	private String additionalName;

	/**
	 * 创建时间
	 */
	private Date createTime;

}
