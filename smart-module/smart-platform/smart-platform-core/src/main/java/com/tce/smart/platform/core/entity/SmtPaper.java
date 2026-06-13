package com.tce.smart.platform.core.entity;

import java.time.LocalDateTime;
import java.util.Date;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 问卷调查-问卷表
 * @author 齐佩
 *
 */

@Data
@TableName("smt_paper")
@EqualsAndHashCode(callSuper = true)
public class SmtPaper extends Model<SmtPaper> {

	@TableId(value = "id", type = IdType.AUTO)
	private Integer id;

	/**
	 * 问卷标题
	 */
	private String title;

	/**
	 * 备注
	 */
	private String remark;

	/**
	 * 创建时间
	 */
	private LocalDateTime createTime;

	/**
	 * 开始时间
	 */
	private LocalDateTime startTime;

	/**
	 * 结束时间
	 */
	private LocalDateTime endTime;

	/**
	 * 状态 0-未开始 1-进行中 2-已结束
	 */
	private Integer status;

	/**
	 * 创建者
	 */
	private String createUser;


	/**
	 * 所属园区id
	 */
	private Integer parkId;

	/**
	 * 0-未删除  1-已删除
	 */
	private Integer isDelete;

}
