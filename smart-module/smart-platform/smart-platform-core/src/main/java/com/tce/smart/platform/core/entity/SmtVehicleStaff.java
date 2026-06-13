package com.tce.smart.platform.core.entity;

import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 车辆员工关联表
 *
 * @author 王艳勇
 * @date 2019-04-15 11:33:13
 */
@Data
@TableName("smt_vehicle_staff")
@EqualsAndHashCode(callSuper = true)
public class SmtVehicleStaff extends Model<SmtVehicleStaff> {
private static final long serialVersionUID = 1L;

    /**
   * 主键
   */
    @TableId
    private Integer id;
    /**
   * 车辆表主键
   */
    @JsonFormat(shape=JsonFormat.Shape.STRING)
    private Long vehicleId;
    /**
   * 员工表主键
   */
    @JsonFormat(shape=JsonFormat.Shape.STRING)
    private Long staffId;

}
