package com.tce.smart.app.vo.fore;

import java.util.Date;
import java.util.List;

import lombok.Data;

/**
 * 简历详情
 * @author qipei
 *
 */
@Data
public class ApplicationDetailVo {

	private String applicationId;

	/**
	 * 岗位名称
	 */
	private  String  jobName;



	/**
	 * 工作部门
	 */
	private String jobDept;

	/**
	 * 姓名
	 */
	private String applicantName;
	/**
	 * 职层
	 */

	private String applicationJche;

	/**
	 * 性别
	 */
	private String applicantGender;


	/**
	 * 年龄
	 */
	private Integer applicantAge;

	/**
	 * 民族
	 */
	private String applicantNation;

	/**
	 * 学历
	 */
	private String applicantEducation;

	/**
	 * 工作年限
	 */
	private Integer workAge;


	/**
	 * 应聘者照片
	 */
	private String applicantPhoto;

	/**
	 * 工作地
	 */
	private String jobAddress;

	/**
	 * 计算机等级
	 */
	private String computerLevel;

	private String language;


	private String applicantAddress;


	private String applicantMobile;


	private String applyDate;


	private  List<EducationVo> educationHis;

	private   List<WorkVo> workHis;




}
