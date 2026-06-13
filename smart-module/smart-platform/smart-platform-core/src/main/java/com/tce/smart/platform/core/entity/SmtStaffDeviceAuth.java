package com.tce.smart.platform.core.entity;

import java.util.Date;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 员工通关权限关联表
 * @author qipei
 *
 */
@Data
@TableName("smt_staff_device_auth")
@EqualsAndHashCode(callSuper = true)
public class SmtStaffDeviceAuth  extends  Model<SmtStaffDeviceAuth>{
	private static final long serialVersionUID = 1L;

	/**
	*
	*/
	@TableId(value = "id", type = IdType.AUTO)
	private Integer id;

	/**
	 * 员工id
	 */
	private Long staffId;

	/**
	 * 权限策略id
	 */
	private Integer authId;

	private Date createTime;

	/**
	 * 权限类型 1:基础权限  2:保密区权限
	 */
	private Integer authType;
}
