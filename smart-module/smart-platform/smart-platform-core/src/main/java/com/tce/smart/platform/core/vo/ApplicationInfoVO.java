package com.tce.smart.platform.core.vo;

import com.baomidou.mybatisplus.extension.activerecord.Model;
import com.tce.smart.platform.api.dto.SmtApplicationDTO;
import com.tce.smart.platform.api.dto.SmtRecruitmentDTO;
import com.tce.smart.platform.core.entity.*;
import lombok.Data;

import java.util.List;

/**
 * 应聘者详情查询返回的信息
 */
@Data
public class ApplicationInfoVO  extends Model<ApplicationInfoVO> {

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
	 * 投递时间
	 */
//	private Date applyDate;

	private String applicantEducation;

	/**
	 * 教育经验
	 */

	List<SmtApplicationEducation> applicationEducation;


	/**
	 * 工作经验
	 */

	List<SmtApplicationWork> applicationWork;

	/**
	 * 紧急联系人
	 */

	List<SmtApplicationEmergency> applicationEmergency;


	/**
	 * 人事关系
	 */

	List<SmtApplicationRelation> applicationRelation;


}
