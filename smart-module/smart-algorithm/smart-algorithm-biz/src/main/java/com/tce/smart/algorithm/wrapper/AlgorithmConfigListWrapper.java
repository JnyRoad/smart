package com.tce.smart.algorithm.wrapper;

import com.tce.smart.algorithm.api.enums.AlgorithmTypeEnum;
import com.tce.smart.algorithm.api.dto.resp.AlgorithmConfigListDTO;
import com.tce.smart.algorithm.entity.AlgorithmConfig;
import com.tce.smart.common.core.util.BeanUtils;
import com.tce.smart.common.core.wrapper.BaseWrapper;
import org.springframework.stereotype.Component;

/**
 * @author wxjason
 */
@Component
public class AlgorithmConfigListWrapper extends BaseWrapper<AlgorithmConfig, AlgorithmConfigListDTO> {

	@Override
	protected AlgorithmConfigListDTO warp(AlgorithmConfig model) {
		AlgorithmConfigListDTO dto = BeanUtils.transform(AlgorithmConfigListDTO.class, model);
		dto.setAlgorithmName(AlgorithmTypeEnum.name(model.getAlgorithmType()));
		return dto;
	}
}
