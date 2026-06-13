package com.tce.smart.platform.core.entity;

import java.util.Date;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 请假申请表
 *
 * @author 梁圆
 * @date 2019-04-13 18:26:36
 */
@Data
@TableName("smt_ask_leave_application")
@EqualsAndHashCode(callSuper = true)
public class SmtAskLeaveApplication extends Model<SmtAskLeaveApplication> {
	private static final long serialVersionUID = 1L;

	/**
	 *
	 */
	@TableId(value = "id", type = IdType.AUTO)
	private Integer id;
	/**
	 *
	 */
	private Long staffId;
	/**
	 *
	 */
	private String staffBadge;
	/**
	 *
	 */
	private String staffName;
	/**
	 *
	 */
	private Date startTime;
	/**
	 *
	 */
	private Date endTime;
	/**
	 * 请假类型 年假 事假等
	 */
	private Integer type;
	/**
	 * 请假时长
	 */
	private String duration;
	/**
	 * 请假的事由
	 */
	private String cause;
	/**
	 * 流程编号
	 */
	private String processId;
	/**
	 * 创建时间
	 */
	private Date createTime;
	/**
	 * 班次
	 */
	private String className;
	/**
	 * 2入
	 */
	private String secondEnter;
	/**
	 * 2出
	 */
	private String secondOut;
	/**
	 * 4入
	 */
	private String fourthEnter;
	/**
	 * 4出
	 */
	private String fourthOut;
	/**
	 * 5入
	 */
	private String fifthEnter;
	/**
	 * 5出
	 */
	private String fifthOut;
	/**
	 *图片id
	 */
	private String photoId;
}
