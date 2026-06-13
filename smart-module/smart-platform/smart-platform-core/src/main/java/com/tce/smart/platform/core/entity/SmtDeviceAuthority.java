package com.tce.smart.platform.core.entity;

import java.time.LocalDateTime;
import java.util.Date;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 设备权限表
 *
 * @author 王艳勇
 * @date 2019-04-15 15:15:34
 */
@Data
@TableName("smt_device_authority")
@EqualsAndHashCode(callSuper = true)
public class SmtDeviceAuthority extends Model<SmtDeviceAuthority> {
private static final long serialVersionUID = 1L;

    /**
   * 主键
   */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;
    /**
   * 权限名称
   */
    private String authorityName;
    /**
   * 备注
   */
    private String remark;
    /**
   * 创建时间
   */
    private LocalDateTime createTime;

    /**
     * 类型 2：人员； 3：车辆
     */
    private Integer type;

    /**
     * 所属园区
     */
    private Integer parkId;

	/**
	 * 权限性质0-公共区域 1-保密区域
	 */
	private Integer areaType;

	/**
	 * 设备使用类型 1.门禁 2.考勤
	 */
	private Integer deviceUseType;

}
