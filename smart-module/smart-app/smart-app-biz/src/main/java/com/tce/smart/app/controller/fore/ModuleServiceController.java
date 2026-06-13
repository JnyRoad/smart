package com.tce.smart.app.controller.fore;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tce.smart.app.service.fore.ForeModuleService;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.core.wrapper.BaseController;

/**
 * 服务模块Controller
 *
 * @author mingkai.wu
 * @date 2019-05-09 15:15:12
 */
@RestController
@RequestMapping("/service")
public class ModuleServiceController extends BaseController {

	@Autowired
	private ForeModuleService foreModuleService;

	/**
	 * 获取模块菜单信息
	 *
	 * @return Result<?>
	 */
	@GetMapping("/module/list")
	public Result<?> getQuestionList() {
		return success(foreModuleService.getForeModuleList());
	}
}
