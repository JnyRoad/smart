package com.tce.smart.platform.service.admittance.impl;

import cn.hutool.json.JSONUtil;
import com.tce.smart.platform.api.dto.resp.admittance.AdmittanceAreaOptionsRespDTO;
import com.tce.smart.platform.core.entity.SmtCommonConfig;
import com.tce.smart.platform.core.service.SmtCommonConfigService;
import com.tce.smart.platform.service.admittance.SmtAdmittanceAreaOptionsService;
import com.tce.smart.tool.enums.AdmittanceOaAreaEnum;
import com.tce.smart.tool.enums.ConfigBusinessEnum;
import com.tce.smart.tool.enums.ConfigBusinessTypeEnum;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
@AllArgsConstructor
@Slf4j
public class SmtAdmittanceAreaOptionsServiceImpl implements SmtAdmittanceAreaOptionsService {
	private static final int DEFAULT_INLINE_AREA_LIMIT = 4;
	private static final String NEW_FACTORY_TYPE = "15";
	private static final String OLD_FACTORY_TYPE = "16";
	private static final int NEW_FACTORY_FLAG = 1;
	private static final int OLD_FACTORY_FLAG = 0;

	private final SmtCommonConfigService commonConfigService;

	@Override
	public AdmittanceAreaOptionsRespDTO getAreaOptions(Integer parkId) {
		SmtCommonConfig config = commonConfigService.getByType(
				ConfigBusinessEnum.ADMITTANCE.getCode(),
				ConfigBusinessTypeEnum.ADMITTANCE_AREA_DISPLAY.getCode(),
				parkId
		);
		if (config == null) {
			return fallbackOptions(parkId);
		}
		AdmittanceAreaOptionsRespDTO configured = parseConfig(config);
		if (!hasConfiguredFactories(configured)) {
			return emptyOptions(parkId);
		}
		AdmittanceAreaOptionsRespDTO options = configuredOptions(parkId, configured);
		if (options.getFactories().isEmpty()) {
			return emptyOptions(parkId);
		}
		return options;
	}

	private AdmittanceAreaOptionsRespDTO parseConfig(SmtCommonConfig config) {
		if (config.getValue() == null || config.getValue().trim().isEmpty()) {
			log.warn("Admittance area display config is empty, parkId={}", config.getParkId());
			return null;
		}
		if (!JSONUtil.isJson(config.getValue())) {
			log.warn("Admittance area display config is not json, parkId={}", config.getParkId());
			return null;
		}
		try {
			return JSONUtil.toBean(config.getValue(), AdmittanceAreaOptionsRespDTO.class);
		} catch (RuntimeException ex) {
			log.warn("Admittance area display config parse failed, parkId={}", config.getParkId(), ex);
			return null;
		}
	}

	private boolean hasConfiguredFactories(AdmittanceAreaOptionsRespDTO configured) {
		return configured != null && configured.getFactories() != null && !configured.getFactories().isEmpty();
	}

	private AdmittanceAreaOptionsRespDTO configuredOptions(Integer parkId, AdmittanceAreaOptionsRespDTO configured) {
		AdmittanceAreaOptionsRespDTO options = new AdmittanceAreaOptionsRespDTO();
		options.setParkId(parkId);
		options.setInlineAreaLimit(DEFAULT_INLINE_AREA_LIMIT);
		if (configured.getInlineAreaLimit() != null && configured.getInlineAreaLimit() > 0) {
			options.setInlineAreaLimit(configured.getInlineAreaLimit());
		}
		configured.getFactories().forEach(configuredFactory -> {
			AdmittanceAreaOptionsRespDTO.FactoryOption factory = configuredFactoryOption(configuredFactory);
			if (factory != null && !factory.getAreas().isEmpty()) {
				options.getFactories().add(factory);
			}
		});
		options.getFactories().sort(Comparator.comparing(AdmittanceAreaOptionsRespDTO.FactoryOption::getSort));
		return options;
	}

	private AdmittanceAreaOptionsRespDTO.FactoryOption configuredFactoryOption(AdmittanceAreaOptionsRespDTO.FactoryOption configuredFactory) {
		if (configuredFactory == null) {
			return null;
		}
		String factoryType = normalizeFactoryType(configuredFactory.getFactoryType());
		if (factoryType == null) {
			return null;
		}
		AdmittanceAreaOptionsRespDTO.FactoryOption factory = new AdmittanceAreaOptionsRespDTO.FactoryOption();
		Integer areaFlag = supportedAreaFlag(configuredFactory.getAreaFlag());
		if (areaFlag == null) {
			areaFlag = defaultAreaFlag(factoryType);
		}
		factory.setFactoryType(factoryType);
		factory.setFactoryName(defaultFactoryName(factoryType, configuredFactory.getFactoryName()));
		factory.setAreaFlag(areaFlag);
		factory.setSort(configuredFactory.getSort() == null ? defaultFactorySort(factoryType) : configuredFactory.getSort());
		factory.getAreas().addAll(configuredExplicitAreaOptions(configuredFactory));
		factory.getAreas().sort(Comparator.comparing(AdmittanceAreaOptionsRespDTO.AreaOption::getSort));
		return factory;
	}

