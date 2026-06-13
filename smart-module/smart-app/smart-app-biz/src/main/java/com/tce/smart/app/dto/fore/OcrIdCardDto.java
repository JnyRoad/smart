package com.tce.smart.app.dto.fore;

import lombok.Data;

/**
 * Ocr识别解析身份证信息Dto
 *
 * @author mingkai.wu
 * @date 2019-05-09 15:17:02
 */
@Data
public class OcrIdCardDto {

	/**
	 * 信息收集表主键id
	 */
	private Integer id;

	/**
	 * 裕同员工号
	 */
	private String staffId;

	/**
	 * 身份证号
	 */
	private String identityCard;

	/**
	 * 姓名
	 */
	private String name;

	/**
	 * 性别
	 */
	private String gender;

	/**
	 * 民族
	 */
	private String ethnicity;

	/**
	 * 出生日期
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
	 * 签发日期
	 */
	private String signDate;

	/**
	 * 有效期限
	 */
	private String validityDate;

	/**
	 * 有效期至
	 */
	private String validityEndDate;

	/**
	 * 身份证正面照片
	 */
	private String idCardFrontPhoto;

	/**
	 * 身份证背面照片
	 */
	private String idCardBackPhoto;

	/**
	 * 人脸照片
	 */
	private String facePhoto;
}
