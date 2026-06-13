package com.tce.smart.platform.core.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;
import java.util.List;

/**
 * @description: SmtSecurityAreaSupplierVO
 * @date: 2020-07-23 17:05
 * @author: wuling
 * @version: 1.0
 */
@Data
public class SmtStaffAppealVO {

	/**
	 * 标识Id
	 */
	private Long id;

	/**
	 * 员工工号
	 */
	private String badge;

	/**
	 * 员工名称
	 */
	private String staffName;

	/**
	 * BU
	 */
	private String compName;

	/**
	 * 部门名称
	 */
	private String depName;

	/**
	 * 反馈人电话
	 */
	private String staffPhone;

	/**
	 * 申诉类型
	 */
	private Integer appealType;

	/**
	 * 申诉类型描述
	 */
	private String appealTypeDesc;

	/**
	 * 描述图片编号列表
	 */
	private String appealImgs;

	/**
	 * 状态
	 */
	private Integer status;

	/**
	 * 状态描述
	 */
	private String statusDesc;

	/**
	 * 是否已转交TA人
	 */
	private Integer ischange;

	/**
	 * 反馈时间
	 */
	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private Date createTime;

	/**
	 * 反馈描述
	 */
	private String appealDesc;

	/**
	 * 回复人名称
	 */
	private String replyName;

	/**
	 * 回复内容
	 */
	private String replyDesc;

	/**
	 * 回复时间
	 */
	@DateTimeFormat(pattern = "yyyy-MM-dd")
	@JsonFormat(pattern = "yyyy-MM-dd")
	private Date replyTime;
}
