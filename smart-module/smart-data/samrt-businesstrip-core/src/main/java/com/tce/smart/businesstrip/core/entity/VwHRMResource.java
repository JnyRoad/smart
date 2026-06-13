package com.tce.smart.businesstrip.core.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 员工表
 *
 * @author liangyuan
 * @date 2019-06-24
 */
@Data
@TableName("vw_hrmresource")
@EqualsAndHashCode(callSuper = true)
public class VwHRMResource extends Model<VwHRMResource> {


	/**
	 * 序列号
	 */
	private static final long serialVersionUID = 7525449936910067774L;

	@TableField("ID")
	private Integer id;
	@TableField("WorkCode")
	private String workCode;
	@TableField("LastName")
	private String lastName;
	@TableField("TelePhone")
	private String telePhone;
	@TableField("Mobile")
	private String mobile;
	@TableField("Email")
	private String email;


}
