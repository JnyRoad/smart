package com.tce.smart.platform.api.dto.resp.approval;

import com.baomidou.mybatisplus.extension.activerecord.Model;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 *
 *
 * @author fushiping
 * @date 2021-04-08 16:25:18
 */
@Data
public class ApprovalNodeRespDTO extends Model<ApprovalNodeRespDTO> {
private static final long serialVersionUID = 1L;

    /**
   * ID
   */
	@ApiModelProperty("id")
    private Integer id;
    /**
   * 审批id
   */
	@ApiModelProperty("审批id")
    private Integer approvalId;
    /**
   * 节点顺序
   */
	@ApiModelProperty("节点顺序")
    private Integer sort;
    /**
   * 节点名称
   */
	@ApiModelProperty("节点名称")
    private String name;

	@ApiModelProperty("审批触发条件")
    private List<ApprovalConditionRespDTO> conditions;

	@ApiModelProperty("审批人列表")
    private List<ApprovalPersonRespDTO> approvalPersons;

    /**
   * 审批人通过规则
   */
	@ApiModelProperty("审批人通过规则")
    private Integer passRule;
    /**
   * 审批人设置
   */
	@ApiModelProperty("审批人设置")
    private Integer isExistApprover;
    /**
   * APP PUSH消息开关
   */
	@ApiModelProperty("APP PUSH消息开关")
    private Integer isAppPush;
    /**
   * APP站内消息开关
   */
	@ApiModelProperty("APP站内消息开关")
    private Integer isAppIn;
	/**
	 * 是否开启微信推送
	 */
	@ApiModelProperty("微信推送开关")
	private Integer isWeChatPush;
    /**
   * 短信模板
   */
	@ApiModelProperty("短信模板")
    private Integer msgTemplate;

}
