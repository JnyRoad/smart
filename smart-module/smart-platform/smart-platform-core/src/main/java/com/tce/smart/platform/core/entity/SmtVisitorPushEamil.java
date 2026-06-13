package com.tce.smart.platform.core.entity;



import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 访客信息接收人email
 * @author QIPEI
 *	2019-08-25 14:14
 */
@Data
@TableName("SMT_VISTOR_PUSH_EMAIL")
@EqualsAndHashCode(callSuper = true)
public class SmtVisitorPushEamil extends Model<SmtVisitorPushEamil> {
	private static final long serialVersionUID = 1L;

	/**
	 *
	 */
	@TableId(value = "id", type = IdType.AUTO)
	private Long id;

	/**
	 * 邮件接受者
	 */
	private String receiver;
	/**
	 * 邮件
	 */
	private String email;
	/**
	 * 推送周期类型 0-每天 1-每周 2-每月
	 */
	private Integer type;

	private Integer parkId;

}
