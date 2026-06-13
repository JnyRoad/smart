package com.tce.smart.platform.core.entity;


import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Date;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@TableName("smt_ehr_to_staff_setting")
@EqualsAndHashCode(callSuper = true)
public class SmtEhrToStaffSetting extends Model<SmtEhrToStaffSetting> {

	private static final long serialVersionUID = 1L;
	/**
	*
	*/
	@TableId(value = "id", type = IdType.AUTO)
	private Integer id;

	/**
	 * BUId
	 */
	private String compId;

	/**
	 * 同步时长
	 */
	private Integer time;
	/**
	 * 同步单位
	 */
	private String timeUnit;
	/**
	 * 同步时间 s
	 */
	private Integer timeSecond;

	/**
	 * 创建时间
	 */
	private LocalDateTime createTime;

	/**
	 * 创建人
	 */
	private String createUser;
}
