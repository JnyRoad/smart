package com.tce.smart.ehrview.core.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @Auther: guohongtai
 * @Date: 2020-07-21 11:03
 */
@Data
@TableName("evw_CALLOWANCE_cancel_allt")
public class EvwCallowanceCancelAllt extends Model<EvwCallowanceCancelAllt> {
	@TableField("BADGE")
	private String BADGE;

	@TableField("NAME")
	private String NAME;

	@TableField("DEPID")
	private Integer DEPID;

	@TableField("XTYPE")
	private Integer XTYPE;

	@TableField("BACKDATE")
	private Date BACKDATE;

	@TableField("AMOUNT")
	private BigDecimal AMOUNT;

	@TableField("REAMRK")
	private String REAMRK;
}