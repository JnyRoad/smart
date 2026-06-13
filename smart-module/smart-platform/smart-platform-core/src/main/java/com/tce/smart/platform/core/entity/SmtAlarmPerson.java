package com.tce.smart.platform.core.entity;

import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 警报人员关联表
 *
 * @author 王艳勇
 * @date 2019-04-15 11:34:49
 */
@Data
@TableName("smt_alarm_person")
@EqualsAndHashCode(callSuper = true)
public class SmtAlarmPerson extends Model<SmtAlarmPerson> {
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
   * 人员ID
   */
    private Long personId;
    /**
   * 人员类型
   */
    private Integer personType;
    /**
   * 是否警报：1-否；0-是；
   */
    private Integer isAlarm;
    /**
   * 创建时间
   */
    private LocalDateTime createTime;

}
