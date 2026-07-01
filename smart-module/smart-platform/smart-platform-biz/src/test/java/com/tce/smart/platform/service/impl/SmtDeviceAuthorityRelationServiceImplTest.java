package com.tce.smart.platform.service.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.tce.smart.platform.core.entity.SmtDeviceAuthorityRelation;
import com.tce.smart.platform.core.mapper.SmtDeviceAuthorityRelationMapper;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;

public class SmtDeviceAuthorityRelationServiceImplTest {

	@Test
	public void getRelationByDeviceIdQueriesByDeviceIdColumn() throws Exception {
		SmtDeviceAuthorityRelationMapper mapper = Mockito.mock(SmtDeviceAuthorityRelationMapper.class);
		SmtDeviceAuthorityRelationServiceImpl service = new SmtDeviceAuthorityRelationServiceImpl();
		setField(service, "baseMapper", mapper);

		SmtDeviceAuthorityRelation relation = new SmtDeviceAuthorityRelation();
		relation.setAuthorityId(100);
		relation.setDeviceId("device-A");
		Mockito.when(mapper.selectList(Mockito.any())).thenReturn(Arrays.asList(relation));

		List<SmtDeviceAuthorityRelation> result = service.getRelationByDeviceId("device-A");

		Assert.assertEquals(1, result.size());
		Assert.assertEquals("device-A", result.get(0).getDeviceId());
		@SuppressWarnings("unchecked")
		ArgumentCaptor<Wrapper<SmtDeviceAuthorityRelation>> wrapperCaptor = ArgumentCaptor.forClass(Wrapper.class);
		Mockito.verify(mapper).selectList(wrapperCaptor.capture());
	}

	private void setField(Object target, String name, Object value) throws Exception {
		Class<?> type = target.getClass();
		while (type != null) {
			try {
				Field field = type.getDeclaredField(name);
				field.setAccessible(true);
				field.set(target, value);
				return;
			} catch (NoSuchFieldException ignored) {
				type = type.getSuperclass();
			}
		}
		throw new NoSuchFieldException(name);
	}
}
