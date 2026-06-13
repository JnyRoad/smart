package com.tce.smart.data.api.feign.temporary;

import com.tce.smart.common.core.constant.ServiceNameConstants;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.data.api.dto.temporary.resp.OcompanyRespDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 *
 * @author QIPEI
 *
 */
@FeignClient(value = ServiceNameConstants.SMART_DATA)
public interface RemoteOcompanyService {

	/**
	 * 通过compid获取ocompay
	 * @param compId
	 * @return
	 */
	@GetMapping("/company/getByComId")
	Result<OcompanyRespDTO> getByComId(@RequestParam("compId") Integer compId);

}
