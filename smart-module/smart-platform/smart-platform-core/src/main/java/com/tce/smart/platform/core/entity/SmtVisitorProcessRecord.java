package com.tce.smart.platform.core.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

/**
 * 访客审批流程表
 * @author QIPEI
 * @date 2019/10/21
 */
@Data
@TableName("smt_visitor_process_record")
@EqualsAndHashCode(callSuper = true)
public class SmtVisitorProcessRecord extends Model<SmtVisitorProcessRecord> {


	private static final long serialVersionUID = 1L;

	/**
	 *
	 */
	@TableId(value = "id", type = IdType.AUTO)
	private Integer id;


	/**
	 * 员工姓名
	 */
	private String staffName;
	/**
	 * 员工号
	 */
	private String staffBadge;

	/**
	 * 员工职层
	 */
	private String staffJche;
	/**
	 * 流程编号
	 */
	private Long visitorId;
	/**
	 * 审批状态
	 */
	private Integer status;
	/**
	 * 审批备注
	 */
	private String remark;
	/**
	 * 审批时间
	 */
	private Date recordDate;
	/**
	 * 创建时间
	 */
	private Date createDate;

	/**审批节点
	 *
	 */
	private Integer recordNode;

	/**
	 * 审批状态名称
	 */
	private String statusName;
}
