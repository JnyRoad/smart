package com.tce.smart.platform.core.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;
import java.util.Date;

@Data
@TableName("smt_approve_list")
@EqualsAndHashCode(callSuper = true)
public class ApproveList extends Model<ApproveList> {
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
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
	 * 备注
	 */
    private String remark;

	/**
	 * 审批时间
	 */
    private LocalDateTime updateTime;

	/**
	 * 审批次序
	 */
    private Integer sort;
	/**
	 * 审批人通过规则
	 */
	private Integer passRule;

	/**
	 * 节点id
	 */
	private Integer nodeId;
}
