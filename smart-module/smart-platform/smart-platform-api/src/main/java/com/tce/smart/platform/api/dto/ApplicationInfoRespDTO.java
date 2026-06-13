package com.tce.smart.platform.api.dto;

import com.tce.smart.common.core.vo.BaseVO;
import lombok.Data;

import java.util.List;

/**
 * 应聘者详情查询返回的信息
 */
@Data
public class ApplicationInfoRespDTO extends BaseVO {


	/**
	 * 应聘者基本信息表
	 */
	private SmtApplicationDTO application;
	/**
	 * 应聘者的所属招聘信息
	 */
	private SmtRecruitmentDTO recruitment;


	/**
	 * 身份证图片base64
	 */
	private String certnoPic;

	/**
	 * 人脸图片base64
	 */
	private String facePic;

	/**
	 * 园区名称
	 */
	private String parkName;

	/**
	 *学历
	 */
	private String applicantEducation;

	/**
	 * 教育经验
	 */

	List<SmtApplicationEducationDTO> applicationEducation;


	/**
	 * 工作经验
	 */

	List<SmtApplicationWorkDTO> applicationWork;

	/**
	 * 紧急联系人
	 */

	List<SmtApplicationEmergencyDTO> applicationEmergency;


	/**
	 * 人事关系
	 */

	List<SmtApplicationRelationDTO> applicationRelation;


}
