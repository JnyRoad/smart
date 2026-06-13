package com.tce.smart.platform.core.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.validator.constraints.Range;

import javax.validation.constraints.NotNull;
import java.util.Date;

/**
 * 警报记录表
 *
 * @author 王艳勇
 * @date 2019-04-15 14:38:11
 */
@Data
@TableName("smt_alarm_record")
@EqualsAndHashCode(callSuper = true)
public class SmtAlarmRecord extends Model<SmtAlarmRecord> {
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
   * 触发警报的员工ID
   */
    private Long personId;
    /**
   * 触发警报姓名
   */
    private String personName;
    /**
   * 触发人员身份证号
   */
    private String personCertno;
    /**
   * 警报ID
   */
    private Integer alarmId;
    /**
   * 警报类型
   */
    @NotNull
    @Range(min = 1, max = 2, message = "警报类型：1-非法闯入；2-陌生人警报")
    private Integer alarmType;
    /**
   * 警报名称
   */
    private String alarmName;
    /**
   * 设备ID
   */
    private String deviceId;
    /**
   * 设备类型
   */
    private Integer deviceType;
    /**
   * 设备名称
   */
    private String deviceName;
    /**
   * 区域ID
   */
    private Integer areaId;
    /**
   * 区域名称
   */
    private String areaName;
    /**
   * 人员库原图
   */
    private String photoId;
    /**
   * 抓拍原图
   */
    private String snapId;
    /**
   * 抓拍缩略图
   */
    private String thumbnailId;
    /**
   * 相似值
   */
    private String similarity;
    /**
   * 警报时间
   */
    private Date alarmTime;
    /**
   * 创建时间
   */
    private Date createTime;

}
