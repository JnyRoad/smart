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
 * 员工反馈表
 * @author 齐佩
 *
 */

@Data
@TableName("smt_feed_back")
@EqualsAndHashCode(callSuper = true)
public class SmtFeedBack extends Model<SmtFeedBack> {



	private static final long serialVersionUID = 1L;
    /**
   * 主键
   */
	@TableId(value = "id", type = IdType.AUTO)
    private Integer id;

	/**
	 * 反馈员工工号
	 */
	private String staffBadge;

	/**
	 * 反馈员工姓名
	 */
	private String staffName;

	/**
	 * 反馈员工电话
	 */
	private String staffPhone;

	/**
	 * 反馈问题标签
	 */
	private String question;

	/**
	 * 反馈时间
	 */
	private LocalDateTime createTime;

	/**
	 * 处理状态 0-未处理 1-已处理
	 */
	private Integer status;

	/**
	 * 处理人
	 */
	private String operator;

	/**
	 * 处理时间
	 */
	private LocalDateTime operateTime;

	/**
	 * 回复
	 */
	private String reply;


}
