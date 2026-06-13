package com.tce.smart.platform.core.vo;

import com.tce.smart.common.core.vo.BaseVO;
import lombok.Data;

@Data
public class DevicePersonVO extends BaseVO {

	/**
	 * 卡牌ID
	 */
	private String cardNo;
	/**
	 * 姓名
	 */
	private String name;

	private String badge;
	/**
	 * 人脸图片
	 */
	private String faceImage;
	/**
	 * 创建时间
	 */
	private String createTime;

	/**
	 * 1:删除中；0：未删除
	 */
	private Integer status;

}
