package com.tce.smart.platform.core.vo;

import com.tce.smart.platform.core.entity.SmtVehicle;

import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotBlank;

/**
 * 车辆及车主详细信息
 * @author Lenovo
 *
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class VehicleVO extends SmtVehicle {
	private static final long serialVersionUID = 1L;

	/**
	 * 员工姓名
	 */
	private String name;

	/**
	 * 员工工号
	 */
	private String badge;

	/**
	 * 部门ID
	 */
	private String depId;

	/**
	 * 部门名称
	 */
	private String depName;

	/**
	 * bu
	 */
	private String compName;

	/**
	 * 身份证号
	 */
	private String certno;

	/**
	 * 男/女，应聘填写时OCR识别出来身份证号码，最后一位顺奇数分配为男性，偶数分配为女性
	 */
	private Integer sex;

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

	private String compId;

	private String staffId;

	private String vehicleTypeName;

	private String vehicleColorName;

	/**
	 * 员工状态 0-已离职 1-在职
	 */
	private Integer staffStatus;

	/**
	 * 福利层次
	 */
	private String jcheName;

}
