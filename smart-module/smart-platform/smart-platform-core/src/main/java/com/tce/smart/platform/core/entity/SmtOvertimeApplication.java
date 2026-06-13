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
 * 职工加班申请表

 *
 * @author 梁圆
 * @date 2019-04-13 18:20:11
 */
@Data
@TableName("smt_overtime_application")
@EqualsAndHashCode(callSuper = true)
public class SmtOvertimeApplication extends Model<SmtOvertimeApplication> {
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
	private String workTime;
	/**
	 * 加班类型
	 */
	private Integer workType;
	/**
	 * 加班班别
	 */
	private Integer workClassCode;
	/**
	 * 加班时长
	 */
	private String duration;
	/**
	 * 原因
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
	 * 2入
	 */
	private String startDateTwo;
	/**
	 * 2出
	 */
	private String endDateTwo;
	/**
	 * 4入
	 */
	private String startDateFour;
	/**
	 * 4出
	 */
	private String endDateFour;
	/**
	 * 5入
	 */
	private String startDateFive;
	/**
	 * 5出
	 */
	private String endDateFive;
	/**
	 * 是否出差
	 */
	private Integer isTravelWork;

}
