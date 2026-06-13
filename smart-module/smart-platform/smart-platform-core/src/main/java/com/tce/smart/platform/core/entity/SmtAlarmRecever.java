package com.tce.smart.platform.core.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;

import cn.hutool.core.date.DateTime;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 警报推送人信息表
 *
 * @author 王艳勇
 * @date 2019-04-15 11:34:43
 */
@Data
@TableName("smt_alarm_recever")
@EqualsAndHashCode(callSuper = true)
public class SmtAlarmRecever extends Model<SmtAlarmRecever> {
private static final long serialVersionUID = 1L;

    /**
   * 主键
   */
    @TableId(value = "id", type = IdType.AUTO)
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
   * 接收人姓名
   */
    private String receverName;
    /**
   * 接收人电话
   */
    private Integer receverPhone;
    /**
   * 创建时间
   */
    private DateTime createTime;
    /**
     * 员工号
     */
    private String staffBadge;
    /**
     * 模板ID
     */
    private Integer templateId;

}
