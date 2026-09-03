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
import com.tce.smart.tool.exception.TCEException;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

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
	 * 已删除或不存在的供应商不能再次执行删除，也不应查询人员。
	 */
	@Test
	public void deleteInactiveSupplierReturnsFalseWithoutReadingPeople() {
		SmtSecurityAreaSupplierMapper supplierMapper = Mockito.mock(SmtSecurityAreaSupplierMapper.class);
		SmtSupplierPersonService personService = Mockito.mock(SmtSupplierPersonService.class);
		TestableSecurityAreaSupplierService service = newSupplierService(supplierMapper, personService);
		Mockito.when(supplierMapper.selectActiveSupplierForUpdate(8L)).thenReturn(null);

		assertFalse(service.delSecurityAreaSupplier(8L));
		Mockito.verify(supplierMapper).selectActiveSupplierForUpdate(8L);
		Mockito.verify(personService, Mockito.never()).list(Mockito.any(Wrapper.class));
		Mockito.verify(supplierMapper, Mockito.never()).deleteById(Mockito.any());
	}

	/**
	 * 供应商仍有关联有效人员时，单条删除必须拒绝，不能写入逻辑删除标识。
	 */
	@Test
	public void deleteSupplierWithActivePeopleIsRejected() {
		SmtSecurityAreaSupplierMapper supplierMapper = Mockito.mock(SmtSecurityAreaSupplierMapper.class);
		SmtSupplierPersonService personService = Mockito.mock(SmtSupplierPersonService.class);
		TestableSecurityAreaSupplierService service = newSupplierService(supplierMapper, personService);
		Mockito.when(supplierMapper.selectActiveSupplierForUpdate(8L)).thenReturn(activeSupplier(8L));
		Mockito.when(personService.list(Mockito.any(Wrapper.class))).thenReturn(Collections.singletonList(new SmtSupplierPerson()));

		try {
			service.delSecurityAreaSupplier(8L);
			fail("有关联人员时必须拒绝删除供应商");
		} catch (TCEException expected) {
			assertTrue(expected.getMessage().contains("已关联授权人员"));
		}
		Mockito.verify(supplierMapper, Mockito.never()).deleteById(Mockito.any());
	}

	/**
	 * 无关联人员的有效供应商应通过 MyBatis-Plus 逻辑删除入口删除。
	 */
	@Test
	public void deleteActiveSupplierWithoutPeopleUsesLogicalDeleteMapper() {
		SmtSecurityAreaSupplierMapper supplierMapper = Mockito.mock(SmtSecurityAreaSupplierMapper.class);
		SmtSupplierPersonService personService = Mockito.mock(SmtSupplierPersonService.class);
		TestableSecurityAreaSupplierService service = newSupplierService(supplierMapper, personService);
		Mockito.when(supplierMapper.selectActiveSupplierForUpdate(8L)).thenReturn(activeSupplier(8L));
		Mockito.when(personService.list(Mockito.any(Wrapper.class))).thenReturn(Collections.emptyList());
		Mockito.when(supplierMapper.deleteById(8L)).thenReturn(1);

		assertTrue(service.delSecurityAreaSupplier(8L));
		Mockito.verify(supplierMapper).deleteById(8L);
	}

	/**
	 * 批量删除保留原语义：失效供应商和仍有关联人员的供应商均跳过，其余供应商删除。
	 */
	@Test
	public void batchDeleteSkipsInactiveAndSupplierWithPeople() {
		SmtSecurityAreaSupplierMapper supplierMapper = Mockito.mock(SmtSecurityAreaSupplierMapper.class);
		SmtSupplierPersonService personService = Mockito.mock(SmtSupplierPersonService.class);
		TestableSecurityAreaSupplierService service = newSupplierService(supplierMapper, personService);
		Mockito.when(supplierMapper.selectActiveSupplierForUpdate(8L)).thenReturn(null);
		Mockito.when(supplierMapper.selectActiveSupplierForUpdate(9L)).thenReturn(activeSupplier(9L));
		Mockito.when(supplierMapper.selectActiveSupplierForUpdate(10L)).thenReturn(activeSupplier(10L));
		Mockito.when(personService.list(Mockito.any(Wrapper.class))).thenReturn(Collections.emptyList(),
				Collections.singletonList(new SmtSupplierPerson()));
		Mockito.when(supplierMapper.deleteById(9L)).thenReturn(1);

		assertTrue(service.delBatchSupplier(Arrays.asList(8L, 9L, 10L)));
		Mockito.verify(supplierMapper).deleteById(9L);
		Mockito.verify(supplierMapper, Mockito.never()).deleteById(8L);
		Mockito.verify(supplierMapper, Mockito.never()).deleteById(10L);
	}

	/**
	 * 构造处于有效状态的供应商记录，供删除分支测试复用。
	 */
	private SmtSecurityAreaSupplier activeSupplier(Long id) {
		SmtSecurityAreaSupplier supplier = new SmtSecurityAreaSupplier();
		supplier.setId(id);
		return supplier;
	}

	/**
	 * 构造注入必要依赖的供应商服务测试实例。
	 */
	private TestableSecurityAreaSupplierService newSupplierService(SmtSecurityAreaSupplierMapper supplierMapper,
			SmtSupplierPersonService personService) {
		return new TestableSecurityAreaSupplierService(supplierMapper, personService,
				Mockito.mock(SmtImageService.class), Mockito.mock(SmtParkService.class), Mockito.mock(ImageService.class));
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
