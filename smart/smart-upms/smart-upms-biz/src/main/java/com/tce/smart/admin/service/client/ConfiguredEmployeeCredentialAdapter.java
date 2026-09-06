package com.tce.smart.admin.service.client;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.tce.smart.admin.api.entity.SysUser;
import com.tce.smart.admin.mapper.SysUserMapper;
import com.tce.smart.common.core.constant.CommonConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * 默认关闭的 DHR 适配器占位。仅当隔离本机演示显式设为 demo 时，才用演示数据库
 * 中的 BCrypt 凭据模拟 DHR 成功；任何未配置或未知模式均失败关闭，绝不假装真实对接。
 */
@Component
@Slf4j
public class ConfiguredEmployeeCredentialAdapter implements ClientEmployeeCredentialAdapter {
	private static final PasswordEncoder ENCODER = new BCryptPasswordEncoder();
	private final SysUserMapper users;
	private final String mode;

	public ConfiguredEmployeeCredentialAdapter(SysUserMapper users,
			@Value("${smart.client.employee-auth.mode:disabled}") String mode) {
		this.users = users;
		this.mode = mode;
	}

	@Override
	public boolean verify(String staffNo, String password) {
		if (!"demo".equalsIgnoreCase(mode)) return false;
		try {
			SysUser user = users.selectOne(Wrappers.<SysUser>query().lambda().eq(SysUser::getUsername, staffNo));
			return available(user) && ENCODER.matches(password, user.getPassword());
		} catch (Exception failure) {
			log.warn("DHR 演示凭据校验失败，类型={}", failure.getClass().getName());
			return false;
		}
	}

	private boolean available(SysUser user) {
		return user != null && CommonConstants.STATUS_NORMAL.equals(user.getDelFlag())
				&& CommonConstants.STATUS_NORMAL.equals(user.getLockFlag()) && user.getPassword() != null;
	}
}
