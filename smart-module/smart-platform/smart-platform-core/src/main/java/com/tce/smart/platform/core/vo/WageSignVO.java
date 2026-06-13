package com.tce.smart.platform.core.vo;

import java.util.Date;

import com.baomidou.mybatisplus.extension.activerecord.Model;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 工资签单列表
 * @author Lenovo
 *
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class WageSignVO extends Model<WageSignVO> {
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
	 * 创建时间
	 */
	private Date createTime;

	private String parkName;

	private Integer signStatus;

	private String signStatusDesc;

	private Integer countNotSign;

	private Integer countSign;

	private String phone;
}
