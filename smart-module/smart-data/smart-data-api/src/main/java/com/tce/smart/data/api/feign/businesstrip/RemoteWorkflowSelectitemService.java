package com.tce.smart.data.api.feign.businesstrip;

import com.tce.smart.common.core.constant.SecurityConstants;
import com.tce.smart.common.core.constant.ServiceNameConstants;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.data.api.dto.oa.resp.WorkflowSelectitemRespDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 *
 * @date 2021-05-27
 */
@FeignClient(value = ServiceNameConstants.SMART_DATA)
public interface RemoteWorkflowSelectitemService {

	/**
	 * 获取所有OA区域列表
	 * @return
	 */
	@GetMapping("/oa/view/list")
	Result<List<WorkflowSelectitemRespDTO>> getList(@RequestParam("selectIdList") List<Integer> selectIdList, @RequestParam("fieldId") Integer fieldId,
													@RequestHeader(SecurityConstants.FROM) String from) ;

}
