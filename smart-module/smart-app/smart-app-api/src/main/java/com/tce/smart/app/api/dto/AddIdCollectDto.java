package com.tce.smart.app.api.dto;

import java.io.Serializable;

import lombok.Data;

/**
 * App员工人脸信息采集
 *
 * @author mkwu
 * @date 2019-07-31
 */
@Data
public class AddIdCollectDto implements Serializable {

	/**
	 * 序列号
	 */
	private static final long serialVersionUID = -2591637256657898721L;

	/**
	 * 员工号
	 */
	private String badge;

	/**
	 * 人脸照片
	 */
	private String facePhoto;

	/**
	 * 人脸照片同步C6状态
	 */
	private String photoSyncFla;
}
