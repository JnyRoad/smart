package com.tce.smart.app.service;

import com.tce.smart.app.dto.fore.OcrIdCardDto;

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
	OcrIdCardDto readIdCardFontImg(String fontImg);

	/**
	 * Ocr识别身份证背面照片信息
	 *
	 * @param identificationPhoto
	 * @return
	 */
	OcrIdCardDto readIdCardBackImg(String backImg);
}
