package com.tce.smart.platform.core.vo;

import java.util.Date;
import java.util.List;

import com.tce.smart.platform.core.entity.SmtApplication;
import com.tce.smart.platform.core.entity.SmtApplicationWork;
import com.tce.smart.platform.core.entity.SmtRecruitment;

import lombok.Data;
/**
 *管理平台展示应聘详情
 * @author qipei
 *
 */
@Data
public class SmtApplicationDetailVO {


	/**
	 * 应聘者基本信息表
	 */
	private SmtApplication application;
	/**
	 * 应聘者的所属招聘信息
	 */
	private SmtRecruitment recruitment;


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
	private Date applyDate;

	private String applicantEducation;

	/**
	 * 工作邮箱
	 */
	private String  applicantEmail;

	/**
	 * 教育经验
	 */

	List<ApplicationEducationVO> applicationEducation;


	/**
	 * 工作经验
	 */

	List<SmtApplicationWork> applicationWork;

	/**
	 * 紧急联系人
	 */

	List<RelationVO> applicationEmergency;


	/**
	 * 人事关系
	 */

	List<OrgrelationVO> applicationRelation;

	/**
	 * 家庭成员
	 */
	List<FamilyMemberVO> applicationFamilyMember;

}
