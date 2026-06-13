package com.tce.smart.ehrview.core.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @Descripition:
 * @Auther: guohongtai
 * @Date: 2020-07-14 09:04
 */
@Data
@TableName("evw_hortationsAll")
public class EvwHortationsAll extends Model<EvwHortationsAll> {
	@TableField("eid")
	private Integer eid;
	@TableField("badge")
	private String badge;
	@TableField("name")
	private String name;
	@TableField("CompID")
	private Integer CompID;
	@TableField("depOne")
	private Integer depOne;
	@TableField("depTwo")
	private Integer depTwo;
	@TableField("DepID")
	private Integer DepID;
	@TableField("JobID")
	private String JobID;
	@TableField("Status")
	private Integer Status;
	@TableField("JchenID")
	private Integer JchenID;
	@TableField("isout")
	private Integer isout;
	@TableField("begindate")
	private Date begindate;
	@TableField("type")
	private Integer type;
	@TableField("kind")
	private Integer kind;
	@TableField("sumMoney")
	private BigDecimal sumMoney;
	@TableField("paymonth")
	private Date paymonth;
	@TableField("Fraction")
	private BigDecimal Fraction;
	@TableField("reason")
	private String reason;
	@TableField("description")
	private String description;
	@TableField("remark")
	private String remark;
}
