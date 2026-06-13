package com.tce.smart.platform.core.entity;

import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 设备人员关联
 *
 * @author 王艳勇
 * @date 2019-04-15 15:15:38
 */
@Data
@TableName("smt_device_person")
@EqualsAndHashCode(callSuper = true)
public class SmtDevicePerson extends Model<SmtDevicePerson> {
private static final long serialVersionUID = 1L;

    /**
   * 主键
   */
    @TableId
    private Integer id;
    /**
   * 设备ID
   */
    private String deviceId;
    /**
   * 员工ID
   */
    private Long staffId;
    /**
   * 策略ID
   */
    private Integer deviceAuthorityId;

    /**
     * 人员类型 1：员工；2：访客
     */
    private Integer personType;
    /**
   * 创建时间
   */
    private LocalDateTime createTime;

}
