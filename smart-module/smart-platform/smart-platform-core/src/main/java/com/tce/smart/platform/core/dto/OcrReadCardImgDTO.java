package com.tce.smart.platform.core.dto;

import com.tce.smart.common.core.ao.BaseAO;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Wechat身份证照片信息Ao
 *
 * @author mingkai.wu
 * @date 2019-05-09 15:13:00
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class OcrReadCardImgDTO extends BaseAO {

	/**
	 * 序列号
	 */
	private static final long serialVersionUID = 2253023160590578553L;

	/**
	 * 招聘岗位Id
	 */
	private Integer id;

	/**
	 * 身份证正面照片
	 */
	private String idCardFrontImg;

	/**
	 * 身份证背面面照片
	 */
	private String idCardBackImg;
}
