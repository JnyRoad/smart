package com.tce.smart.app.dto.fore;

import cn.hutool.core.util.ImageUtil;
import lombok.Data;

/**
 * 发送Ocr服务Dto
 *
 * @author mingkai.wu
 * @date 2019-05-09 15:16:45
 */
@Data
public class OcrSendDto {
	/**
	 * Oce服务用户名
	 */
	private String username;

	/**
	 * Ocr服务请求消息体
	 */
	private String paramdata;

	/**
	 * 消息签名
	 */
	private String signdata = "NULL";

	/**
	 * 图片类型
	 */
	private String imgtype = ImageUtil.IMAGE_TYPE_JPG;
}
