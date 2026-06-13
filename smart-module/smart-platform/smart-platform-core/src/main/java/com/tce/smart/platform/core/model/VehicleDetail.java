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
public class VehicleDetail extends BaseVO {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;
	/**
	*
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
    private String vehicleColorName;
    /**
   * 车辆类型:(区分轿车、货车等)
   */
    private Integer vehicleType;
    private String vehicleTypeName;
    /**
   * 驾驶证图片ID
   */
    private String driverLicenseId;
    /**
   * 行驶证图片ID
   */
    private String drivinglLicenseId;

    /**
	 * 员工姓名
	 */
	private String name;

	/**
	 * 员工工号
	 */
	private String badge;

	/**
	 * 部门名称
	 */
	private String depName;

	/**
	 * 身份证号
	 */
	private String certno;

	/**
	 * 男/女，应聘填写时OCR识别出来身份证号码，最后一位顺奇数分配为男性，偶数分配为女性
	 */
	private Integer sex;
	private String sexName;

	/**
	 * 电话
	 */
	private String phone;

	/**
	 * 人脸照片id
	 */
	private String facePicId;

	/**
	 * 园区名称
	 */
	private String parkName;

	private String authorityName;

	private String compId;
	private String compName;
	/**
	 * 部门ID
	 */
	private String depId;
	private String staffId;

	private List<SmtPark> parks;

	private String parkId;

	private Integer authorityId;

	private List<SmtDeviceAuthority> auths;
}
