package com.tce.smart.platform.core.vo;

import lombok.Data;

/**
 * 根据人脸获取简历结果
 * @author qipei
 *
 */
@Data
public class FaceApplicationVO {
	/**
	 *应聘id
	 */
	private String applicationId;
	/**
	 * 岗位
	 */
	private String  jobName;
	/**
	 * 工作地址
	 */
	private String jobAddress;
	/**
	 * 工作部门
	 */
	private String  jobDept;
	/**
	 * 姓名
	 */
	private String applicantName;
	/**
	 * 性别
	 */
	private String  applicantGender;
	/**
	 * 年龄
	 */
	private Integer  applicantAge;
	/**
	 * 教育
	 */
	private String  applicantEducation;

	/**
	 * 图片base64
	 */
	private String applicantPhoto;


}
