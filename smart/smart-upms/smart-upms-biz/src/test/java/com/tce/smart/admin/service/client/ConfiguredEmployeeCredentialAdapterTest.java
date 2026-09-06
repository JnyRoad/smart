package com.tce.smart.admin.service.client;

import com.tce.smart.admin.api.entity.SysUser;
import com.tce.smart.admin.mapper.SysUserMapper;
import org.junit.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** 本机演示适配器必须显式启用，常规配置不能把本地哈希误当作已对接 DHR。 */
public class ConfiguredEmployeeCredentialAdapterTest {
	@Test
	public void disabledModeFailsClosedWithoutReadingCredentials() {
		SysUserMapper mapper = mock(SysUserMapper.class);
		ConfiguredEmployeeCredentialAdapter adapter = new ConfiguredEmployeeCredentialAdapter(mapper, "disabled");
		assertFalse(adapter.verify("E100", "ValidCredential8X"));
		verify(mapper, never()).selectOne(any());
	}

	@Test
	public void explicitDemoModeChecksOnlyActiveDemoUserHash() {
		SysUserMapper mapper = mock(SysUserMapper.class);
		SysUser user = new SysUser();
		user.setDelFlag("0"); user.setLockFlag("0");
		user.setPassword(new BCryptPasswordEncoder().encode("ValidCredential8X"));
		when(mapper.selectOne(any())).thenReturn(user);
		ConfiguredEmployeeCredentialAdapter adapter = new ConfiguredEmployeeCredentialAdapter(mapper, "demo");
		assertTrue(adapter.verify("E100", "ValidCredential8X"));
		assertFalse(adapter.verify("E100", "wrong"));
	}
}
