package com.tce.smart.platform.core.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

/**
 * App权限表
 *
 * @author mckaywu
 * @date 2019-06-12 11:03:21
 */
@Data
@TableName("smt_app_auth")
@EqualsAndHashCode(callSuper = true)
public class SmtAppAuth extends Model<SmtAppAuth> {

	/**
	 * 序列号
	 */
	private static final long serialVersionUID = -5025381506747827243L;

	/**
	 * 主键
	 */
	private Integer id;

	/**
	 * 权限名称
	 */
	private String authName;

	/**
	 * 模块ID,多个用逗号","分隔'
	 */
	private String moduleId;


	/**
	 * HR权限ID,多个用逗号","分隔'
	 */
	private String hrAuthId;

	/**
	 * 权限描述
	 */
	private String authDesc;

	/**
	 * 是否是预置权限 0-是 1-否
	 */
	private Integer initFlag;

	/**
	 * 删除标志 0-未删除 1-已删除
	 */
	private Integer delFlag;

	/**
	 * 创建时间
	 */
	private Date createTime;

	/**
	 * 园区id
	 */
	private Integer parkId;

	/**
	 * 职层id,多个用逗号","分隔'
	 */
	private String jcheId;
}
