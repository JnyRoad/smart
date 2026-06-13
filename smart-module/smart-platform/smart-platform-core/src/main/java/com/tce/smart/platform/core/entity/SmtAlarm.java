package com.tce.smart.platform.core.entity;

import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 警报信息记录
 *
 * @author 王艳勇
 * @date 2019-04-15 11:34:58
 */
@Data
@TableName("smt_alarm")
@EqualsAndHashCode(callSuper = true)
public class SmtAlarm extends Model<SmtAlarm> {
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
   * 告警类型：待确认
   */
    private Integer alarmType;
    /**
   * 告警名称
   */
    private String alarmName;
    /**
   * 地点ID
   */
    private Integer areaId;
    /**
   * 短信模板ID
   */
    private Integer templateId;
    /**
   * 是否启动：1-禁用；0-启用；
   */
    private Integer isEnable;
    /**
   * 是否删除：1-是；0-否；
   */
    private Integer isDelete;
    /**
   * 创建时间
   */
    private LocalDateTime createTime;

}
