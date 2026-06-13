package com.tce.smart.platform.core.entity;

import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 警报设备表
 *
 * @author 王艳勇
 * @date 2019-04-15 11:34:54
 */
@Data
@TableName("smt_alarm_device")
@EqualsAndHashCode(callSuper = true)
public class SmtAlarmDevice extends Model<SmtAlarmDevice> {
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
   * 设备类型：1-门禁；2-闸机；3-道闸；4-摄像头；
   */
    private Integer deviceType;
    /**
   * 设备ID
   */
    private Integer deviceId;
    /**
   * 地点ID
   */
    private Integer areaId;
    /**
   * 创建时间
   */
    private LocalDateTime createTime;

}
