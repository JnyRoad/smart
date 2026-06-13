package com.tce.smart.platform.api.feign.badge;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.tce.smart.common.core.constant.SecurityConstants;
import com.tce.smart.common.core.constant.ServiceNameConstants;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.platform.api.dto.req.badge.QueryLossInfoReqDTO;
import com.tce.smart.platform.api.dto.resp.badge.BadgeLossInfoRespDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

/**
 * 厂牌挂失
 * @author fushiping
 * @date 2020/7/10 15:34
 **/
@FeignClient(value = ServiceNameConstants.PLATFORM_SERVICE)
public interface RemoteBadgeLossService {


	/**
	 * 保存挂失记录
	 * @param parkId
	 * @param staffNo
	 * @param from
	 * @return
	 */
	@GetMapping("/badge/loss/save")
	Result<Boolean> save(@RequestParam("parkId") Integer parkId, @RequestParam("staffNo") String staffNo,
						 @RequestHeader(SecurityConstants.FROM) String from);
}
