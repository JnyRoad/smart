package com.tce.smart.platform.api.dto.req;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 添加或修改应聘消息
 */
@Data
public class SaveWechatApplicationReqDTO implements Serializable {

	/**
	 * 应聘id
	 */
	private Long applicatioId;

	/**
	 * 招聘id
	 */
	private Integer recruitId;

	/**
	 * 证件照片base64
	 */
	private String certnoPicture;

	/**
	 * 人脸照片base64
	 */
	private String facePicture;

	/**
	 * 所属园区id
	 */
	private Integer parkId;

	/**
	 * 应聘者姓名
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
	 * 2位数字，根据身份证号码计算年龄
	 */
	private Integer age;

	/**
	 * 民族
	 */
	private String nation;

	/**
	 * 出生年月日
	 */
	private String birth;

	/**
	 * 电话
	 */
	private String phone;

	/**
	 * 微信号
	 */
	private String wechat;

	/**
	 * 家庭住址
	 */
	private String homeAddress;

	/**
	 * 应聘状态 -1-编辑中 0-已投递1-已拒绝 2-已邀请3-待入职/4待复试，暂定/5-已入职 默认是0
	 */
	private Integer status;

	/**
	 * 签证机关
	 */
	private String police;

	/**
	 * 证件有效开始时间
	 */
	private Date validDateFm;

	/**
	 * 证件有效结束时间
	 */
	private Date validDate;


	/**
	 * 证件有效结束时间 存长期
	 */
	private String validDateChar;

	private Integer maritalStatus;


}
