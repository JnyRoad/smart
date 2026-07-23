package com.tce.smart.app.service.fore.impl;

import com.tce.smart.app.service.fore.IcbcCommonService;
import com.tce.smart.common.core.constant.SecurityConstants;
import com.tce.smart.common.core.exception.TCEException;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.security.util.SecurityUtils;
import com.tce.smart.platform.api.dto.resp.InternalStaffIdentityRespDTO;
import com.tce.smart.platform.api.feign.RemoteStaffInternalService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * 工商银行实名服务。
 *
 * ICBC UI SDK 会把实名资料装进客户端 HTML；该模式已被停用。
 * 后续必须接入银行的服务端受理 API，不能恢复客户端表单兼容分支。
 */
@Service
@Slf4j
public class IcbcCommonServiceImpl implements IcbcCommonService {

	@Autowired
	private RemoteStaffInternalService remoteStaffInternalService;

	@Override
	public Boolean initializeEaccount() {
		String badge = SecurityUtils.getUser().getUsername();
		Result<InternalStaffIdentityRespDTO> identityStaffResponse = remoteStaffInternalService.getIdentityStaff(badge,
				SecurityConstants.FROM_IN, SecurityConstants.INTERNAL_SERVICE_AUTH_REQUIRED, "icbc-eaccount");
		if (!identityStaffResponse.isSuccess() || Objects.isNull(identityStaffResponse.getData())) {
			throw new TCEException("银行实名资料校验失败");
		}
		// 不再调用 UI SDK 生成表单：那会把身份证号再次带回客户端。
		log.warn("工商银行实名流程已拒绝不安全的客户端表单模式 purpose=icbc-eaccount");
		throw new TCEException("银行实名服务正在进行安全升级，请使用银行服务端实名流程");
	}
}
