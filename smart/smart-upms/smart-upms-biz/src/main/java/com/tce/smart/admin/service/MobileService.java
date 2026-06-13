package com.tce.smart.admin.service;

import com.tce.smart.common.core.model.Result;

public interface MobileService {
	/**
	 * 发送手机验证码
	 *
	 * @param mobile mobile
	 * @return code
	 */
	Result<Boolean> sendSmsCode(String mobile);
}
