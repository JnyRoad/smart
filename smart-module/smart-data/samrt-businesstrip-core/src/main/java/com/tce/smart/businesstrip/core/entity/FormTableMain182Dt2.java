package com.tce.smart.businesstrip.core.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * @author: Li.JiaJun
 * @since: 2021/8/10 11:34
 */
@Data
@TableName("FORMTABLE_MAIN_182_DT2")
public class FormTableMain182Dt2 {

	/**
	 * 主键ID
	 */
	@TableField("ID")
	private Integer id;

	/**
	 * 主表ID
	 */
	@TableField("MAINID")
	private Integer mainid;

	/**
	 * 名称
	 */
	@TableField("WPMC")
	private String wpmc;

	/**
	 * 单位
	 */
	@TableField("WPDW")
	private String wpdw;

	/**
	 * 数量
	 */
	@TableField("WPSL")
	private Integer wpsl;

	/**
	 * 物品流向
	 */
	@TableField("WPLX")
	private String wplx;

	/**
	 * 接收单位
	 */
	@TableField("JSDW")
	private String jsdw;

	/**
	 * 备注(原因)
	 */
	@TableField("BZ")
	private String bz;

	/**
	 * 运输方式
	 */
	@TableField("YSFS")
	private Integer ysfs;

	/**
	 * 姓名
	 */
	@TableField("XM")
	private Integer xm;

	/**
	 * 车牌号
	 */
	@TableField("CPH")
	private String cph;

	/**
	 * 放行日期
	 */
	@TableField("FXRQ")
	private String fxrq;

	/**
	 * 资产编码
	 */
	@TableField("WPBM")
	private String wpbm;

	/**
	 * 是否返厂
	 */
	@TableField("WPSFFC")
	private String wpsffc;

	/**
	 * 返厂日期
	 */
	@TableField("WPFCRQ")
	private String wpfcrq;

	/**
	 * 返厂时间
	 */
	@TableField("WPFCSJ")
	private String wpfcsj;
}
