package com.tce.smart.platform.api.feign;

import com.tce.smart.common.core.constant.ServiceNameConstants;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.platform.api.dto.req.*;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * 申请审批
 * @author 梁圆
 *
 */
@FeignClient(value = ServiceNameConstants.PLATFORM_SERVICE)
public interface RemoteApplicationApprovalService {

	/**
	 * 调休审批
	 * @return success、false
	 */
	@PostMapping("")
    Result<?> getBreakApplicationApproval(@RequestBody AddBreakOffApplicationApprovalReqDTO addBreakOffApplicationApprovalReqDTO);

	/**
	 * 请假审批
	 * @return success、false
	 */
	@PostMapping("")
	Result<?> getLeaveApplicationApproval(@RequestBody AddAskLeavelApplicationReqDTO addAskLeavelApplicationReqDTO);
	/**
	 * 加班审批
	 * @return success、false
	 */
	@PostMapping("")
	Result<?> getOverTimeApplicationApproval(@RequestBody AddOverTimeApplicationReqDTO addOverTimeApplicationReqDTO);
	/**
	 * 出差审批
	 * @return success、false
	 */
	@PostMapping("")
	Result<?> getTravelApplicationApproval(@RequestBody AddSmtTravelApplicationReqDTO addSmtTravelApplicationReqDTO);
	/**
	 * 补卡审批
	 * @return success、false
	 */
	@PostMapping("")
	Result<?> getReplaceApplicationApproval(@RequestBody AddReplaceApplicationReqDTO addReplaceApplicationReqDTO);
}
