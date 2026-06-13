package com.tce.smart.algorithm.wrapper;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.tce.smart.algorithm.api.enums.AlgorithmTypeEnum;
import com.tce.smart.algorithm.api.dto.resp.AlgorithmConfigDetailDTO;
import com.tce.smart.algorithm.api.dto.resp.ConfigDetailDTO;
import com.tce.smart.algorithm.entity.AlgorithmConfig;
import com.tce.smart.common.core.util.BeanUtils;
import com.tce.smart.common.core.wrapper.BaseWrapper;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author wxjason
 */
@Component
public class AlgorithmConfigDetailWrapper extends BaseWrapper<AlgorithmConfig, AlgorithmConfigDetailDTO> {

	@Override
	protected AlgorithmConfigDetailDTO warp(AlgorithmConfig model) {
		AlgorithmConfigDetailDTO dto = BeanUtils.transform(AlgorithmConfigDetailDTO.class, model);
		dto.setAlgorithmName(AlgorithmTypeEnum.name(model.getAlgorithmType()));
		JSONObject content = JSONUtil.parseObj(model.getContent());
		List<ConfigDetailDTO> configList = content.entrySet().stream().map(m -> ConfigDetailDTO.builder().key(m.getKey()).value((String) m.getValue()).build()).collect(Collectors.toList());
		dto.setConfigList(configList);
		return dto;
	}
}
