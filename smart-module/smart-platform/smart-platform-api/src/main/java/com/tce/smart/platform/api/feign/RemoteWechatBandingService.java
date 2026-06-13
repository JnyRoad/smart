package com.tce.smart.platform.api.feign;

import com.tce.smart.common.core.constant.SecurityConstants;
import com.tce.smart.common.core.constant.ServiceNameConstants;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.platform.api.dto.req.WechatBandingReqDTO;
import com.tce.smart.platform.api.dto.resp.WechatBandingRespDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

/**
 * @author fushiping
 * @date 2021/10/11 0011 09:57
 **/
@FeignClient(value = ServiceNameConstants.PLATFORM_SERVICE)
public interface RemoteWechatBandingService {

	/**
	 * 新增绑定信息
	 */
	@PostMapping("/wechat/banding/save")
	Result<Boolean> save(@RequestBody WechatBandingReqDTO wechatBandingReqDTO, @RequestHeader(SecurityConstants.FROM) String from);

	/**
	 * 根据openId获取工号信息
	 * @param openId
	 */
	@GetMapping("/wechat/banding/info/{openId}")
	Result<WechatBandingRespDTO> getByOpenId(@PathVariable("openId") String openId, @RequestHeader(SecurityConstants.FROM) String from);

	/**
	 * 解绑微信openId与工号的关系
	 * @param id
	 * @return
	 */
	@PostMapping("/wechat/banding/{id}")
	Result<Boolean> removeById(@PathVariable("id") Long id);
}
