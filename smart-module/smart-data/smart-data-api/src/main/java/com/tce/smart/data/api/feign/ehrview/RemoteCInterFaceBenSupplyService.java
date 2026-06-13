package com.tce.smart.data.api.feign.ehrview;

import com.tce.smart.common.core.constant.SecurityConstants;
import com.tce.smart.common.core.constant.ServiceNameConstants;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.data.api.dto.ehrview.AvaGetskyPayYSHRDTO;
import com.tce.smart.data.api.dto.ehrview.req.AvaGetskyPayYSHRReqDTO;
import com.tce.smart.data.api.dto.ehrview.req.CInterFaceBenSupplyReqDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 费用结算
 * @author wuling
 *
 */

@FeignClient(value = ServiceNameConstants.SMART_DATA)
public interface RemoteCInterFaceBenSupplyService {


	/**
	 * 推送员工费用到EHR
	 * @param cInterFaceBenSupplyReqDTO
	 * @return
	 */
	  @GetMapping("/cinter/supply/save")
	  Result<Boolean> save(@RequestBody CInterFaceBenSupplyReqDTO cInterFaceBenSupplyReqDTO,
									   @RequestHeader(SecurityConstants.FROM) String from);

}
