package com.tce.smart.platform.core.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 消息模板
 *
 * @author mingkai.wu
 * @date 2019-05-15 18:06:13
 */
@Data
@TableName("smt_msg_temp")
@EqualsAndHashCode(callSuper = true)
public class SmtMsgTemp extends Model<SmtMsgTemp> {

	/**
	 * 序列号
	 */
	private static final long serialVersionUID = -7273380905173585457L;

	/**
	 * 主键ID
	 */
	@TableId
	private Integer id;

	/**
	 * 模板名称
	 */
	private String name;

	/**
	 * 园区ID
	 */
	private Integer parkId;

	/**
	 * 模板类型：1、离线消息
	 */
	private Integer msgType;
}
