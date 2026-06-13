package com.tce.smart.platform.core.entity;

import java.time.LocalDateTime;
import java.util.Date;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 设备车辆关联
 *
 * @author 王艳勇
 * @date 2019-04-16 16:06:14
 */
@Data
@TableName("smt_device_vehicle")
@EqualsAndHashCode(callSuper = true)
public class SmtDeviceVehicle extends Model<SmtDeviceVehicle> {
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
   * 车辆ID
   */
    private String vehicleId;
    /**
   * 设备权限ID
   */
    private Integer deviceAuthorityId;
    /**
   * 开始有效时间
   */
    private Date startValidTime;
    /**
   * 结束有效时间
   */
    private Date endValidTime;
    /**
   * 创建时间
   */
    private LocalDateTime createTime;

}
