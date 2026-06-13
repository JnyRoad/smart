package com.tce.smart.platform.api.dto.resp.approval;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
public class ApproveProcessListReqDTO {
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
     * 审批员工号
     */
    private String approveBadge;

	/**
	 * 审批次序
	 */
    private Integer sort;
	/**
	 * 审批人通过规则
	 */
	private Integer passRule;
	/**
	 * APP PUSH消息开关
	 */
	private Integer isAppPush;

	/**
	 * 微信推送开关
	 */
	private Integer isWeChatPush;
	/**
	 * 短信模板
	 */
	private Integer msgTemplate;
	/**
	 * 节点id
	 */
	private Integer nodeId;

	private Integer IsExistApprover;
}
