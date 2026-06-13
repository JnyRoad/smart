package com.tce.smart.platform.core.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 消息推送记录表
 *
 * @author mingkai.wu
 * @date 2019-05-15 18:08:42
 */
@Data
@TableName("smt_msg_record")
@EqualsAndHashCode(callSuper = true)
public class SmtMsgRecord extends Model<SmtMsgRecord> {

	/**
	 * 序列号
	 */
	private static final long serialVersionUID = -1274147842492304518L;

	/**
	 * 主键ID
	 */
	@TableId(type= IdType.AUTO)
	private Integer id;

	/**
	 * 模板Id
	 */
	private Integer tempId;

	/**
	 * 模板名称
	 */
	private String tempName;

	/**
	 * 消息接收对象
	 */
	private String msgObject;

	/**
	 * 消息内容
	 */
	private String msgContent;

	/**
	 * 消息状态：0-初始化，1-发送成功，2-发送失败'
	 */
	private Integer msgState;

	/**
	 * 是否已读：0-未读，1-已读
	 */
	private Integer readState;

	/**
	 * 消息描述
	 */
	private String msgDesc;

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
	 * 是否已删除：0-删除，1-正常
	 */
	private Integer deleteState;

	/**
	 * 创建时间
	 */
	@TableField(fill = FieldFill.INSERT)
	private LocalDateTime createTime;

	/**
	 * 更新时间
	 */
	@TableField(fill = FieldFill.INSERT_UPDATE)
	private LocalDateTime updateTime;

}
