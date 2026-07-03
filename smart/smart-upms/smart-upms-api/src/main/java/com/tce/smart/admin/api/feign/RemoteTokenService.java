package com.tce.smart.admin.api.feign;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tce.smart.common.core.constant.SecurityConstants;
import com.tce.smart.common.core.constant.ServiceNameConstants;
import com.tce.smart.common.core.model.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 *
 * @date 2018/9/4
 */
@FeignClient(value = ServiceNameConstants.AUTH_SERVICE)
public interface RemoteTokenService {
	/**
	 * 分页查询token 信息
	 *
	 * @param from   内部调用标志
	 * @param params 分页参数
	 * @param from   内部调用标志
	 * @return page
	 */
	@PostMapping("/token/page")
    Result<Page> getTokenPage(@RequestBody Map<String, Object> params, @RequestHeader(SecurityConstants.FROM) String from);

	/**
	 * 删除token
	 *
	 * @param from  内部调用标志
	 * @param token token
	 * @param from  内部调用标志
	 * @return
	 */
	@PostMapping("/token/{token}")
    Result<Boolean> removeTokenById(@PathVariable("token") String token, @RequestHeader(SecurityConstants.FROM) String from);

	/**
	 * 按 clientId 批量吊销该客户端签发的所有 token（access token + refresh token）。
	 * 用于 upms 重置 client secret 或删除 client 后，让旧 token 立即失效。
	 *
	 * @param clientId 客户端ID
	 * @param from     内部调用标志
	 * @return 吊销的 token 数量
	 */
	@DeleteMapping("/token/client/{clientId}")
	Result<Integer> removeTokensByClientId(@PathVariable("clientId") String clientId, @RequestHeader(SecurityConstants.FROM) String from);
}
