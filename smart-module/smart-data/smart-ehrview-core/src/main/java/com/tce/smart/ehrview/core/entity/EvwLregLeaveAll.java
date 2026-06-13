package com.tce.smart.ehrview.core.entity;

import java.math.BigDecimal;
import java.util.Date;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;

import lombok.Data;

/**
 * 请假历史表
 * @author 齐佩
 *
 */
@Data
@TableName("evw_LRegLeave_all")
public class EvwLregLeaveAll  extends Model<EvwLregLeaveAll>{

	@TableField("BADGE")
	private String BADGE;

	@TableField("NAME")
	private String NAME;

	@TableField("TWID")
	private Integer TWID;

	@TableField("DEPID")
	private Integer DEPID;

	@TableField("BeginTime")
	private Date BeginTime;

	@TableField("EndTime")
	private Date EndTime;

	@TableField("AMOUNT")
	private BigDecimal AMOUNT;

	@TableField("Unit")
	private Integer Unit;

	@TableField("DayoffReason")
	private String DayoffReason;
}
