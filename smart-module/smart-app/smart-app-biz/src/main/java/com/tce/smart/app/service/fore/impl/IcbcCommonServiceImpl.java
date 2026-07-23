package com.tce.smart.app.service.fore.impl;

import com.tce.smart.app.service.fore.IcbcCommonService;
import com.tce.smart.common.core.exception.TCEException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 工商银行实名服务。
 *
 * ICBC UI SDK 会把实名资料装进客户端 HTML；该模式已被停用。
 * 后续必须接入银行的服务端受理 API，不能恢复客户端表单兼容分支。
 */
@Service
@Slf4j
public class IcbcCommonServiceImpl implements IcbcCommonService {

	@Override
	public Boolean initializeEaccount() {
		// 旧 UI SDK 模式已停用，必须先拒绝，禁止读取身份证资料或发起内部员工查询。
		log.warn("工商银行实名流程已拒绝不安全的客户端表单模式 purpose=icbc-eaccount");
		throw new TCEException("银行实名服务正在进行安全升级，请使用银行服务端实名流程");
	}
}
