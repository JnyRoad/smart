package com.tce.smart.platform.core.entity;

import javax.validation.constraints.NotBlank;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;

import cn.hutool.core.date.DateTime;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 设备区域关联
 *
 * @author 王艳勇
 * @date 2019-04-15 15:12:58
 */
@Data
@TableName("smt_device_area")
@EqualsAndHashCode(callSuper = true)
public class SmtDeviceArea extends Model<SmtDeviceArea> {
private static final long serialVersionUID = 1L;

    /**
   * 主键
   */
    @TableId
    private Integer id;
    /**
   * 设备ID
   */
    @NotBlank(message = "设备ID不能为空")
    private String deviceId;
    /**
   * 设备区域ID
   */
    @NotBlank(message = "设备区域ID不能为空")
    private Integer areaId;
    /**
   * 创建时间
   */
    private LocalDateTime createTime;

}
