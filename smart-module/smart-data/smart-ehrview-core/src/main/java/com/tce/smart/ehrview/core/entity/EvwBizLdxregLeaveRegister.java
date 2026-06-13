package com.tce.smart.ehrview.core.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;

import java.util.Date;

/**
 * @Auther: guohongtai
 * @Date: 2020-07-22 11:40
 */
@Data
@TableName("evw_biz_LDXREGLEAVE_REGISTER")
public class EvwBizLdxregLeaveRegister extends Model<EvwBizLdxregLeaveRegister> {
	@TableField("BADGE")
	private String BADGE;
	@TableField("NAME")
	private String NAME;
	@TableField("TWID")
	private Integer TWID;
	@TableField("DEPID")
	private Integer DEPID;
	@TableField("BeginTime")
	private Date BEGINTIME;
	@TableField("REMARK")
	private String REMARK;
	@TableField("FormState")
	private Integer FormState;
}