	private List<AdmittanceAreaOptionsRespDTO.AreaOption> configuredExplicitAreaOptions(AdmittanceAreaOptionsRespDTO.FactoryOption configuredFactory) {
		if (configuredFactory.getAreas() == null || configuredFactory.getAreas().isEmpty()) {
			return Collections.emptyList();
		}
		List<AdmittanceAreaOptionsRespDTO.AreaOption> areaOptions = new ArrayList<>();
		for (int i = 0; i < configuredFactory.getAreas().size(); i++) {
			AdmittanceAreaOptionsRespDTO.AreaOption configuredArea = configuredFactory.getAreas().get(i);
			AdmittanceOaAreaEnum areaEnum = AdmittanceOaAreaEnum.getEnum(configuredArea.getAreaCode());
			if (areaEnum == null) {
				continue;
			}
			AdmittanceAreaOptionsRespDTO.AreaOption area = new AdmittanceAreaOptionsRespDTO.AreaOption();
			area.setAreaCode(configuredArea.getAreaCode());
			area.setAreaName(configuredAreaName(configuredArea, areaEnum));
			area.setIsCommon(Boolean.TRUE.equals(configuredArea.getIsCommon()));
			area.setSort(configuredArea.getSort() == null ? i + 1 : configuredArea.getSort());
			areaOptions.add(area);
		}
		return areaOptions;
	}

	private String configuredAreaName(AdmittanceAreaOptionsRespDTO.AreaOption configuredArea, AdmittanceOaAreaEnum areaEnum) {
		if (configuredArea.getAreaName() == null || configuredArea.getAreaName().trim().isEmpty()) {
			return areaEnum.getDesc();
		}
		return configuredArea.getAreaName();
	}

	private String normalizeFactoryType(String factoryType) {
		if (factoryType == null || factoryType.trim().isEmpty()) {
			return null;
		}
		return factoryType.trim();
	}

	private Integer supportedAreaFlag(Integer areaFlag) {
		if (Objects.equals(NEW_FACTORY_FLAG, areaFlag) || Objects.equals(OLD_FACTORY_FLAG, areaFlag)) {
			return areaFlag;
		}
		return null;
	}

	private Integer defaultAreaFlag(String factoryType) {
		if (NEW_FACTORY_TYPE.equals(factoryType)) {
			return NEW_FACTORY_FLAG;
		}
		if (OLD_FACTORY_TYPE.equals(factoryType)) {
			return OLD_FACTORY_FLAG;
		}
		return null;
	}

	private String defaultFactoryName(String factoryType, String configuredFactoryName) {
		if (NEW_FACTORY_TYPE.equals(factoryType)) {
			return "新工厂";
		}
		if (OLD_FACTORY_TYPE.equals(factoryType)) {
			return "老工厂";
		}
		return configuredFactoryName;
	}

	private Integer defaultFactorySort(String factoryType) {
		if (NEW_FACTORY_TYPE.equals(factoryType)) {
			return 1;
		}
		if (OLD_FACTORY_TYPE.equals(factoryType)) {
			return 2;
		}
		return 0;
	}

	private AdmittanceAreaOptionsRespDTO fallbackOptions(Integer parkId) {
		AdmittanceAreaOptionsRespDTO options = new AdmittanceAreaOptionsRespDTO();
		options.setParkId(parkId);
		options.setInlineAreaLimit(DEFAULT_INLINE_AREA_LIMIT);
		options.getFactories().add(factoryOption(NEW_FACTORY_TYPE, "新工厂", NEW_FACTORY_FLAG, 1));
		options.getFactories().add(factoryOption(OLD_FACTORY_TYPE, "老工厂", OLD_FACTORY_FLAG, 2));
		return options;
	}

	private AdmittanceAreaOptionsRespDTO emptyOptions(Integer parkId) {
		AdmittanceAreaOptionsRespDTO options = new AdmittanceAreaOptionsRespDTO();
		options.setParkId(parkId);
		options.setInlineAreaLimit(DEFAULT_INLINE_AREA_LIMIT);
		return options;
	}

	private AdmittanceAreaOptionsRespDTO.FactoryOption factoryOption(String factoryType, String factoryName, Integer areaFlag, Integer sort) {
		AdmittanceAreaOptionsRespDTO.FactoryOption factory = new AdmittanceAreaOptionsRespDTO.FactoryOption();
		factory.setFactoryType(factoryType);
		factory.setFactoryName(factoryName);
		factory.setAreaFlag(areaFlag);
		factory.setSort(sort);
		factory.getAreas().addAll(areaOptionsByFlag(areaFlag));
		return factory;
	}

	private List<AdmittanceAreaOptionsRespDTO.AreaOption> areaOptionsByFlag(Integer areaFlag) {
		if (supportedAreaFlag(areaFlag) == null) {
			return Collections.emptyList();
		}
		List<Map<String, Object>> areas = AdmittanceOaAreaEnum.getType(areaFlag);
		List<AdmittanceAreaOptionsRespDTO.AreaOption> areaOptions = new ArrayList<>();
		for (int i = 0; i < areas.size(); i++) {
			Map<String, Object> map = areas.get(i);
			AdmittanceAreaOptionsRespDTO.AreaOption area = new AdmittanceAreaOptionsRespDTO.AreaOption();
			area.setAreaCode(Integer.parseInt(Objects.toString(map.get("code"))));
			area.setAreaName(Objects.toString(map.get("desc")));
			area.setIsCommon(i < DEFAULT_INLINE_AREA_LIMIT);
			area.setSort(i + 1);
			areaOptions.add(area);
		}
		return areaOptions;
	}
}
