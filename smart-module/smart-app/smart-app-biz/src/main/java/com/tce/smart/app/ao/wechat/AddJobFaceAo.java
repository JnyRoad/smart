package com.tce.smart.app.ao.wechat;

import com.tce.smart.common.core.ao.BaseAO;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Wechat岗位应聘上传人脸Ao
 *
 * @author mingkai.wu
 * @date 2019-05-09 15:13:00
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class AddJobFaceAo extends BaseAO {

	/**
	 * 序列号
	 */
	private static final long serialVersionUID = 2938173266332492810L;

	/**
	 * 应聘id
	 */
	private String applicationId;

	/**
	 * 人脸照片
	 */
	private String facePhoto;
}
