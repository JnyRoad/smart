package com.tce.smart.platform.core.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 警报策略表
 *
 * @author 王艳勇
 * @date 2019-04-15 11:34:31
 */
@Data
@TableName("smt_alarm_strategy")
@EqualsAndHashCode(callSuper = true)
public class SmtAlarmStrategy extends Model<SmtAlarmStrategy> {
private static final long serialVersionUID = 1L;

    /**
   * 主键
   */
    @TableId
    private Integer id;
    /**
   * 园区主键
   */
    private Integer parkId;
    /**
   * 警报ID
   */
    private Integer alarmId;
    /**
   * 布控开始时间
   */
    private LocalDateTime startTime;
    /**
   * 布控结束时间
   */
    private LocalDateTime endTime;
    /**
   * 周期（单位：分钟）
   */
    private Integer deviceId;
    /**
   * 频次
   */
    private Integer frequency;
    /**
   * 识别阈值
   */
    private BigDecimal threshold;
    /**
   * 是否从长期有效：1-否；0-是；
   */
    private Integer isPermanent;
    /**
   * 是否报警：1-否；0-是；
   */
    private Integer isAlarm;
    /**
   * 创建时间
   */
    private LocalDateTime createTime;

}
