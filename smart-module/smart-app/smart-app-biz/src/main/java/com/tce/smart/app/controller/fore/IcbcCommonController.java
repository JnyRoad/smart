package com.tce.smart.app.controller.fore;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tce.smart.app.service.fore.IcbcCommonService;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.core.wrapper.BaseController;

import lombok.AllArgsConstructor;

/**
 * 工商银行服务控制器
 *
 * @author mkwu
 * @date 2019-08-23
 */
@RestController
@AllArgsConstructor
@RequestMapping("/icbc")
public class IcbcCommonController extends BaseController {

	private final IcbcCommonService icbcCommonService;

	/**
	 * 初始化 e 钱包实名请求。
	 *
	 * @return 服务端受理结果，不返回银行 HTML 或身份证号
	 */
	@RequestMapping("/eaccount")
	public Result<Boolean> initializeEaccount() {
		return success(icbcCommonService.initializeEaccount());
	}

}
