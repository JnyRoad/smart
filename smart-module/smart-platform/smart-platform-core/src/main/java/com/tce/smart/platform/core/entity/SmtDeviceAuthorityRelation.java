package com.tce.smart.platform.core.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 设备权限关联表
 *
 * @author 王艳勇
 * @date 2019-04-15 15:15:34
 */
@Data
@TableName("smt_device_authority_relation")
@EqualsAndHashCode(callSuper = true)
public class SmtDeviceAuthorityRelation extends Model<SmtDeviceAuthorityRelation> {
private static final long serialVersionUID = 1L;

    /**
   * 主键
   */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;
    /**
   * 权限ID
   */
    private Integer authorityId;
    /**
   * 设备ID
   */
    private String deviceId;

    /**
     * 园区ID
     */
    private Integer parkId;

}
