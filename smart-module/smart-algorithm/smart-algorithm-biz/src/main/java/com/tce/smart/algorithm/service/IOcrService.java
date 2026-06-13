package com.tce.smart.algorithm.service;

/**
 * @ClassName: IOcrService
 * @Package com.tce.smart.algorithm.service
 * @Description: OCR识别算法接口
 * @Author wuxinjian
 * @Date 2019-10-10 10:12
 * @Version V1.0
 */
public interface IOcrService {


	/**
	 * OCR识别
	 * @param id 全网唯一ID
	 * @param imageBase64
	 * @param cardType
	 * @return  String
	 */
	String ocr(String id, String cardType, String imageBase64);

	/**
	 * 处理器
	 * @return
	 */
	String handler();
}
