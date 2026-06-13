package com.tce.smart.platform.api.feign;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tce.smart.common.core.constant.SecurityConstants;
import com.tce.smart.common.core.constant.ServiceNameConstants;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.platform.api.dto.*;
import com.tce.smart.platform.api.dto.req.AddBreakOffApplicationReqDTO;
import com.tce.smart.platform.api.dto.req.SearchPatchReqDTO;
import com.tce.smart.platform.api.dto.resp.SearchBreakOffTypeRespDTO;
import com.tce.smart.platform.api.dto.resp.SearchBreakoffApplicationRespDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 调休申请管理
 * @author 梁园
 *
 */
@FeignClient(value = ServiceNameConstants.PLATFORM_SERVICE)
public interface RemoteBreakOffApplicationService {

	/**
	 * 查看调休列表
	 * @param current
	 * @param size
	 * @param staffBadge
	 * @param from
	 * @return
	 */
	@GetMapping("/application/breakOff/page")
	Result<Page<SearchBreakoffApplicationRespDTO>> getSmtBreakOffApplicationPage(@RequestParam("current") final long current, @RequestParam("size") final long size, @RequestParam("staffBadge") final String staffBadge, @RequestHeader(SecurityConstants.FROM) String from);


	/**
	 * 通过id查询调休申请表
	 * @param id
	 * @return
	 */
	@GetMapping("/application/breakOff/detail/{id}")
	 Result<SearchBreakoffApplicationDetailDTO> getById(@PathVariable("id") Integer id, @RequestHeader(SecurityConstants.FROM) String from);

	/**
	 * 添加调休申请
	 * @param addBreakoffApplicationDTO 实体
	 * @return success、false
	 */
	@PostMapping("/application/breakOff/add")
    Result<?>saveBreakOffApplication(@RequestBody AddBreakOffApplicationReqDTO addBreakoffApplicationDTO, @RequestHeader(SecurityConstants.FROM) String from);

	/**
	 * 获取调休类型
	 * @return
	 */
	@GetMapping("/application/breakOff/type")
	 Result<List<SearchBreakOffTypeRespDTO>> getBreakOffTypeList(@RequestHeader(SecurityConstants.FROM) String from);

	/**
	 *  获取可调休天数
	 * @return
	 */
	 @PostMapping("/application/breakOff/getRestCount")
	 Result<List<SmtBreakoffApplicationDTO>> getRestCountList(@RequestBody SearchPatchReqDTO searchPatchDTO, @RequestHeader(SecurityConstants.FROM) String from);

 }
