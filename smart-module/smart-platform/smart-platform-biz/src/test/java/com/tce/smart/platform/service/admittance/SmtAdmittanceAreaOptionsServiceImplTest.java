package com.tce.smart.platform.service.admittance;

import com.tce.smart.platform.api.dto.resp.admittance.AdmittanceAreaOptionsRespDTO;
import com.tce.smart.platform.core.entity.SmtCommonConfig;
import com.tce.smart.platform.core.service.SmtCommonConfigService;
import com.tce.smart.platform.service.admittance.impl.SmtAdmittanceAreaOptionsServiceImpl;
import com.tce.smart.tool.enums.AdmittanceOaAreaEnum;
import com.tce.smart.tool.enums.ConfigBusinessEnum;
import com.tce.smart.tool.enums.ConfigBusinessTypeEnum;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.List;
import java.util.stream.Collectors;

public class SmtAdmittanceAreaOptionsServiceImplTest {

	@Test
	public void getAreaOptionsUsesParkScopedConfigForCommonAndSort() {
		SmtCommonConfigService configService = Mockito.mock(SmtCommonConfigService.class);
		SmtCommonConfig config = config("{\"inlineAreaLimit\":2,\"factories\":[{\"factoryType\":\"15\",\"factoryName\":\"新工厂\",\"areaFlag\":1,\"sort\":1,\"areas\":[{\"areaCode\":3,\"areaName\":\"外围门岗\",\"isCommon\":true,\"sort\":1},{\"areaCode\":0,\"areaName\":\"1F\",\"isCommon\":false,\"sort\":9}]}]}");
		Mockito.when(configService.getByType(ConfigBusinessEnum.ADMITTANCE.getCode(), ConfigBusinessTypeEnum.ADMITTANCE_AREA_DISPLAY.getCode(), 5000021)).thenReturn(config);

		SmtAdmittanceAreaOptionsServiceImpl service = new SmtAdmittanceAreaOptionsServiceImpl(configService);
		AdmittanceAreaOptionsRespDTO result = service.getAreaOptions(5000021);

		Assert.assertEquals(Integer.valueOf(5000021), result.getParkId());
		Assert.assertEquals(Integer.valueOf(2), result.getInlineAreaLimit());
		Assert.assertEquals("15", result.getFactories().get(0).getFactoryType());
		Assert.assertEquals(Integer.valueOf(3), result.getFactories().get(0).getAreas().get(0).getAreaCode());
		Assert.assertEquals("外围门岗", result.getFactories().get(0).getAreas().get(0).getAreaName());
		Assert.assertEquals(Boolean.TRUE, result.getFactories().get(0).getAreas().get(0).getIsCommon());
		Assert.assertEquals(Integer.valueOf(1), result.getFactories().get(0).getAreas().get(0).getSort());
	}

	@Test
	public void getAreaOptionsKeepsOnlyConfiguredAreasForConfiguredFactory() {
		SmtCommonConfigService configService = Mockito.mock(SmtCommonConfigService.class);
		SmtCommonConfig config = config("{\"factories\":[{\"factoryType\":\"15\",\"areas\":[{\"areaCode\":1,\"isCommon\":true,\"sort\":1}]}]}");
		Mockito.when(configService.getByType(ConfigBusinessEnum.ADMITTANCE.getCode(), ConfigBusinessTypeEnum.ADMITTANCE_AREA_DISPLAY.getCode(), 5000021)).thenReturn(config);

		SmtAdmittanceAreaOptionsServiceImpl service = new SmtAdmittanceAreaOptionsServiceImpl(configService);
		AdmittanceAreaOptionsRespDTO result = service.getAreaOptions(5000021);

		Assert.assertEquals(1, result.getFactories().size());
		Assert.assertEquals(1, result.getFactories().get(0).getAreas().size());
		Assert.assertTrue(areaCodes(result.getFactories().get(0)).contains(1));
		Assert.assertFalse(areaCodes(result.getFactories().get(0)).contains(0));
		Assert.assertFalse(areaCodes(result.getFactories().get(0)).contains(6));
	}

