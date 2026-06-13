package com.tce.smart.platform.core.entity;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 准备邮件接收人
 *
 * @author 齐佩
 */
@Data
@TableName("smt_email_receive")
@EqualsAndHashCode(callSuper = true)
public class SmtEmailReceive extends Model<SmtEmailReceive> {
	private static final long serialVersionUID = 2088203594642752380L;
	/**
	 * 主键
	 */
	@TableId(value = "id", type = IdType.AUTO)
	private String id;

	/**
	 * 模板id
	 */
	private String templateId;

	/**
	 * 模板名称
	 */
	private String name;

	/**
	 * 模板内容
	 */
	private String phone;
	/**
	 * 备注
	 */
	private String email;

	/**
	 * 园区ID
	 */
	private Integer parkId;

	/**
	 * 园区
	 */
	@TableField(exist = false)
	private String parkName;
}