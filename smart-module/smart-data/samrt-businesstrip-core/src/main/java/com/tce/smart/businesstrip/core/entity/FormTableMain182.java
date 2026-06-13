package com.tce.smart.businesstrip.core.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * @author: Li.JiaJun
 * @since: 2021/8/10 11:21
 */
@Data
@TableName("FORMTABLE_MAIN_182")
public class FormTableMain182 {


	/**
	 * 主键ID
	 */
	@TableField("ID")
	private Integer id;

	/**
	 * 申请单id
	 */
	@TableField("REQUESTID")
	private Integer requestid;

	/**
	 * 流程编号
	 */
	@TableField("LCBH")
	private String lcbh;

	/**
	 * 申请人
	 */
	@TableField("SQR")
	private Integer sqr;

	/**
	 * 申请部门
	 */
	@TableField("SQBM")
	private Integer sqbm;

	/**
	 * 放行事项
	 */
	@TableField("FXSX")
	private Integer fxsx;

	/**
	 * 人员放行
	 */
	@TableField("RYFX")
	private String ryfx;

	/**
	 * 物品放行
	 */
	@TableField("WPFX")
	private String wpfx;

	/**
	 * 放行人级别
	 */
	@TableField("SQRJB")
	private Integer sqrjb;

	/**
	 * 附件上传
	 */
	@TableField("FJSC")
	private String fjsc;

	/**
	 * 安检附件上传
	 */
	@TableField("AJFJSC")
	private String ajfjsc;

	/**
	 * 放行去处
	 */
	@TableField("FXQC")
	private Integer fxqc;

	/**
	 * 出发地点
	 */
	@TableField("FXDD")
	private Integer fxdd;

	/**
	 * 到达地点
	 */
	@TableField("DDDD")
	private Integer dddd;

	/**
	 * 出发地点详情
	 */
	@TableField("FXDDXQ")
	private String fxddxq;

	/**
	 * 到达地点详情
	 */
	@TableField("DDDDXQ")
	private String ddddxq;

	/**
	 * 物品放行类别
	 */
	@TableField("WPFXLB")
	private Integer wpfxlb;

	/**
	 * 是否返厂
	 */
	@TableField("SFFC")
	private Integer sffc;

	/**
	 * 放行事项
	 */
	@TableField("FXSX1")
	private Integer fxsx1;
}
