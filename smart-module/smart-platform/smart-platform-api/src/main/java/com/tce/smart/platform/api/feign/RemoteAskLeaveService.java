package com.tce.smart.platform.api.feign;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tce.smart.common.core.constant.SecurityConstants;
import com.tce.smart.common.core.constant.ServiceNameConstants;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.platform.api.dto.req.AddAskLeavelApplicationReqDTO;
import com.tce.smart.platform.api.dto.resp.SearchAskLeaveApplicationDetailRespDTO;
import com.tce.smart.platform.api.dto.resp.SearchAskLeaveApplicationRespDTO;
import com.tce.smart.platform.api.dto.resp.SearchAskLeaveTypeRespDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 请假信息
 * @author ly
 * @date 2019-05-10 17:19:50
 */
@FeignClient(value = ServiceNameConstants.PLATFORM_SERVICE)
public interface RemoteAskLeaveService {

	/**
	 * 分页获取请假信息
	 * @param current
	 * @param size
	 * @param staffBadge
	 * @param from
	 * @return
	 */
	@GetMapping("/application/askLeave/page")
	Result<Page<SearchAskLeaveApplicationRespDTO>> getAskLeavePage(@RequestParam("current") final long current, @RequestParam("size") final long size, @RequestParam("staffBadge") final String staffBadge, @RequestHeader(SecurityConstants.FROM) String from);

	/**
	 * 根据请假的id查询请假的数据
	 * @param id
	 * @param from
	 * @return
	 */
	@GetMapping("/application/askLeave/detail/{id}")
	Result<SearchAskLeaveApplicationDetailRespDTO> getAskLeaveById(@RequestParam("id") Integer id, @RequestHeader(SecurityConstants.FROM) String from);

	/**
	 * 请假申请
	 * @param addAskLeavelApplicationDTO
	 * @param from
	 * @return
	 */
	@PostMapping("/application/askLeave/add")
	Result<?> add(@RequestBody AddAskLeavelApplicationReqDTO addAskLeavelApplicationDTO, @RequestHeader(SecurityConstants.FROM) String from);
	/**
	 *
	 * 获取请假类型
	 * @return Result<List<SearchAskLeaveTypeVO>>
	 */
	@GetMapping("/application/askLeave/type")
	Result<List<SearchAskLeaveTypeRespDTO>> getAskTypeList(@RequestHeader(SecurityConstants.FROM) String from);
}
