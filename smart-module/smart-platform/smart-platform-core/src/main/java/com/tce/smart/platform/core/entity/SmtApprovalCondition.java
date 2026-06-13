package com.tce.smart.platform.core.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 *
 *
 * @author fushiping
 * @date 2021-04-08 16:25:24
 */
@Data
@TableName("smt_approval_condition")
@EqualsAndHashCode(callSuper = true)
public class SmtApprovalCondition extends Model<SmtApprovalCondition> {
private static final long serialVersionUID = 1L;

    /**
   *
   */
    @TableId
    private Integer id;
    /**
   * 节点id
   */
    private Integer nodeId;
    /**
   * 条件类型code
   */
    private Integer conditionType;
    /**
   * 条件比较符
   */
    private Integer comparator;
    /**
   * 对比值
   */
    private String compareValue;
    /**
   * 连接符
   */
    private Integer connector;
    /**
   * 条件顺序
   */
    private Integer sort;
    /**
   * 空白字段
   */
    private String blank1;
    /**
   * 空白字段
   */
    private String blank2;

}