	@Test
	public void getAreaOptionsKeepsConfiguredFactoryTypesOutsideLegacyDefaults() {
		SmtCommonConfigService configService = Mockito.mock(SmtCommonConfigService.class);
		SmtCommonConfig config = config("{\"factories\":[{\"factoryType\":\"88\",\"factoryName\":\"技术园区\",\"sort\":3,\"areas\":[{\"areaCode\":0,\"areaName\":\"研发楼\",\"isCommon\":true,\"sort\":1}]}]}");
		Mockito.when(configService.getByType(ConfigBusinessEnum.ADMITTANCE.getCode(), ConfigBusinessTypeEnum.ADMITTANCE_AREA_DISPLAY.getCode(), 5000021)).thenReturn(config);

		SmtAdmittanceAreaOptionsServiceImpl service = new SmtAdmittanceAreaOptionsServiceImpl(configService);
		AdmittanceAreaOptionsRespDTO result = service.getAreaOptions(5000021);

		Assert.assertEquals(1, result.getFactories().size());
		AdmittanceAreaOptionsRespDTO.FactoryOption factory = result.getFactories().stream()
				.filter(item -> "88".equals(item.getFactoryType()))
				.findFirst()
				.orElse(null);
		Assert.assertNotNull(factory);
		Assert.assertEquals("技术园区", factory.getFactoryName());
		Assert.assertEquals(Integer.valueOf(0), factory.getAreas().get(0).getAreaCode());
		Assert.assertEquals("研发楼", factory.getAreas().get(0).getAreaName());
		Assert.assertEquals(Boolean.TRUE, factory.getAreas().get(0).getIsCommon());
	}

	@Test
	public void getAreaOptionsFiltersAreaCodesUnsupportedByOaSubmit() {
		SmtCommonConfigService configService = Mockito.mock(SmtCommonConfigService.class);
		SmtCommonConfig config = config("{\"factories\":[{\"factoryType\":\"88\",\"factoryName\":\"技术园区\",\"sort\":3,\"areas\":[{\"areaCode\":301,\"areaName\":\"研发楼\",\"isCommon\":true,\"sort\":1},{\"areaCode\":0,\"isCommon\":true,\"sort\":2}]}]}");
		Mockito.when(configService.getByType(ConfigBusinessEnum.ADMITTANCE.getCode(), ConfigBusinessTypeEnum.ADMITTANCE_AREA_DISPLAY.getCode(), 5000021)).thenReturn(config);

		SmtAdmittanceAreaOptionsServiceImpl service = new SmtAdmittanceAreaOptionsServiceImpl(configService);
		AdmittanceAreaOptionsRespDTO result = service.getAreaOptions(5000021);

		Assert.assertEquals(1, result.getFactories().size());
		Assert.assertFalse(areaCodes(result.getFactories().get(0)).contains(301));
		Assert.assertTrue(areaCodes(result.getFactories().get(0)).contains(0));
	}

	@Test
	public void getAreaOptionsUsesConfiguredAreaCodesAsAllowList() {
		SmtCommonConfigService configService = Mockito.mock(SmtCommonConfigService.class);
		SmtCommonConfig config = config("{\"factories\":[{\"factoryType\":\"15\",\"areas\":[{\"areaCode\":999,\"areaName\":\"过期区域\",\"isCommon\":true,\"sort\":1},{\"areaCode\":1,\"isCommon\":true,\"sort\":2}]}]}");
		Mockito.when(configService.getByType(ConfigBusinessEnum.ADMITTANCE.getCode(), ConfigBusinessTypeEnum.ADMITTANCE_AREA_DISPLAY.getCode(), 5000021)).thenReturn(config);

		SmtAdmittanceAreaOptionsServiceImpl service = new SmtAdmittanceAreaOptionsServiceImpl(configService);
		AdmittanceAreaOptionsRespDTO result = service.getAreaOptions(5000021);

		Assert.assertFalse(areaCodes(result.getFactories().get(0)).contains(999));
		Assert.assertEquals(1, result.getFactories().get(0).getAreas().size());
		Assert.assertTrue(areaCodes(result.getFactories().get(0)).contains(1));
	}

	@Test
	public void getAreaOptionsFallsBackToEnumNamesWhenConfigAreaNameIsBlank() {
		SmtCommonConfigService configService = Mockito.mock(SmtCommonConfigService.class);
		SmtCommonConfig config = config("{\"factories\":[{\"factoryType\":\"15\",\"factoryName\":\"配置里的新厂名称\",\"areas\":[{\"areaCode\":1,\"areaName\":\"\",\"isCommon\":true,\"sort\":1}]}]}");
		Mockito.when(configService.getByType(ConfigBusinessEnum.ADMITTANCE.getCode(), ConfigBusinessTypeEnum.ADMITTANCE_AREA_DISPLAY.getCode(), 5000021)).thenReturn(config);

		SmtAdmittanceAreaOptionsServiceImpl service = new SmtAdmittanceAreaOptionsServiceImpl(configService);
		AdmittanceAreaOptionsRespDTO result = service.getAreaOptions(5000021);

		Assert.assertEquals("新工厂", result.getFactories().get(0).getFactoryName());
		Assert.assertEquals(AdmittanceOaAreaEnum.getEnum(1).getDesc(), result.getFactories().get(0).getAreas().get(0).getAreaName());
		Assert.assertEquals(Boolean.TRUE, result.getFactories().get(0).getAreas().get(0).getIsCommon());
	}

