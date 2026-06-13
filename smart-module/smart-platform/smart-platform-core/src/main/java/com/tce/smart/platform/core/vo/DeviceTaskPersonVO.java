package com.tce.smart.platform.core.vo;

import com.tce.smart.common.core.vo.BaseVO;
import lombok.Data;

@Data
public class DeviceTaskPersonVO extends BaseVO {

	private String cardNo;
	/**
	 * 姓名
	 */
	private String name;
	/**
	 * 人脸图片
	 */
	private String faceImage;

}
