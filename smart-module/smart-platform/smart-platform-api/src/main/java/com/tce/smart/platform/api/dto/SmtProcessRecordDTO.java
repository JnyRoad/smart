package com.tce.smart.platform.api.dto;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.Date;

/**
 * 流程审批记录表
 *
 * @author梁圆
 * @date 2019-05-05 11:34:58
 */
@Data
public class SmtProcessRecordDTO implements Serializable {
	private static final long serialVersionUID = 5715804800133925747L;

	/**
	 * 主键
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
	 * 流程编号
	 */
	private String processId;
	/**
	 * 审批状态
	 */
	private String status;
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
	private Date creatTime;
	/**
	 * 节点名称
	 */
	private String nodeName;
	/**
	 * 节点状态
	 */
	private Integer nodeState;
}
