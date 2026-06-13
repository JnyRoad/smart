package com.tce.smart.algorithm.service;

import com.tce.smart.algorithm.api.dto.resp.FaceFeaturesDTO;
import com.tce.smart.common.core.model.Result;

/**
 * @description: 人脸服务
 * @date: 2020-07-03 15:30
 * @author: wuling
 * @version: 1.0
 */
public interface IFaceService {
	/**
	 * 人脸获取特征值
	 * @param base64Face
	 * @return
	 */
	FaceFeaturesDTO featuresExtract(String  base64Face)throws Exception;
}
