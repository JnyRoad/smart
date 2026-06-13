package com.tce.smart.platform.api.feign.badge;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.tce.smart.common.core.constant.SecurityConstants;
import com.tce.smart.common.core.constant.ServiceNameConstants;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.platform.api.dto.req.badge.EditBadgeApplyReqDTO;
import com.tce.smart.platform.api.dto.req.badge.QueryLossInfoReqDTO;
import com.tce.smart.platform.api.dto.resp.badge.BadgeApplyDetailRespDTO;
import com.tce.smart.platform.api.dto.resp.badge.BadgeApplyRecordRespDTO;
import com.tce.smart.platform.api.dto.resp.badge.BadgeLossInfoRespDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

/**
 * 厂牌补领
 * @author fushiping
 * @date 2020/7/10 15:34
 **/
@FeignClient(value = ServiceNameConstants.PLATFORM_SERVICE)
public interface RemoteBadgeApplyService {

	/**
	 * 获得个人挂失记录
	 * @param from
	 * @return
	 */
	@PostMapping("/badge/apply/page")
	Result<IPage<BadgeApplyRecordRespDTO>> getList(@RequestParam("current") final Integer current, @RequestParam("size") final Integer size,
												   @RequestHeader(SecurityConstants.FROM) String from);

	/**
	 * 新增补领记录
	 * @param reqDTO
	 * @param from
	 * @return
	 */
	@PostMapping("/badge/apply/save")
	Result<Boolean> save(@RequestBody EditBadgeApplyReqDTO reqDTO,
						 @RequestHeader(SecurityConstants.FROM) String from);

	/**
	 * 补领记录详情
	 * @param id 补领申请id
	 * @param from
	 * @return
	 */
	@GetMapping("/badge/apply/detail")
	Result<BadgeApplyDetailRespDTO> getById(@RequestParam("id") Long id,
											@RequestHeader(SecurityConstants.FROM) String from);
}
