package com.tce.smart.algorithm.service;

import com.tce.smart.algorithm.api.dto.req.CompareDTO;
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
public class CompareStrategyService {

	private Map<String, ICompareService> handlers;

	private ICompareService handler(final String algorithmType) {
		return handlers
				.entrySet()
				.stream()
				.filter(h -> h.getValue().handler().equals(algorithmType))
				.findFirst()
				.orElseThrow(() -> new SmartException(AlgorithmConstants.UNKNOWN_ALGORITHM_TYPE))
				.getValue();
	}

	public com.tce.smart.algorithm.api.dto.resp.CompareDTO compare(String id, String algorithmType, CompareDTO compareDTO) {
		return handler(algorithmType).compare(id, compareDTO);
	}
}
