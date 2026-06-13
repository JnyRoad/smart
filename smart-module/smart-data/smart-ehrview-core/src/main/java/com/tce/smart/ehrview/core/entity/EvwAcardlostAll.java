package com.tce.smart.ehrview.core.entity;

import java.util.Date;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;

import lombok.Data;

@Data
@TableName("evw_ACARDLOST_ALL")
public class EvwAcardlostAll extends Model<EvwAcardlostAll> {
	@TableField("BADGE")
	private String BADGE;

	@TableField("NAME")
	private String NAME;

	@TableField("DEPID")
	private Integer DEPID;

	@TableField("KQSTARTDATE")
	private Date KQSTARTDATE;

	@TableField("REASON")
	private Integer REASON;

	@TableField("SHIFT")
	private String SHIFT;
}