	@Test
	public void getAreaOptionsFallsBackToEnumOnlyWhenConfigMissing() {
		SmtCommonConfigService configService = Mockito.mock(SmtCommonConfigService.class);
		Mockito.when(configService.getByType(ConfigBusinessEnum.ADMITTANCE.getCode(), ConfigBusinessTypeEnum.ADMITTANCE_AREA_DISPLAY.getCode(), 5000021)).thenReturn(null);

		SmtAdmittanceAreaOptionsServiceImpl service = new SmtAdmittanceAreaOptionsServiceImpl(configService);

		assertFallback(service.getAreaOptions(5000021), 5000021);
	}

	@Test
	public void getAreaOptionsFailsClosedWhenConfigCannotBeParsed() {
		SmtCommonConfigService configService = Mockito.mock(SmtCommonConfigService.class);
		Mockito.when(configService.getByType(ConfigBusinessEnum.ADMITTANCE.getCode(), ConfigBusinessTypeEnum.ADMITTANCE_AREA_DISPLAY.getCode(), 5000021)).thenReturn(config(null));
		Mockito.when(configService.getByType(ConfigBusinessEnum.ADMITTANCE.getCode(), ConfigBusinessTypeEnum.ADMITTANCE_AREA_DISPLAY.getCode(), 5000022)).thenReturn(config(""));
		Mockito.when(configService.getByType(ConfigBusinessEnum.ADMITTANCE.getCode(), ConfigBusinessTypeEnum.ADMITTANCE_AREA_DISPLAY.getCode(), 5000023)).thenReturn(config("{invalid"));

		SmtAdmittanceAreaOptionsServiceImpl service = new SmtAdmittanceAreaOptionsServiceImpl(configService);

		assertNoFactories(service.getAreaOptions(5000021), 5000021);
		assertNoFactories(service.getAreaOptions(5000022), 5000022);
		assertNoFactories(service.getAreaOptions(5000023), 5000023);
	}

	@Test
	public void getAreaOptionsFailsClosedWhenConfiguredFactoriesHaveNoValidAreas() {
		SmtCommonConfigService configService = Mockito.mock(SmtCommonConfigService.class);
		Mockito.when(configService.getByType(ConfigBusinessEnum.ADMITTANCE.getCode(), ConfigBusinessTypeEnum.ADMITTANCE_AREA_DISPLAY.getCode(), 5000021)).thenReturn(config("{\"factories\":[]}"));
		Mockito.when(configService.getByType(ConfigBusinessEnum.ADMITTANCE.getCode(), ConfigBusinessTypeEnum.ADMITTANCE_AREA_DISPLAY.getCode(), 5000022)).thenReturn(config("{\"factories\":[{\"factoryType\":\"88\",\"areas\":[{\"areaCode\":301,\"isCommon\":true}]}]}"));

		SmtAdmittanceAreaOptionsServiceImpl service = new SmtAdmittanceAreaOptionsServiceImpl(configService);

		assertNoFactories(service.getAreaOptions(5000021), 5000021);
		assertNoFactories(service.getAreaOptions(5000022), 5000022);
	}

	private SmtCommonConfig config(String value) {
		SmtCommonConfig config = new SmtCommonConfig();
		config.setBusinessType(ConfigBusinessEnum.ADMITTANCE.getCode());
		config.setConfigType(ConfigBusinessTypeEnum.ADMITTANCE_AREA_DISPLAY.getCode());
		config.setParkId(5000021);
		config.setValue(value);
		return config;
	}

	private void assertFallback(AdmittanceAreaOptionsRespDTO result, Integer parkId) {
		Assert.assertEquals(parkId, result.getParkId());
		Assert.assertEquals(Integer.valueOf(4), result.getInlineAreaLimit());
		Assert.assertEquals(2, result.getFactories().size());
		Assert.assertEquals("15", result.getFactories().get(0).getFactoryType());
		Assert.assertEquals(AdmittanceOaAreaEnum.getType(1).size(), result.getFactories().get(0).getAreas().size());
		Assert.assertEquals(Boolean.TRUE, result.getFactories().get(0).getAreas().get(0).getIsCommon());
	}

	private void assertNoFactories(AdmittanceAreaOptionsRespDTO result, Integer parkId) {
		Assert.assertEquals(parkId, result.getParkId());
		Assert.assertEquals(Integer.valueOf(4), result.getInlineAreaLimit());
		Assert.assertTrue(result.getFactories().isEmpty());
	}

	private List<Integer> areaCodes(AdmittanceAreaOptionsRespDTO.FactoryOption factory) {
		return factory.getAreas().stream()
				.map(AdmittanceAreaOptionsRespDTO.AreaOption::getAreaCode)
				.collect(Collectors.toList());
	}
}
