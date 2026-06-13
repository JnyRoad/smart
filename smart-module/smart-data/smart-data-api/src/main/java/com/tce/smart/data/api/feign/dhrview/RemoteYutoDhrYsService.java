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
	 * 根据 bu列表 获取DHR员工信息
	 *
	 * @param from     调用标志
	 * @return Result
	 */
	@GetMapping("/empdhr/ys/page")
    Result<Page<YutoDhrPsndoDTO>> page(@RequestParam("current") Long current,
									   @RequestParam("size") Long size,
									   @RequestParam("buIds") List<Integer> buIds,
									   @RequestHeader(SecurityConstants.FROM) String from);

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
