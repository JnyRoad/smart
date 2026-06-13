package com.tce.smart.platform.api.dto.resp;

import com.tce.smart.common.core.vo.BaseVO;
import com.tce.smart.platform.api.dto.SmtStaffDTO;
import com.tce.smart.platform.api.dto.SmtStaffEmergencyDTO;
import lombok.Data;

import java.util.List;


@Data
public class StaffInfoRespDTO extends BaseVO {

	private SmtStaffDTO smtStaff;

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
	List<SmtStaffEmergencyDTO>  smtStaffEmergency;

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
	 * 派遣公司
	 */
	private String pqcompany;
}
