package com.tce.smart.platform.core.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

/**
 * app招聘数据权限表
 *
 * @author mckaywu
 * @date 2019-06-12 10:57:52
 */
@Data
@TableName("smt_app_hr_auth")
@EqualsAndHashCode(callSuper = true)
public class SmtAppHrAuth extends Model<SmtAppHrAuth> {

	/**
	 * 序列号
	 */
	private static final long serialVersionUID = -2288461676730499070L;

	/**
	 * 主键
	 */
	private String id;

	/**
	 * 权限名称
	 */
	private String authName;

	/**
	 * 层级ID，多个用逗","号分开
	 */
	private String jobLeave;

	/**
	 * 删除标志 0-未删除 1-已删除
	 */
	private Integer delFlag;

	/**
	 * 创建时间
	 */
	private Date createTime;
}
