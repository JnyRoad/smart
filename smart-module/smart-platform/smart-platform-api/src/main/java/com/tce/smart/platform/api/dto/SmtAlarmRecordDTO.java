package com.tce.smart.platform.api.dto;

import lombok.Data;
import org.hibernate.validator.constraints.Range;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Date;

/**
 * 警报记录表
 *
 * @author 王艳勇
 * @date 2019-04-15 14:38:11
 */
@Data
public class SmtAlarmRecordDTO implements Serializable {
private static final long serialVersionUID = -5711638455748963620L;

    /**
   * 主键
   */
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
