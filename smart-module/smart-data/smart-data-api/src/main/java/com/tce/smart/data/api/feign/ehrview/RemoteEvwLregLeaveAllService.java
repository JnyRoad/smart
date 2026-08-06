package com.tce.smart.data.api.feign.ehrview;

import java.util.List;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.tce.smart.common.core.constant.ServiceNameConstants;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.data.api.dto.ehrview.resp.EvwLregLeaveAllRespDTO;

/**
 * 历史请假
 * @author 齐佩
 *
 */
@FeignClient(value = ServiceNameConstants.SMART_DATA)
public interface RemoteEvwLregLeaveAllService {
	@GetMapping("/evwLregLeaveAll/info")
	Result<List<EvwLregLeaveAllRespDTO>> info(@RequestParam("badge") String badge, @RequestParam("beginTime") String beginTime,@RequestParam("endTime") String endTime);
}
