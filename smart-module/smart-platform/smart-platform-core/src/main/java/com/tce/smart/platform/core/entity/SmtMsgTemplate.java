package com.tce.smart.platform.core.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 消息模板
 *
 * @author mingkai.wu
 * @date 2019-05-15 18:06:13
 */
@Data
@TableName("smt_msg_template")
@EqualsAndHashCode(callSuper = true)
public class SmtMsgTemplate extends Model<SmtMsgTemplate> {

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
	 * 模板类型 1-短信模板 2-邮箱模板
	 */
	private Integer msgType;

	/**
	 * 模板分类 1-访客预约通知，2-招聘通知，3-离职通知
	 */
	private Integer tempType;

	/**
	 * 模板编码
	 */
	private String tempCode;

	/**
	 * 模板名称
	 */
	private String tempName;

	/**
	 * 模板内容
	 */
	private String tempContent;

	/**
	 * 模板状态：0-不可用，1-可用
	 */
	private Integer tempState;

	/**
	 * 备用字段1
	 */
	private String remark1;

	/**
	 * 备用字段3
	 */
	private String remark2;

	/**
	 * 备用字段2
	 */
	private String remark3;

	/**
	 * 创建时间
	 */
	private LocalDateTime createTime;

	/**
	 * 园区ID
	 */
	private Integer parkId;
}
