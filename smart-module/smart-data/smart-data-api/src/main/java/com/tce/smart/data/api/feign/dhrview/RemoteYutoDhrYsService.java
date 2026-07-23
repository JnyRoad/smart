package com.tce.smart.data.api.feign.dhrview;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tce.smart.common.core.constant.SecurityConstants;
import com.tce.smart.common.core.constant.ServiceNameConstants;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.data.api.dto.dhrview.resp.YutoDhrPsndoDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 *
 * @date 2021-05-27
 */
@FeignClient(value = ServiceNameConstants.SMART_DATA)
public interface RemoteYutoDhrYsService {

	/**
	 * 受控服务读取 BU 列表对应的 DHR 员工信息。
	 *
	 * 响应包含员工敏感信息，调用方必须通过服务令牌，不得复用旧通用分页路径。
	 *
	 * @param from     调用标志
	 * @return Result
	 */
	@GetMapping("/empdhr/ys/internal/page")
    Result<Page<YutoDhrPsndoDTO>> page(@RequestParam("current") Long current,
									   @RequestParam("size") Long size,
									   @RequestParam("buIds") List<Integer> buIds,
									   @RequestHeader(SecurityConstants.FROM) String from,
									   @RequestHeader(SecurityConstants.INTERNAL_SERVICE_AUTH) String serviceAuth);

	/**
	 * 根据员工工号获得员工性质
	 *
	 * @param from     调用标志
	 * @return Result
	 */
	@GetMapping("/empdhr/ys/get/properties")
	Result<String> getProperties(@RequestParam("badge") String badge,
									   @RequestHeader(SecurityConstants.FROM) String from);

	/**
	 * 根据userId获取员工工号
	 *
	 * @param from     调用标志
	 * @return Result
	 */
	@GetMapping("/empdhr/ys/badge/{userId}")
	Result<String> getBadgeByUserId(@PathVariable("userId") String userId,
								 @RequestHeader(SecurityConstants.FROM) String from);

}
