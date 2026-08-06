package com.tce.smart.app.service.fore;

/**
 * 工商银行服务接口
 *
 * @author mkwu
 * @date 2019-08-23
 */
public interface IcbcCommonService {

	/**
	 * 初始化银行实名流程；不允许向客户端返回包含身份证号的银行 HTML 表单。
	 */
	Boolean initializeEaccount();

}
