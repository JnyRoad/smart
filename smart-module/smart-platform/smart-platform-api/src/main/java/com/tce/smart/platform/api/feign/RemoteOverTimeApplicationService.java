package com.tce.smart.platform.api.feign;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tce.smart.common.core.constant.SecurityConstants;
import com.tce.smart.common.core.constant.ServiceNameConstants;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.platform.api.dto.req.AddOverTimeApplicationReqDTO;
import com.tce.smart.platform.api.dto.resp.SearchOverClassTimeTypeRespDTO;
import com.tce.smart.platform.api.dto.resp.SearchOverTimeApplicationDetailRespDTO;
import com.tce.smart.platform.api.dto.resp.SearchOverTimeApplicationRespDTO;
import com.tce.smart.platform.api.dto.resp.SearchOverTimeTypeRespDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 加班申请管理
 * @author 梁园
 *
 */
@FeignClient(value = ServiceNameConstants.PLATFORM_SERVICE)
public interface RemoteOverTimeApplicationService {


	/**
	 * 查看加班列表
	 * @param current
	 * @param size
	 * @param staffBadge
	 * @param from
	 * @return
	 */
	@GetMapping("/application/overTime/page")
	Result<Page<SearchOverTimeApplicationRespDTO>> getOvertimeApplicationPage(@RequestParam("current") final long current, @RequestParam("size") final long size, @RequestParam("staffBadge") final String staffBadge, @RequestHeader(SecurityConstants.FROM) String from);


	/**
	 * 通过id查询加班申请表
	 * @param id
	 * @return
	 */
	@GetMapping("/application/overTime/detail/{id}")
	 Result<SearchOverTimeApplicationDetailRespDTO> getOverTimeById(@PathVariable("id") Integer id, @RequestHeader(SecurityConstants.FROM) String from);

	/**
	 * 添加加班申请
	 * @param addOverApplicationDTO 实体
	 * @return success、false
	 */
	@PostMapping("/application/overTime/add")
    Result<?>save(@RequestBody AddOverTimeApplicationReqDTO addOverApplicationDTO, @RequestHeader(SecurityConstants.FROM) String from);

	/**
	 * 获取班别类型
	 * @return
	 */
	@GetMapping("/application/overTime/classType")
	 Result<List<SearchOverClassTimeTypeRespDTO>> getOverClassTypeList(@RequestHeader(SecurityConstants.FROM) String from);

	/**
	 * 获取加班类型
	 * @return
	 */
	@GetMapping("/application/overTime/overTimeType")
	Result<List<SearchOverTimeTypeRespDTO>> getOverTypeList(@RequestHeader(SecurityConstants.FROM) String from);

 }
