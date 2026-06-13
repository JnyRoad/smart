package com.tce.smart.algorithm.service;

import com.tce.smart.algorithm.constant.AlgorithmConstants;
import com.tce.smart.common.core.exception.SmartException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import java.util.Map;

/**
 * OCR策略类
 * @author wxjason
 */
@Slf4j
@Component
@AllArgsConstructor
public class OcrStrategyService {

	private Map<String, IOcrService> handlers;

	private IOcrService handler(final String algorithmType) {
		return handlers
				.entrySet()
				.stream()
				.filter(h -> h.getValue().handler().equals(algorithmType))
				.findFirst()
				.orElseThrow(() -> new SmartException(AlgorithmConstants.UNKNOWN_ALGORITHM_TYPE))
				.getValue();
	}

	public String ocr(String id, String algorithmType, String cardType, String imageBase64) {
		return handler(algorithmType).ocr(id, cardType, imageBase64);
	}
}
