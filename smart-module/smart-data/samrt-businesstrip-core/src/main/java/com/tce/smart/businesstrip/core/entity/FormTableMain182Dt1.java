package com.tce.smart.businesstrip.core.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * @author: Li.JiaJun
 * @since: 2021/8/10 11:34
 */
@Data
@TableName("FORMTABLE_MAIN_182_DT1")
public class FormTableMain182Dt1 {

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
	 * 姓名
	 */
	@TableField("XM")
	private Integer xm;

	/**
	 * 工号
	 */
	@TableField("GH")
	private String gh;

	/**
	 * 离厂日期
	 */
	@TableField("LCRQ")
	private String lcrq;

	/**
	 * 离厂时间
	 */
	@TableField("LCSJ")
	private String lcsj;

	/**
	 * 离厂事由
	 */
	@TableField("LCSY")
	private String lcsy;

	/**
	 * 返厂日期
	 */
	@TableField("FCRQ")
	private String fcrq;

	/**
	 * 返厂时间
	 */
	@TableField("FCSJ")
	private String fcsj;

	/**
	 * 级别
	 */
	@TableField("JB")
	private Integer jb;
}
