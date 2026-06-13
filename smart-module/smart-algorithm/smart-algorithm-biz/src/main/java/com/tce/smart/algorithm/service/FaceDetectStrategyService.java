package com.tce.smart.algorithm.service;

import com.tce.smart.algorithm.constant.AlgorithmConstants;
import com.tce.smart.common.core.exception.SmartException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * @ClassName: ILivenessStaticService
 * @Package com.tce.smart.algorithm.service
 * @Description: 静默活体算法接口
 * @Author wuxinjian
 * @Date 2019-10-10 10:12
 * @Version V1.0
 */
@Slf4j
@Component
@AllArgsConstructor
public class FaceDetectStrategyService {

	private Map<String, IFaceDetectService> handlers;

	private IFaceDetectService handler(final String algorithmType) {
		return handlers
				.entrySet()
				.stream()
				.filter(h -> h.getValue().handler().equals(algorithmType))
				.findFirst()
				.orElseThrow(() -> new SmartException(AlgorithmConstants.UNKNOWN_ALGORITHM_TYPE))
				.getValue();
	}

	public String faceDetect(String id, String algorithmType, Integer faceDetectType, String imageBase64) {
		return handler(algorithmType).faceDetect(id, faceDetectType, imageBase64);
	}
}
