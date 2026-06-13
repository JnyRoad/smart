package com.tce.smart.platform.core.dto;



import lombok.Data;

import java.util.List;

@Data
public class SearchStaffDTO {


	/**
	 * 员工姓名
	 */
	private String name;

	/**
	 * 员工工号
	 */
	private String badge;

	/**
	 * 岗位ID
	 */
	private String jobId;
	/**
	 * 岗位名称
	 */
	private String jobName;
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
	 * 职层ID
	 */
	private String jcheId;
	/**
	 * 职层名称
	 */
	private String jcheName;

	private String depAbbr;

	/**
	 * 福利层次
	 */
	private String welfareLevel;

	/**
	 * 人脸照片id
	 */
	private String facePicId;

	/**
	 * 身份证号
	 */
	private String certno;
	/**
	 * 男/女，应聘填写时OCR识别出来身份证号码，最后一位顺奇数分配为男性，偶数分配为女性
	 */
	private Integer sex;
	/**
	 * 2位数字，根据身份证号码计算年龄
	 */
	private Integer age;
	/**
	 * 电话
	 */
	private String phone;

	/**
	 * 员工状态 0-已离职 1-在职
	 */
	private Integer status;


	 /**
     * 住宿状态  0-未住宿  1-内宿  2-外宿
     */
    private Integer  dormitoryStatus;

    /**
     * 园区id
     */
    private Integer parkId;

    private String endTime;

	private String startTime;

	private String badges;

	private List<String> badgeList;
}
