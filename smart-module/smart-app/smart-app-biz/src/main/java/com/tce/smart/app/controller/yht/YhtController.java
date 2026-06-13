package com.tce.smart.app.controller.yht;

import com.tce.smart.app.service.yht.YhtAuthService;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.core.wrapper.BaseController;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author sunfujian
 * @since 2021/10/12 10:05
 */
@RestController
@AllArgsConstructor
@RequestMapping("/yht")
public class YhtController extends BaseController {

	private final YhtAuthService yhtAuthService;

	@GetMapping("/user/badge")
	public Result<String> getUserBadge(@RequestParam("code") String code) {
		return success(yhtAuthService.getUserBadge(code));
	}
}
