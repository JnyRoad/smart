package com.tce.smart.admin.controller;

import com.tce.smart.admin.api.feign.RemoteTokenService;
import com.tce.smart.common.core.constant.SecurityConstants;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.log.annotation.SysLog;
import io.swagger.annotations.Api;
import lombok.AllArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * getTokenPage 管理
 */
@RestController
@AllArgsConstructor
@RequestMapping("/token")
@Api(value = "token", description = "令牌管理模块")
public class TokenController {
	private final RemoteTokenService remoteTokenService;

	/**
	 * 分页token 信息
	 *
	 * @param params 参数集
	 * @return token集合
	 */
	@GetMapping("/page")
	public Result getTokenPage(@RequestParam Map<String, Object> params) {
		return remoteTokenService.getTokenPage(params, SecurityConstants.FROM_IN);
	}

	/**
	 * 删除
	 *
	 * @param token getTokenPage
	 * @return success/false
	 */
	@SysLog("删除用户token")
	@PostMapping("/{token}")
	@PreAuthorize("@pms.hasPermission('sys_token_del')")
	public Result removeById(@PathVariable String token) {
		return remoteTokenService.removeTokenById(token, SecurityConstants.FROM_IN);
	}
}
