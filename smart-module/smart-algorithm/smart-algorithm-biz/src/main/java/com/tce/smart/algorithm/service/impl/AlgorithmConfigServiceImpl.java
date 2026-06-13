package com.tce.smart.algorithm.service.impl;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tce.smart.algorithm.api.dto.req.UpdateAlgorithmConfigDTO;
import com.tce.smart.algorithm.constant.AlgorithmConstants;
import com.tce.smart.algorithm.entity.AlgorithmConfig;
import com.tce.smart.algorithm.mapper.AlgorithmConfigMapper;
import com.tce.smart.algorithm.service.AlgorithmConfigService;
import com.tce.smart.common.core.exception.SmartException;
import com.tce.smart.common.core.util.StringUtils;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

/**
 * @ClassName ConfigServiceImpl
 * @Description TODD
 * @Author bingeox
 * @Date 2019\8\1 0001 14:30
 * Version 1.0
 **/
@Slf4j
@Service
@AllArgsConstructor
public class AlgorithmConfigServiceImpl extends ServiceImpl<AlgorithmConfigMapper, AlgorithmConfig> implements AlgorithmConfigService {

	private final RedisTemplate<String, Object> redisTemplate;

    @Override
	@Cacheable(value="algorithm_configs")
    public AlgorithmConfig getByAlgorithmType(String algorithmType) {
        AlgorithmConfig algorithmConfig = this.getOne(Wrappers.<AlgorithmConfig>lambdaQuery().eq(AlgorithmConfig::getAlgorithmType, algorithmType));
        if (Objects.isNull(algorithmConfig)) {
            throw new SmartException("该算法类型配置不存在");
        }
        return algorithmConfig;
    }

    @Override
    public <T> T getAlgorithmProperties(String algorithmType, Class<T> tClass){
		String propertiesKey = AlgorithmConstants.ALGORITHM_CONFIG_PREFIX + algorithmType;
		try {
			Boolean hasProperties = redisTemplate.hasKey(propertiesKey);
			if (Objects.nonNull(hasProperties) && hasProperties) {
				AlgorithmConfig algorithmConfig = (AlgorithmConfig) redisTemplate.opsForValue().get(propertiesKey);
				return JSONUtil.toBean(Objects.requireNonNull(algorithmConfig).getContent(), tClass);
			}
		} catch (Exception e) {
			log.error("Redis连接异常:{}", e.getMessage(), e);
		}
		AlgorithmConfig algorithmConfig = this.getOne(Wrappers.<AlgorithmConfig>lambdaQuery().eq(AlgorithmConfig::getAlgorithmType, algorithmType));
		if (Objects.isNull(algorithmConfig)) {
			throw new SmartException("该算法类型配置不存在");
		}
		try {
			redisTemplate.opsForValue().set(propertiesKey, algorithmConfig);
		} catch (Exception e) {
			log.error("Redis缓存算法配置异常:{}", e.getMessage(), e);
		}
		return JSONUtil.toBean(algorithmConfig.getContent(), tClass);
	}

	@Override
	public IPage<AlgorithmConfig> getPageList(Page<AlgorithmConfig> page, String algorithmType) {
		return this.page(page, Wrappers.<AlgorithmConfig>lambdaQuery()
				.like(StringUtils.isNotBlank(algorithmType), AlgorithmConfig::getAlgorithmType, algorithmType));
	}

	@Override
	@CachePut(value = "algorithm_configs", key = "#updateAlgorithmConfigDTO.algorithmType")
	public AlgorithmConfig updateConfig(UpdateAlgorithmConfigDTO updateAlgorithmConfigDTO) {
		AlgorithmConfig algorithmConfig = this.getByAlgorithmType(updateAlgorithmConfigDTO.getAlgorithmType());
		final JSONObject contentObj = new JSONObject();
		updateAlgorithmConfigDTO.getConfigList().forEach(config -> contentObj.put(config.getKey(), config.getValue()));
		algorithmConfig.setContent(contentObj.toString());
		this.updateById(algorithmConfig);
		return algorithmConfig;
	}

	@Override
	public List<AlgorithmConfig> getList(String type) {
		return this.list(Wrappers.<AlgorithmConfig>lambdaQuery()
				.likeRight(StringUtils.isNotBlank(type), AlgorithmConfig::getAlgorithmType, type));
	}
}
