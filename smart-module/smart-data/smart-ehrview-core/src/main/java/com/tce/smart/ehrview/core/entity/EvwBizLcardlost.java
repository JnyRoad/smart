package com.tce.smart.ehrview.core.entity;

import java.util.Date;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;

import lombok.Data;

@Data
@TableName("evw_BIZ_LCARDLOST")
public class EvwBizLcardlost extends Model<EvwBizLcardlost> {
	@TableField("BADGE")
	private String BADGE;
	@TableField("NAME")
	private String NAME;

	@TableField("DEPID")
	private Integer DEPID;
	/**
	 * 补卡时间
	 */
	@TableField("KQSTARTDATE")
	private Date KQSTARTDATE;

	@TableField("REASON")
	private Integer REASON;

	@TableField("SHIFT")
	private String SHIFT;

	@TableField("FormState")
	private Integer FormState;

}
