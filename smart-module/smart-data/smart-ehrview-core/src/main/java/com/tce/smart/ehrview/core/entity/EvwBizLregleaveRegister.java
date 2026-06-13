package com.tce.smart.ehrview.core.entity;


import java.math.BigDecimal;
import java.util.Date;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;

import lombok.Data;

/**
 * 请假信息
 * @author 齐佩
 *
 */
@Data
@TableName("evw_BIZ_LREGLEAVE_REGISTER")
public class EvwBizLregleaveRegister extends Model<EvwBizLregleaveRegister>{
	/**
	 * 员工号
	 */
	@TableField("BADGE")
	private String BADGE;

	@TableField("NAME")
	private String NAME;

	@TableField("TWID")
	private Integer TWID;

	@TableField("DEPID")
	private Integer DEPID;
	/**
	 * 请假开始时间
	 */
	@TableField("BeginTime")
	private Date BeginTime;
	/**
	 * 请假结束时间
	 */
	@TableField("EndTime")
	private Date EndTime;

	@TableField("Amount")
	private BigDecimal Amount;

	@TableField("Unit")
	private Integer Unit;

	@TableField("DayoffReason")
	private String DayoffReason;

	@TableField("FormState")
	private Integer FormState;
}
