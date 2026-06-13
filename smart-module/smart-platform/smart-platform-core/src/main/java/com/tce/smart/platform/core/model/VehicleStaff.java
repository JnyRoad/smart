package com.tce.smart.platform.core.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.tce.smart.common.core.vo.BaseVO;

import com.tce.smart.platform.core.entity.SmtPark;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class VehicleStaff extends BaseVO {

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
	 * 员工所属园区id
	 */
	private Integer parkId;

	/**
	 * 员工工号
	 */
	private String badge;
	/**
	 * 员工姓名
	 */
	private String name;
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

	/**
	 * BUId
	 */
	private String compId;
	/**
	 * buname
	 */
	private String compName;
	/**
	 * 部门ID
	 */
	private String depId;
	/**
	 * 部门名称
	 */
	private String depName;

	/**
	 * 岗位Id
	 */
	private String jobId;
	/**
	 * 岗位名称
	 */
	private String jobName;

	private Integer status;

	private String statusDesc;

	private List<SmtPark> parks;
}
