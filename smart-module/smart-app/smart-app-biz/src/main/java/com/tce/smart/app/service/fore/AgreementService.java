package com.tce.smart.app.service.fore;

import com.tce.smart.app.vo.fore.AgreementDetailVo;

/**
 * 协议服务接口
 *
 * @author mckaywu
 * @date 2019-06-17 10:03:12
 */
public interface AgreementService {

	/**
	 * 查App服务协议
	 *
	 * @param parkId 园区ID
	 * @return 协议详情
	 */
	AgreementDetailVo getRootOutAgree(final String parkId);

	/**
	 * 查App服务协议
	 *
	 * @return 协议详情
	 */
	AgreementDetailVo getServiceAgree();
}
