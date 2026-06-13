package com.tce.smart.ehrview.core.entity;

import java.math.BigDecimal;
import java.util.Date;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;

import lombok.Data;

/**
 * 加班历史表
 * @author 齐佩
 *
 */
@Data
@TableName("evw_LERGOT_ALL")
public class EvwLergotAll extends Model<EvwLergotAll> {
	@TableField("BADGE")
	private String BADGE;

	@TableField("NAME")
	private String NAME;

	@TableField("DEPID")
	private Integer DEPID;
	/**
	 * 加班日期
	 */
	@TableField("OTTERM")
	private Date OTTERM;

	@TableField("OTTYPE")
	private Integer OTTYPE;

	@TableField("OT2STARTTIME")
	private String OT2STARTTIME;

	@TableField("OT2ENDTIME")
	private String OT2ENDTIME;

	@TableField("OT4STARTTIME")
	private String OT4STARTTIME;

	@TableField("OT4ENDTIME")
	private String OT4ENDTIME;

	@TableField("OT5STARTTIME")
	private String OT5STARTTIME;

	@TableField("OT5ENDTIME")
	private String OT5ENDTIME;

	@TableField("AMOUNT")
	private BigDecimal AMOUNT;

	@TableField("REASON")
	private String REASON;
}
