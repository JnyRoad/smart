package com.tce.smart.platform.core.dto;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class RepairsApprovalListDTO implements Serializable {

    private static final long serialVersionUID = 1L;

	private Integer id;
	/**
	 * 业务ID 离职申请ID或访客申请ID
	 */
	private String businessId;

	/**
	 * 待审批名称
	 */
	private String approveName;

	/**
	 * 审批类型 1：离职审批；2：访客审批； 3：物品放行审批;	  4：员工申诉审批
	 */
	private Integer approveType;

	/**
	 * 审批状态 0：待审批；1：通过；2：拒绝；3: 关闭；4：等待
	 */
	private Integer approveState;

	/**
	 * 创建时间
	 */
	private LocalDateTime createTime;
	/**
	 * 审批员工号
	 */
	private String approveBadge;
	/**
	 * 审批次序
	 */
	private Integer sort;


	/**
	 * 节点id
	 */
	private Integer nodeId;

	/**
	 * 范围类型
	 */
	private Integer
			rangeType;
	/**
	 * 报修类型
	 */
	private Integer repairType;

	/**
	 * 楼栋名称
	 */
	private String dormitoryName;
	/**
	 * 故障描述
	 */
	private String faultDesc;

	/**
	 * 宿舍保修状态
	 */
	private Integer status;
}
