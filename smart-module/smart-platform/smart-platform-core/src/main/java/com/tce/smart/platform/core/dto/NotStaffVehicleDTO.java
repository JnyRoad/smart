package com.tce.smart.platform.core.dto;

import java.util.Date;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 车辆信息表
 *
 * @author 王艳勇
 * @date 2019-04-15 11:33:02
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class NotStaffVehicleDTO extends Model<NotStaffVehicleDTO> {
private static final long serialVersionUID = 1L;

    /**
   * 主键
   */
    @TableId(value = "id", type = IdType.ID_WORKER)
    @JsonFormat(shape=JsonFormat.Shape.STRING)
    private Long id;
    /**
   * 园区ID
   */
    private String parkId;
    /**
   * 车牌号
   */
    @NotBlank(message = "车牌号不能为空")
    @Pattern(regexp = "^(([京津沪渝冀豫云辽黑湘皖鲁新苏浙赣鄂桂甘晋蒙陕吉闽贵粤青藏川宁琼使领][A-Z](([0-9]{5}[DF])|([DF]([A-HJ-NP-Z0-9])[0-9]{4})))|([京津沪渝冀豫云辽黑湘皖鲁新苏浙赣鄂桂甘晋蒙陕吉闽贵粤青藏川宁琼使领][A-Z][A-HJ-NP-Z0-9]{4,5}[A-HJ-NP-Z0-9挂学警港澳使领]))$",message = "车牌号格式不正确")
    private String vehiclePlate;
    /**
   * 车辆品牌
   */
    private String vehicleBrand;
    /**
   * 车辆颜色
   */
    private Integer vehicleColor;
    /**
   * 车辆类型:(区分轿车、货车等)
   */
    private Integer vehicleType;
    /**
   * 驾驶证图片ID
   */
    private String driverLicenseId;
    /**
   * 行驶证图片ID
   */
    private String drivinglLicenseId;
    /**
   * 车辆归属分类：0:园区车辆；1：员工车辆；2：访客车辆；3：物流车辆 ;5:非员工车辆
   */
    private Integer vehicleAscription;
    /**
   * 是否删除：0未删；1：删除
   */
    private Integer isDelete;
    /**
   * 创建时间
   */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone = "GMT+8")
    private Date createTime;

	/**
	 * 员工姓名
	 */
    @NotBlank(message = "姓名不能为空")
	private String name;

	/**
	 * 电话
	 */
	@NotBlank(message = "手机号不能为空")
	private String phone;

	/**
	 * 备注
	 */
	private String remark;

	/**
	 *  车辆的权限ID
	 */
	private Integer authorityId;
}
