package com.tce.smart.ehrview.core.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @Auther: guohongtai
 * @Date: 2020-07-22 11:37
 */
@Data
@TableName("evw_LdxRegLeave_all")
public class EvwLdxRegLeaveAll extends Model<EvwLdxRegLeaveAll> {

	@TableField("BADGE")
	private String BADGE;

	@TableField("NAME")
	private String NAME;

	@TableField("TWID")
	private Integer TWID;

	@TableField("DEPID")
	private Integer DEPID;

	@TableField("BEGINTIME")
	private Date BEGINTIME;

	@TableField("REMARK")
	private String REMARK;
}
