package com.tce.smart.platform.service.admittance;

import com.tce.smart.platform.api.dto.resp.admittance.AdmittanceAreaOptionsRespDTO;
import com.tce.smart.platform.service.admittance.impl.SmtAdmittanceApplyServiceImpl;
import com.tce.smart.tool.enums.ExceptionTypeEnum;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class SmtAdmittanceApplyServiceImplTest {

	@Test
	public void validateApplyAreaTypeRejectsEmptyAreaType() throws Exception {
		Assert.assertEquals(ExceptionTypeEnum.valueOf("VISITOR_AREA_TYPE_EMPTY"), validateApplyAreaType(5000021, "15", null));
		Assert.assertEquals(ExceptionTypeEnum.valueOf("VISITOR_AREA_TYPE_EMPTY"), validateApplyAreaType(5000021, "15", Collections.emptyList()));
	}

	@Test
	public void validateApplyAreaTypeRejectsInvalidAreaCode() throws Exception {
		Assert.assertEquals(ExceptionTypeEnum.valueOf("VISITOR_AREA_TYPE_ERROR"), validateApplyAreaType(5000021, "15", Arrays.asList(1, 999)));
		Assert.assertEquals(ExceptionTypeEnum.valueOf("VISITOR_AREA_TYPE_ERROR"), validateApplyAreaType(5000021, "15", Arrays.asList(1, null)));
	}

	@Test
	public void validateApplyAreaTypeAcceptsEnumAreaCodes() throws Exception {
		Assert.assertEquals(ExceptionTypeEnum.CHECK_SUCCESS, validateApplyAreaType(5000021, "15", Arrays.asList(0, 1, 6)));
		Assert.assertEquals(ExceptionTypeEnum.CHECK_SUCCESS, validateApplyAreaType(5000021, "16", Arrays.asList(7, 8, 14)));
	}

	@Test
	public void validateApplyAreaTypeKeepsLegacyHefeiPayloadWithoutAreaFields() throws Exception {
		Assert.assertEquals(ExceptionTypeEnum.CHECK_SUCCESS, validateApplyAreaType(20381, null, null));
	}

	@Test
	public void validateApplyAreaTypeRejectsNonHefeiPayloadWithoutAreaFields() throws Exception {
		Assert.assertEquals(ExceptionTypeEnum.valueOf("VISITOR_AREA_TYPE_EMPTY"), validateApplyAreaType(5000021, null, null));
		Assert.assertEquals(ExceptionTypeEnum.valueOf("VISITOR_AREA_TYPE_EMPTY"), validateApplyAreaType(null, null, null));
	}

	@Test
	public void validateApplyAreaTypeRejectsAreaCodesWithoutFactoryType() throws Exception {
		Assert.assertEquals(ExceptionTypeEnum.valueOf("VISITOR_AREA_TYPE_ERROR"), validateApplyAreaType(5000021, null, Collections.singletonList(0)));
	}

	@Test
	public void validateApplyAreaTypeRejectsAreaCodesFromAnotherFactory() throws Exception {
		Assert.assertEquals(ExceptionTypeEnum.valueOf("VISITOR_AREA_TYPE_ERROR"), validateApplyAreaType(5000021, "15", Collections.singletonList(7)));
		Assert.assertEquals(ExceptionTypeEnum.valueOf("VISITOR_AREA_TYPE_ERROR"), validateApplyAreaType(5000021, "16", Collections.singletonList(0)));
	}

	@Test
	public void validateApplyAreaTypeUsesAreaOptionsFactoryTypesInsteadOfLegacyConstants() throws Exception {
		Assert.assertEquals(ExceptionTypeEnum.CHECK_SUCCESS, validateApplyAreaType(5000021, "88", Collections.singletonList(0), areaOptions(factory("88", 0))));
		Assert.assertEquals(ExceptionTypeEnum.valueOf("VISITOR_AREA_TYPE_ERROR"), validateApplyAreaType(5000021, "15", Collections.singletonList(0), areaOptions(factory("88", 0))));
	}

	@Test
	public void validateApplyAreaTypeRejectsAreaCodesUnsupportedByOaSubmit() throws Exception {
		Assert.assertEquals(ExceptionTypeEnum.valueOf("VISITOR_AREA_TYPE_ERROR"), validateApplyAreaType(5000021, "88", Collections.singletonList(301), areaOptions(factory("88", 301))));
	}

	private ExceptionTypeEnum validateApplyAreaType(Integer parkId, String permitFactoryType, List<Integer> areaType) throws Exception {
		return validateApplyAreaType(parkId, permitFactoryType, areaType, legacyAreaOptions());
	}

	private ExceptionTypeEnum validateApplyAreaType(Integer parkId, String permitFactoryType, List<Integer> areaType, AdmittanceAreaOptionsRespDTO areaOptions) throws Exception {
		SmtAdmittanceApplyServiceImpl service = new SmtAdmittanceApplyServiceImpl();
		SmtAdmittanceAreaOptionsService areaOptionsService = Mockito.mock(SmtAdmittanceAreaOptionsService.class);
		Mockito.when(areaOptionsService.getAreaOptions(parkId)).thenReturn(areaOptions);
		Field field = SmtAdmittanceApplyServiceImpl.class.getDeclaredField("smtAdmittanceAreaOptionsService");
		field.setAccessible(true);
		field.set(service, areaOptionsService);
		Method method = SmtAdmittanceApplyServiceImpl.class.getDeclaredMethod("validateApplyAreaType", Integer.class, String.class, List.class);
		method.setAccessible(true);
		return (ExceptionTypeEnum) method.invoke(service, parkId, permitFactoryType, areaType);
	}

	private AdmittanceAreaOptionsRespDTO legacyAreaOptions() {
		return areaOptions(factory("15", 0, 1, 6), factory("16", 7, 8, 14));
	}

	private AdmittanceAreaOptionsRespDTO areaOptions(AdmittanceAreaOptionsRespDTO.FactoryOption... factories) {
		AdmittanceAreaOptionsRespDTO options = new AdmittanceAreaOptionsRespDTO();
		options.setParkId(5000021);
		options.setFactories(Arrays.asList(factories));
		return options;
	}

	private AdmittanceAreaOptionsRespDTO.FactoryOption factory(String factoryType, Integer... areaCodes) {
		AdmittanceAreaOptionsRespDTO.FactoryOption factory = new AdmittanceAreaOptionsRespDTO.FactoryOption();
		factory.setFactoryType(factoryType);
		for (Integer areaCode : areaCodes) {
			AdmittanceAreaOptionsRespDTO.AreaOption area = new AdmittanceAreaOptionsRespDTO.AreaOption();
			area.setAreaCode(areaCode);
			factory.getAreas().add(area);
		}
		return factory;
	}
}
