package com.tce.smart.data.api.feign.consume;

import com.tce.smart.common.core.constant.SecurityConstants;
import com.tce.smart.common.core.constant.ServiceNameConstants;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.data.api.dto.consume.req.XfPlanMoneyReqDTO;
import com.tce.smart.data.api.dto.consume.resp.TxEmpCardRespDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 员工充值计划
 *
 * @author fushiping
 * @date 2020-8-9
 */
@FeignClient(value = ServiceNameConstants.SMART_DATA)
public interface RemoteXfPlanMoneyCardService {

	/**
	 * 批量新增充值计划
	 */
	@PostMapping("/xfMoney/save")
	Result<List<String>> save(@RequestBody List<XfPlanMoneyReqDTO> reqDTO,
									 @RequestHeader(SecurityConstants.FROM) String from);

	/**
	 * 单个新增充值计划
	 */
	@PostMapping("/xfMoney/save/single")
	Result<Boolean> saveSinglePlan(@RequestBody XfPlanMoneyReqDTO reqDTO,
							  @RequestHeader(SecurityConstants.FROM) String from);

}
