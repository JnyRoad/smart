package com.tce.smart.app.controller;

import com.tce.smart.app.entity.AppModuleInfo;
import com.tce.smart.app.service.AppNavigationService;
import com.tce.smart.app.vo.AppNavigationVo;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.core.wrapper.BaseController;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;
import static com.tce.smart.common.core.model.Result.success;

/**
 * @author fushiping
 * @date 2019/6/18 17:08
 * 获取APP首页导航菜单
 **/

@RestController
@AllArgsConstructor
@RequestMapping("/appnavigation")
public class AppNavigationController extends BaseController {

	private final AppNavigationService appNavigationService;

	@GetMapping("/serve")
	public Result getServe(){
		List<AppNavigationVo> list = appNavigationService.getNavigationMenu();
		return success(list);
	}

}
