package com.tce.smart.platform.core.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.*;

import java.util.Date;

/**
 * @description: SmtRepairsReply
 * @date: 2020-07-24 16:28
 * @author: wuling
 * @version: 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("SMT_REPAIRS_REPLY")
@EqualsAndHashCode(callSuper = true)
public class SmtRepairsReply extends Model<SmtRepairsReply> {
	private static final long serialVersionUID = -2252855284028419380L;

	/**
	 * 主键ID
	 */
	@TableId(value = "id", type = IdType.ID_WORKER)
	private Long id;

	/**
	 * 报修记录Id
	 */
	private Long repairId;

	/**
	 * 回复人名称
	 */
	private String replyName;

	/**
	 * 回复状态
	 */
	private Integer replyStatus;

	/**
	 * 回复结果
	 */
	private String replyResult;

	/**
	 * 创建时间
	 */
	private Date createTime;
}
