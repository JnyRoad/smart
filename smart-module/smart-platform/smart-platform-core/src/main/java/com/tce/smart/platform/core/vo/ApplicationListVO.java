package com.tce.smart.platform.core.vo;

import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;

import java.util.Date;

/**
 * app查询岗位返回的信息
 */
@Data
public class ApplicationListVO extends Model<ApplicationListVO>{

	private String applicationId;

	/**
	 * 岗位名称
	 */
	private  String  jobName;

	/**
	 * 职层
	 */
	private String jcheName;

	/**
	 * 应聘者照片
	 */
	private String applicantPhoto;

	/**
	 * 工作地
	 */
	private String jobAddress;

	/**
	 * 工作部门
	 */
	private String jobDept;

	/**
	 * 姓名
	 */
	private String applicantName;


	/**
	 * 性别
	 */
	private String applicantGender;


	/**
	 * 年龄
	 */
	private Integer applicantAge;

	/**
	 * 学历
	 */
	private String applicantEducation;

	/**
	 * 状态
	 */
	private Integer status;

	/**
	 * 投递时间
	 */
	private Date applyDate;

	/**
	 * 园区id
	 */
	private Integer parkId;

	/**
	 * 园区名
	 */
	private String parkName;
}
