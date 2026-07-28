package com.tce.smart.data.api.feign.ehrview;


import com.tce.smart.common.core.constant.SecurityConstants;
import com.tce.smart.common.core.constant.ServiceNameConstants;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.data.api.dto.ehrview.req.OvwYsConComanyReqDTO;
import com.tce.smart.data.api.dto.ehrview.resp.OvwYsConComanyRespDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/***
 * description: 合同签约单位feign接口 <br>
 * date: 2019/11/27 14:03 <br>
 * author: mckaywu <br>
 * version: 1.0 <br>
 */
@FeignClient(value = ServiceNameConstants.SMART_DATA)
public interface RemoteOvwYsConComanyService {

	/**
	 * 根据title查询列表
	 *
	 * @param ovwYsConComany ovwYsConComany
	 * @return Result<List < OvwYsConComany>>
	 */
	@PostMapping("/ys/con/comany/getByTitle")
	Result<List<OvwYsConComanyRespDTO>> getByTitle(@RequestBody OvwYsConComanyReqDTO ovwYsConComany,
													   @RequestHeader(SecurityConstants.FROM) String from,
													   @RequestHeader(SecurityConstants.INTERNAL_SERVICE_AUTH) String serviceAuth);

	/**
	 * 根据compId查询
	 *
	 * @param compId compId
	 * @return Result<OvwYsConComany>
	 */
	@GetMapping("/ys/con/comany/getByCompId/{compId}")
	Result<OvwYsConComanyRespDTO> getByCompId(@PathVariable("compId") Integer compId,
			@RequestHeader(SecurityConstants.FROM) String from,
			@RequestHeader(SecurityConstants.INTERNAL_SERVICE_AUTH) String serviceAuth);
}
