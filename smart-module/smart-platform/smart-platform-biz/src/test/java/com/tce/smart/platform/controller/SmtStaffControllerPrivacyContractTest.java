package com.tce.smart.platform.controller;

import com.tce.smart.platform.api.dto.resp.StaffLookupRespDTO;
import org.junit.Test;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * 员工查询接口的隐私契约测试。
 *
 * 外部查询响应只能保留业务识别所需的最小字段，且历史上直接返回员工实体的
 * 三个工号查询入口不得以兼容别名继续存在。
 */
public class SmtStaffControllerPrivacyContractTest {

	@Test
	public void staffLookupDtoDoesNotExposeSensitiveProperties() {
		Set<String> names = Arrays.stream(StaffLookupRespDTO.class.getDeclaredFields())
				.map(Field::getName)
				.collect(Collectors.toSet());

		assertEquals(new HashSet<>(Arrays.asList("staffId", "badge", "name", "departmentName")), names);
	}

	@Test
	public void legacyBadgeHandlersAreNotPublicApiHandlers() {
		assertMethodDoesNotExist("getByBadge");
		assertMethodDoesNotExist("getOneByBadge");
		assertMethodDoesNotExist("getSimpleSttaffByBadge");
	}

	private void assertMethodDoesNotExist(String methodName) {
		try {
			SmtStaffController.class.getMethod(methodName, String.class);
			fail("历史员工实体查询入口不应继续暴露：" + methodName);
		} catch (NoSuchMethodException expected) {
			// 预期：已删除历史公开入口。
		}
	}
}
