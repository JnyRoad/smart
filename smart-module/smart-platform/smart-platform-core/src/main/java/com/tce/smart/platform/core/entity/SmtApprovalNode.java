package com.tce.smart.platform.core.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 *
 *
 * @author fushiping
 * @date 2021-04-08 16:25:18
 */
@Data
@TableName("smt_approval_node")
@EqualsAndHashCode(callSuper = true)
public class SmtApprovalNode extends Model<SmtApprovalNode> {
private static final long serialVersionUID = 1L;

    /**
   * ID
   */
    @TableId
    private Integer id;
    /**
   * 审批id
   */
    private Integer approvalId;
    /**
   * 节点顺序
   */
    private Integer sort;
    /**
   * 节点名称
   */
    private String name;
    /**
   * 审批人通过规则
   */
    private Integer passRule;
    /**
   * 审批人设置
   */
    private Integer isExistApprover;
    /**
   * APP PUSH消息开关
   */
    private Integer isAppPush;
    /**
   * APP站内消息开关
   */
    private Integer isAppIn;
    /**
   * 短信模板
   */
    private Integer msgTemplate;
    /**
   * 是否开启微信推送
   */
    private Integer isWeChatPush;
    /**
   * 空白字段
   */
    private String blank1;

}
