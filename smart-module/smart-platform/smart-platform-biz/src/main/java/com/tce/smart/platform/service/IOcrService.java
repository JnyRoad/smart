package com.tce.smart.platform.service;

import com.tce.smart.platform.core.dto.OcrIdCardDTO;

/**
 *
 * @ClassName AppOaManagerService.java
 * @Author mingkai.wu
 * @Date 2019-04-29 09:43
 * @Description
 */
public interface IOcrService {

	/**
	 * Ocr识别身份证正面照片信息
	 *
	 * @param identificationPhoto
	 * @return
	 */
	OcrIdCardDTO readIdCardFontImg(String fontImg);

	/**
	 * Ocr识别身份证背面照片信息
	 *
	 * @param identificationPhoto
	 * @return
	 */
	OcrIdCardDTO readIdCardBackImg(String backImg);
}
