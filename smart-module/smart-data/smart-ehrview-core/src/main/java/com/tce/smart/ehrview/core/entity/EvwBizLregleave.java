package com.tce.smart.ehrview.core.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @Auther: guohongtai
 * @Date: 2020-07-20 19:01
 */
@Data
@TableName("evw_LRegLeave_all")
public class EvwBizLregleave extends Model<EvwBizLregleave> {
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
}
