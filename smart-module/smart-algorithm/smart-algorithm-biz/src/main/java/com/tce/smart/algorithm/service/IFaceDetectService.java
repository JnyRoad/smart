package com.tce.smart.algorithm.service;

/**
 * @ClassName: ILivenessStaticService
 * @Package com.tce.smart.algorithm.service
 * @Description: 静默活体算法接口
 * @Author wuxinjian
 * @Date 2019-10-10 10:12
 * @Version V1.0
 */
public interface IFaceDetectService {


    /**
     * 人脸检测算法
     * @param id
	 * @param faceDetectType
     * @param imageBase64
     * @return
     */
	String faceDetect(String id, Integer faceDetectType, String imageBase64);

	/**
	 * 处理器
	 * @return
	 */
	String handler();
}
