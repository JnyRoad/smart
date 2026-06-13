package com.tce.smart.app.ao.wechat;

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
public class OcrReadCardImgAo extends BaseAO {

	/**
	 * 序列号
	 */
	private static final long serialVersionUID = 2253023160590578553L;

	/**
	 * 招聘岗位Id
	 */
	private Integer recruitId;

	/**
	 * 招聘岗位职层名称
	 */
	private String jcheName;

	/**
	 * 身份证正面照片
	 */
	private String idCardFrontImg;

	/**
	 * 身份证背面面照片
	 */
	private String idCardBackImg;
}
