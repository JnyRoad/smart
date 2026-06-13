package com.tce.smart.app.ao.wechat;

import com.tce.smart.common.core.ao.BaseAO;

import lombok.Data;
import lombok.EqualsAndHashCode;

/***
 * 人脸检测
 * @author liangyuan
 *
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class CheckFaceAo extends BaseAO {
	/**
	 * 序列号
	 */
	private static final long serialVersionUID = 8687754044704419673L;


	/**
	 * 访客照片
	 */
	private String visitorPhoto;

	/**
	 * 图片类型，若不传默认为访客人脸图片
	 */
	private Integer photoType;

}
