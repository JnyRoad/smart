package com.tce.smart.platform.core.model;

import com.tce.smart.common.core.vo.BaseVO;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 工资签单详情
 * @author Lenovo
 *
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class WageSignDetail extends BaseVO {
	private static final long serialVersionUID = 1L;

	/**
	 * 状态
	 */
	private Integer id;

	/**
	 * 员工号
	 */
	private String badge;

	/**
	 * 员工姓名
	 */
	private String name;

	/**
	 * 部门名称
	 */
	private String depName;

	/**
	 * bu
	 */
	private String compName;

	/**
	 * 工资月份
	 */
	private String wageDate;

	/**
	 * 签名照
	 */
	private String signImg;

	/**
	 * 签单时间
	 */
	private String createTime;

	/**
	 * 所属园区
	 */
	private String parkName;

	private Integer signStatus;

	private String signStatusDesc;

	private String noticeStatus;

}
