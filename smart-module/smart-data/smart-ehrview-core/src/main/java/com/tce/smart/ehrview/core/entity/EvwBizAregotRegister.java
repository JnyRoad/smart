package com.tce.smart.ehrview.core.entity;

import java.math.BigDecimal;
import java.util.Date;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;

import lombok.Data;

@Data
@TableName("evw_BIZ_AREGOT_REGISTER")
public class EvwBizAregotRegister extends Model<EvwBizAregotRegister> {
	@TableField("Badge")
	private String BADGE;
	@TableField("Name")
	private String NAME;
	@TableField("DepID")
	private Integer DEPID;

	/**
	 * 加班日期
	 */
	@TableField("OTTerm")
	private Date OTTERM;
	@TableField("OTType")
	private String OTTYPE;
	@TableField("Ot2StartTime")
	private String OT2STARTTIME;
	@TableField("Ot2EndTime")
	private String OT2ENDTIME;
	@TableField("Ot4StartTime")
	private String OT4STARTTIME;
	@TableField("Ot4EndTime")
	private String OT4ENDTIME;
	@TableField("Amount")
	private BigDecimal Amount;
	@TableField("Reason")
	private String Reason;
	@TableField("FormState")
	private Integer FormState;
}
