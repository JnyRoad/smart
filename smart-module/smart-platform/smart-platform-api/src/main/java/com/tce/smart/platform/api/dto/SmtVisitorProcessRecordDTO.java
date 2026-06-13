package com.tce.smart.platform.api.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 访客审批流程表
 * @author QIPEI
 * @date 2019/10/21
 */
@Data
public class SmtVisitorProcessRecordDTO implements Serializable {


	private static final long serialVersionUID = 3641159389632669110L;

	/**
	 *
	 */
	private Integer id;


	/**
	 * 员工姓名
	 */
	@ApiModelProperty(value = "员工姓名",required = true)
	private String staffName;

	/**
	 * 员工号
	 */
	@ApiModelProperty(value = "员工号",required = true)
	private String staffBadge;

	/**
	 * 员工职层
	 */
	@ApiModelProperty(value = "员工职层",required = true)
	private String staffJche;

	/**
	 * 流程编号
	 */
	@ApiModelProperty(value = "流程编号",required = true)
	private Long visitorId;

	/**
	 * 审批状态
	 */
	@ApiModelProperty(value = "审批状态",required = true)
	private Integer status;

	/**
	 * 审批备注
	 */
	@ApiModelProperty(value = "审批备注",required = true)
	private String remark;

	/**
	 * 审批时间
	 */
	@ApiModelProperty(value = "审批时间",required = true)
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private Date recordDate;

	/**
	 * 创建时间
	 */
	@ApiModelProperty(value = "创建时间",required = true)
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private Date createDate;

	/**审批节点
	 *
	 */
	@ApiModelProperty(value = "审批节点",required = true)
	private Integer recordNode;

	/**
	 * 审批状态名称
	 */
	@ApiModelProperty(value = "审批状态名称",required = true)
	private String statusName;
}
