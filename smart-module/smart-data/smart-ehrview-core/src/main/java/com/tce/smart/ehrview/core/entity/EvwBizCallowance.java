package com.tce.smart.ehrview.core.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @Auther: guohongtai
 * @Date: 2020-07-22 09:33
 */
@Data
@TableName("evw_BIZ_CALLOWANCE")
public class EvwBizCallowance extends Model<EvwBizCallowance> {

	@TableField("BADGE")
	private String BADGE;

	@TableField("NAME")
	private String NAME;

	@TableField("DEPID")
	private Integer DEPID;

	@TableField("XTYPE")
	private Integer XTYPE;

	@TableField("BEGINDATE")
	private Date BEGINDATE;

	@TableField("APPENDDATE")
	private Date APPENDDATE;

	@TableField("AMOUNT")
	private BigDecimal AMOUNT;

	@TableField("REAMRK")
	private String REAMRK;

	@TableField("FormState")
	private Integer FormState;
}
