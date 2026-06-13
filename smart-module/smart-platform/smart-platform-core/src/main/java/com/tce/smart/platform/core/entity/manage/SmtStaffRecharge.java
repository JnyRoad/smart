package com.tce.smart.platform.core.entity.manage;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 *
 *
 * @author fushiping
 * @date 2020-07-17 16:51:38
 */
@Data
@TableName("SMT_STAFF_RECHARGE")
@EqualsAndHashCode(callSuper = true)
public class SmtStaffRecharge extends Model<SmtStaffRecharge> {
private static final long serialVersionUID = 1L;

    /**
   * id
   */
	@TableId(type = IdType.ID_WORKER)
    private Long id;
    /**
   * 员工工号
   */
    private String badge;
    /**
   * 餐补结算
   */
    private BigDecimal account;
    /**
   * 结算时间
   */
	@TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    /**
   * 同步状态
   */
    private Integer syncStatus;
    /**
   * 备用字段
   */
    private String blank;
    /**
   * 考勤月份
   */
    private String checkMonth;
    /**
   * 应出勤
   */
    private Double shouldOn;
    /**
   * 实出勤
   */
    private Double actualOn;

    /**
   * 餐补标准
   */
    private BigDecimal standard;

	/**
	 * 名单类型
	 */
    private Integer rechargeType;

}
