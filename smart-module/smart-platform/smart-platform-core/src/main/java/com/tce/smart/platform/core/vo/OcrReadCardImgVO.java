package com.tce.smart.platform.core.vo;

import java.util.Date;

import com.tce.smart.common.core.vo.BaseVO;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Ocr身份证照片识别结果Vo
 *
 * @author mingkai.wu
 * @date 2019-05-10 16:10:57
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class OcrReadCardImgVO extends BaseVO {

	/**
	 * 序列号
	 */
	private static final long serialVersionUID = -1192313527719429774L;

	/**
	 * 应聘id
	 */
	private String applicationId;

	/**
	 * 应聘岗位id
	 */
	private String recruitId;

	/**
	 * 应聘岗位职层名称
	 */
	private String jcheName;

	/**
	 * 姓名
	 */
	private String name;

	/**
	 * 身份证号
	 */
	private String identification;

	private String cardFrontImg;

	/**
	 * 性别
	 */
	private String gender;

	/**
	 * 民族 OcrReadCardImgVo
	 */
	private String ethnicity;

	/**
	 * 生日
	 */
	private String birthday;

	/**
	 * 家庭住址
	 */
	private String address;

	/**
	 * 签发机关
	 */
	private String signOrg;

	/**
	 * 有效期限
	 */
	private String validityDate;

	private String email;

	private Integer maritalStatus;



}
