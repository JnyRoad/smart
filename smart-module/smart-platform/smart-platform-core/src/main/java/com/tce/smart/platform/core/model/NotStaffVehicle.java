package com.tce.smart.platform.core.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.tce.smart.common.core.vo.BaseVO;

import com.tce.smart.platform.core.entity.SmtDeviceAuthority;
import com.tce.smart.platform.core.entity.SmtPark;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class NotStaffVehicle extends BaseVO {/**
	 *
	 */
	private static final long serialVersionUID = -5083432977026814344L;

    /**
   * 主键
   */
	@JsonFormat(shape=JsonFormat.Shape.STRING)
    private Long id;
    /**
   * 车牌号
   */
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

    private String name;

    private String phone;

    private String remark;

	/**
	 * 园区名称
	 */
	private String parkName;

	/**
	 * 园区ID
	 */
	private String parkId;

	private List<SmtPark> parkList;

	/**
	 * 权限名称
	 * */
	private String authorityName;

	/**
	 * 权限名称
	 */
	private Integer authorityId;

	/**
	 * 权限列表
	 */
	private List<SmtDeviceAuthority> auths;

}
