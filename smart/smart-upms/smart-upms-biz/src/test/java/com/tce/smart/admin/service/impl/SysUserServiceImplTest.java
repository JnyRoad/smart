package com.tce.smart.admin.service.impl;

import com.tce.smart.admin.api.entity.SysUser;
import com.tce.smart.admin.api.feign.RemoteEvwEmphrYsService;
import com.tce.smart.admin.service.SysRoleService;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/** 可选的薪资资料服务不可用时，认证链仍应返回基础身份与权限。 */
public class SysUserServiceImplTest {
	@Test
	public void findUserInfoContinuesWhenOptionalSalaryServiceIsUnavailable() {
		SysUserServiceImpl service = new SysUserServiceImpl();
		SysRoleService roles = mock(SysRoleService.class);
		RemoteEvwEmphrYsService salaries = mock(RemoteEvwEmphrYsService.class);
		ReflectionTestUtils.setField(service, "sysRoleService", roles);
		ReflectionTestUtils.setField(service, "remoteEvwEmphrYsService", salaries);
		when(roles.findRolesByUserId(101)).thenReturn(Collections.emptyList());
		when(salaries.info(anyString(), anyString())).thenThrow(new IllegalStateException("demo service offline"));
		SysUser user = new SysUser();
		user.setUserId(101);
		user.setUsername("APP_EMPLOYEE");

		Assert.assertSame(user, service.findUserInfo(user).getSysUser());
	}
}
