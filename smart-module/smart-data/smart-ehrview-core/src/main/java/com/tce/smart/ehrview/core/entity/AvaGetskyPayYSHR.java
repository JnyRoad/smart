package com.tce.smart.ehrview.core.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * 考勤汇总
 * @author QIPEI
 *
 */
@Data
@TableName("aVA_GetskyPay_YS_HR")
public class AvaGetskyPayYSHR {

	@TableField("term")
	private Date TERM;

	@TableField("badge")
	private String Badge;

	@TableField("CompId")
	private String CompId;

	@TableField("a6")
	private Double A6;

	@TableField("a7")
	private Double A7;

	@TableField("a10")
	private Double A10;

	@TableField("a11")
	private Double A11;

	@TableField("a12")
	private Double A12;

	@TableField("a15")
	private Double A15;

	@TableField("a16")
	private Double A16;

	@TableField("a17")
	private Double A17;

	@TableField("a18")
	private Double A18;

	@TableField("a19")
	private Double A19;

	@TableField("a20")
	private Double A20;

	@TableField("a21")
	private Double A21;

	@TableField("a22")
	private Double A22;

	@TableField("a23")
	private Double A23;

	@TableField("a24")
	private Double A24;

	@TableField("a25")
	private Double A25;

	@TableField("a27")
	private Double A27;

	@TableField("a28")
	private Double A28;

	@TableField("a29")
	private Double A29;

	@TableField("a30")
	private Double A30;

	@TableField("a36")
	private Double A36;





}
