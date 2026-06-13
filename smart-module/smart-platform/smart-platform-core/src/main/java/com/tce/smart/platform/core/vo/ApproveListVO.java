package com.tce.smart.platform.core.vo;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

import com.tce.smart.common.core.vo.BaseVO;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class ApproveListVO extends BaseVO {
    private static final long serialVersionUID = 1L;

    /**
     * 业务ID 离职申请ID或访客申请ID
     */
    private String approveId;

    /**
     * 待审批名称
     */
    private String approveName;

    /**
     * 审批类型 1：离职审批；2：访客审批；
     */
    private String approveType;

    /**
     * 审批状态 0：待审批；1：通过；2：拒绝；
     */
    private String approveState;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 审批员工号
     */
    private String approveBadge;

    /**
     * 审批描述
     */
    private String approveDesc;

    /**
     * 审批项目
     */
    private List<ApproveItemVO> approveItem;

	/**
	 *  访客过了离开时间还未审批的状态
	 */
	private Integer status;
	/**
	 * 审批次序
	 */
	private Integer sort;

	private String articlesTypeDesc;

	private String articleName;

	private String carrier;

	private String roomInfo;

	/**
	 * 审批节点描述
	 */
	private String approveNodeDesc;
}
