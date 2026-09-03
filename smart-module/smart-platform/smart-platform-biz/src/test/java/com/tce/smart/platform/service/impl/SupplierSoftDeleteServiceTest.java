package com.tce.smart.platform.service.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.tce.smart.platform.core.dto.SmtVisitorSupplierFindDTO;
import com.tce.smart.platform.core.entity.SmtSecurityAreaSupplier;
import com.tce.smart.platform.core.entity.SmtSupplierPerson;
import com.tce.smart.platform.core.mapper.SmtSecurityAreaSupplierMapper;
import com.tce.smart.platform.core.mapper.SmtSupplierPersonMapper;
import com.tce.smart.platform.core.service.SmtImageService;
import com.tce.smart.platform.service.ImageService;
import com.tce.smart.platform.service.SmtParkService;
import com.tce.smart.platform.service.SmtSupplierPersonService;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * 保密供应商软删除服务边界测试。
 */
public class SupplierSoftDeleteServiceTest {

	/**
	 * 新增人员时忽略请求体伪造的删除标识，并固定写入有效状态。
	 */
	@Test
	public void saveSupplierPersonForcesActiveDeleteFlag() {
		SmtSupplierPersonMapper personMapper = Mockito.mock(SmtSupplierPersonMapper.class);
		SmtSecurityAreaSupplierMapper supplierMapper = Mockito.mock(SmtSecurityAreaSupplierMapper.class);
		TestableSupplierPersonService service = new TestableSupplierPersonService(personMapper, supplierMapper);
		SmtSecurityAreaSupplier supplier = new SmtSecurityAreaSupplier();
		supplier.setId(8L);
		Mockito.when(supplierMapper.selectActiveSupplierForUpdate(8L)).thenReturn(supplier);
		Mockito.when(personMapper.selectCount(Mockito.any(Wrapper.class))).thenReturn(0);
		Mockito.when(personMapper.insert(Mockito.any(SmtSupplierPerson.class))).thenReturn(1);

		SmtSupplierPerson person = new SmtSupplierPerson();
		person.setSupplierId(8L);
		person.setIdCard("440100200001010011");
		person.setDelFlag(1);

		assertTrue(service.saveSupplierPerson(person));
		ArgumentCaptor<SmtSupplierPerson> personCaptor = ArgumentCaptor.forClass(SmtSupplierPerson.class);
		Mockito.verify(personMapper).insert(personCaptor.capture());
		assertEquals(Integer.valueOf(0), personCaptor.getValue().getDelFlag());
	}

	/**
	 * 访客校验必须使用已关联有效供应商的单条读取。
	 */
	@Test
	public void visitorCheckUsesSingleActiveSupplierQuery() {
		SmtSupplierPersonMapper personMapper = Mockito.mock(SmtSupplierPersonMapper.class);
		SmtSecurityAreaSupplierMapper supplierMapper = Mockito.mock(SmtSecurityAreaSupplierMapper.class);
		TestableSupplierPersonService service = new TestableSupplierPersonService(personMapper, supplierMapper);
		Mockito.when(personMapper.existsActiveSupplierPerson(8L, "440100200001010011")).thenReturn(0);
		SmtVisitorSupplierFindDTO request = new SmtVisitorSupplierFindDTO();
		request.setSupplierId("8");
		request.setIdCard("440100200001010011");

		assertFalse(service.getVisitorSupplier(request));
		Mockito.verify(personMapper, Mockito.never()).selectOne(Mockito.any(Wrapper.class));
		Mockito.verify(personMapper).existsActiveSupplierPerson(8L, "440100200001010011");
	}

	/**
	 * 订单端人员列表必须调用关联有效供应商的单条读取。
	 */
	@Test
	public void orderPersonListUsesActiveSupplierPersonQuery() {
		SmtSecurityAreaSupplierMapper supplierMapper = Mockito.mock(SmtSecurityAreaSupplierMapper.class);
		SmtSupplierPersonService personService = Mockito.mock(SmtSupplierPersonService.class);
		TestableSecurityAreaSupplierService service = new TestableSecurityAreaSupplierService(
				supplierMapper, personService, Mockito.mock(SmtImageService.class),
				Mockito.mock(SmtParkService.class), Mockito.mock(ImageService.class));
		Mockito.when(personService.getActiveSupplierPersonList(8L)).thenReturn(java.util.Collections.emptyList());

		assertTrue(service.getSecurityAreaSupplierPersonList(8L).isEmpty());
		Mockito.verify(personService).getActiveSupplierPersonList(8L);
	}

	/**
	 * 为单元测试注入 MyBatis-Plus 基础 Mapper。
	 */
	private static final class TestableSupplierPersonService extends SmtSupplierPersonServiceImpl {

		private TestableSupplierPersonService(SmtSupplierPersonMapper personMapper,
				SmtSecurityAreaSupplierMapper supplierMapper) {
			super(personMapper, supplierMapper);
			this.baseMapper = personMapper;
		}
	}

	/**
	 * 为单元测试注入供应商服务的基础 Mapper。
	 */
	private static final class TestableSecurityAreaSupplierService extends SmtSecurityAreaSupplierServiceImpl {

		private TestableSecurityAreaSupplierService(SmtSecurityAreaSupplierMapper supplierMapper,
				SmtSupplierPersonService personService, SmtImageService imageService,
				SmtParkService parkService, ImageService localImageService) {
			super(supplierMapper, personService, imageService, parkService, localImageService);
			this.baseMapper = supplierMapper;
		}
	}
}
