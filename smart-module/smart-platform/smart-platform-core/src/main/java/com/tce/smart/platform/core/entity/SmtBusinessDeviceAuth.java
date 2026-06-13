package com.tce.smart.platform.core.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 *
 *
 * @author fushiping
 * @date 2020-08-05 18:22:56
 */
@Data
@TableName("smt_business_device_auth")
@EqualsAndHashCode(callSuper = true)
public class SmtBusinessDeviceAuth extends Model<SmtBusinessDeviceAuth> {
private static final long serialVersionUID = 1L;

    /**
   * ID
   */
	@TableId(type = IdType.ID_WORKER)
    private Long id;
    /**
   * 业务code
   */
    private Integer businessCode;
    /**
   * 权限配置id
   */
    private Integer authId;
    /**
   * 园区id
   */
    private Integer parkId;

    private Integer type;

	/**
	 * 部门ID
	 */
	private String depId;

}
