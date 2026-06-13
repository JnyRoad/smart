package com.tce.smart.platform.core.vo;

import com.baomidou.mybatisplus.extension.activerecord.Model;
import com.tce.smart.platform.core.entity.SmtStaff;
import com.tce.smart.platform.core.entity.SmtStaffEducation;
import com.tce.smart.platform.core.entity.SmtStaffEmergency;
import com.tce.smart.platform.core.entity.SmtStaffFamily;
import com.tce.smart.platform.core.entity.SmtStaffRelation;
import com.tce.smart.platform.core.entity.SmtStaffWork;

import lombok.Data;

import java.util.List;


@Data
public class StaffInfoVO  extends Model<StaffInfoVO> {

	private  SmtStaff smtStaff;

	/**
	 * 身份证图片base64
	 */
	private String certnoPic;

	/**
	 * 人脸图片base64
	 */
	private String facePic;

	/**
	 * 紧急联系人
	 */
	List<SmtStaffEmergency>  smtStaffEmergency;

	/**
	 * 宿舍状态
	 */
	private Integer dormitoryState;
	/**
	 * 宿舍状态描述
	 */
	private String dormitoryStateDesc;

	/**
	 * 内宿申请状态
	 */
	private Integer applyState;

	/**
	 * 内宿申请状态描述
	 */
	private String applyStateDesc;

	/**
	 * 车辆是否添加
	 */
	private Integer vehicleState;

	/**
	 * 描述
	 */
	private String vehicleStateDesc;

	/**
	 * 员工状态
	 */
	private Integer status;

	/**
	 * 员工状态描述
	 */
	private String statusDes;

	/**
	 * 员工类型
	 */
	private Integer empType;

	/**
	 * 员工类型描述
	 */
	private String empTypeDes;

	/**
	 * 所属园区
	 */
	private String parkName;

	/**
	 * 任职关系
	 */
	List<SmtStaffRelation>  relation;

	/**
	 * 家庭关系
	 */
	List<SmtStaffFamily>  family;

	/**
	 * 教育经历
	 */
	List<SmtStaffEducation>  education;

	/**
	 * 工作经历
	 */
	List<SmtStaffWork>  work;

}
