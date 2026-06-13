package com.tce.smart.platform.api.dto.req.approval;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 *
 *
 * @author fushiping
 * @date 2021-04-08 16:25:18
 */
@Data
public class EditApprovalNodeReqDTO extends Model<EditApprovalNodeReqDTO> {
private static final long serialVersionUID = 1L;


	@ApiModelProperty("主键(非必填)")
    private Integer id;
    /**
   * 审批id
   */
	@ApiModelProperty("审批id")
	@NotNull(message = "审批id不可为空")
    private Integer approvalId;
    /**
   * 节点顺序
   */
	@ApiModelProperty("节点顺序")
	@NotNull(message = "节点顺序不可为空")
    private Integer sort;
    /**
   * 节点名称
   */
	@ApiModelProperty("节点名称")
	@NotNull(message = "节点名称不可为空")
    private String name;

	/**
	 * 触发条件
	 */
	@ApiModelProperty("触发条件")
	@NotNull(message = "触发条件不可为空")
	private List<EditApprovalConditionReqDTO> conditions;

	/**
	 * 审批人列表
	 */
	@ApiModelProperty("审批人列表")
	private List<EditApprovalPersonReqDTO> approvalPersons;
    /**
   * 审批人通过规则
   */
	@ApiModelProperty("审批人通过规则")
    private Integer passRule;
    /**
   * 审批人设置
   */
	@ApiModelProperty("审批人设置")
	@NotNull(message = "审批人设置不可为空")
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
