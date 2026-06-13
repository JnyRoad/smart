package com.tce.smart.algorithm.config;

import com.google.common.collect.Maps;
import com.tce.smart.algorithm.api.enums.AlgorithmTypeEnum;
import com.tce.smart.algorithm.constant.AlgorithmConstants;
import com.tce.smart.algorithm.service.*;
import com.tce.smart.algorithm.service.impl.*;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

/**
 * @ClassName: UploaderConfiguration
 * @Package com.tce.smart.certification.config.properties
 * @Description:
 * @Author wuxinjian
 * @Date 2020/2/26 11:36
 * @Version V1.0
 */
@Configuration
@AllArgsConstructor
public class AlgorithmHandlerConfiguration {

	private final AlgorithmConfigService algorithmConfigService;

	private final OcrDisServiceImpl ocrWenTongService;

	private final FaceDetectFaceAllServiceImpl faceDetectFaceAllService;

	private final CompareFaceAllServiceImpl compareFaceAllService;

	@Bean
	public Map<String, IOcrService> ocrHandlers() {
		Map<String, IOcrService> handlers = Maps.newConcurrentMap();
		algorithmConfigService.getList(AlgorithmConstants.OCR_ALGORITHM_PREFIX).forEach(algorithm -> {
			if (algorithm.getAlgorithmType().startsWith(AlgorithmTypeEnum.OCR_WENTONG.getType())) {
				handlers.put(algorithm.getAlgorithmType(), ocrWenTongService);
			}
		});
		return handlers;
	}

	@Bean
	public Map<String, IFaceDetectService> faceDetectHandlers() {
		Map<String, IFaceDetectService> handlers = Maps.newConcurrentMap();
		algorithmConfigService.getList(AlgorithmConstants.FACE_DETECT_ALGORITHM_PREFIX).forEach(algorithm -> {
			if (algorithm.getAlgorithmType().startsWith(AlgorithmTypeEnum.FACEDETECT_FACEALL.getType())) {
				handlers.put(algorithm.getAlgorithmType(), faceDetectFaceAllService);
			}
		});
		return handlers;
	}


	@Bean
	public Map<String, ICompareService> compareHandlers() {
		Map<String, ICompareService> handlers = Maps.newConcurrentMap();
		algorithmConfigService.getList(AlgorithmConstants.COMPARE_ALGORITHM_PREFIX).forEach(algorithm -> {
			if (algorithm.getAlgorithmType().startsWith(AlgorithmTypeEnum.COMPARE_FACEALL.getType())) {
				handlers.put(algorithm.getAlgorithmType(), compareFaceAllService);
			}
		});
		return handlers;
	}
}
