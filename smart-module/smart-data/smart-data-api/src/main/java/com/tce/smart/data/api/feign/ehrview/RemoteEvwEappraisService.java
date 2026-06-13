package com.tce.smart.data.api.feign.ehrview;

import com.tce.smart.common.core.constant.ServiceNameConstants;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.data.api.dto.ehrview.EvwEappraisDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 *
 * @date 2018/6/22
 */
@FeignClient(value = ServiceNameConstants.SMART_DATA)
public interface RemoteEvwEappraisService {
	/**
	 * 根据员工号获取评优人员信息
	 *
	 * @param badge
	 * @param badge     调用标志
	 * @return Result
	 */
	@GetMapping("/eapprais/info")
    Result<EvwEappraisDTO> info(@RequestParam("badge") String badge);

}
