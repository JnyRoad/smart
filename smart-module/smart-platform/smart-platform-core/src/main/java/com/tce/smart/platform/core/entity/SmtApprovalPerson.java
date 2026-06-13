package com.tce.smart.platform.core.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 *
 *
 * @author fushiping
 * @date 2021-04-08 16:25:00
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("smt_approval_person")
@EqualsAndHashCode(callSuper = true)
public class SmtApprovalPerson extends Model<SmtApprovalPerson> {
private static final long serialVersionUID = 1L;

    /**
   * ID
   */
    @TableId
    private Integer id;
    /**
   * 节点id
   */
    private Integer nodeId;
    /**
   * 审批人工号
   */
    private String approverBadge;
    /**
   * 审批人姓名
   */
    private String approverName;
    /**
   * 审批人顺序
   */
    private Integer sort;
    /**
   * 审批结果
   */
    private Integer result;
    /**
   * 空白字段1
   */
    private String blank1;
    /**
   * 空白字段2
   */
    private String blank2;

}
