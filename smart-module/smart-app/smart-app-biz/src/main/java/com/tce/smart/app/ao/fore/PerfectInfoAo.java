package com.tce.smart.app.ao.fore;

import com.tce.smart.common.core.ao.BaseAO;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * App信息完善Ao
 *
 * @author mingkai.wu
 * @date 2019-05-09 15:13:00
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class PerfectInfoAo extends BaseAO {

	/**
	 * 序列号
	 */
	private static final long serialVersionUID = -442328314351924863L;

	/**
	 * 信息采集编号
	 */
	private Integer perfectId;

	/**
	 * 身份证正面照片
	 */
	private String identificationPhoto;

	/**
	 * 人脸照片
	 */
	private String facePhoto;

	/**
	 * 设备编号
	 */
	private String deviceNo;
}
