package com.tce.smart.platform.api.feign;

import com.tce.smart.common.core.constant.ServiceNameConstants;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.platform.api.dto.req.SmtStaffAppealReqDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * @description: RemoteSmtStaffAppealService
 * @date: 2020-07-23 14:47
 * @author: wuling
 * @version: 1.0
 */
@FeignClient(value = ServiceNameConstants.PLATFORM_SERVICE)
public interface RemoteSmtStaffAppealService {

	/**
	 * 保存员工申诉记录
	 * @param smtStaffAppealReqDTO 员工申诉信息
	 * @return Result
	 */
	@PostMapping("/staff/appeal/save")
	Result<Boolean> saveStaffAppealRecord(@RequestBody SmtStaffAppealReqDTO smtStaffAppealReqDTO);
}
