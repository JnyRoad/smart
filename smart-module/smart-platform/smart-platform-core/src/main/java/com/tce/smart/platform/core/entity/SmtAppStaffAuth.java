package com.tce.smart.platform.core.entity;

import java.util.Date;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 员工App权限
 *
 * @author mckaywu
 * @date 2019-06-12 11:05:08
 */
@Data
@TableName("smt_app_staff_auth")
@EqualsAndHashCode(callSuper = true)
public class SmtAppStaffAuth extends Model<SmtAppStaffAuth> {
	private static final long serialVersionUID = 1L;

	/**
	 * 主键
	 */
	private Integer id;

	/**
	 * 员工ID
	 */
	private Long staffId;

	/**
	 * 权限ID
	 */
	private Integer authId;

	/**
	 * 创建时间
	 */
	private Date create_time;
}
