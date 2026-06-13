package com.tce.smart.platform.api.feign;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tce.smart.common.core.constant.SecurityConstants;
import com.tce.smart.common.core.constant.ServiceNameConstants;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.platform.api.dto.req.SmtDormitoryRepairsReqDTO;
import com.tce.smart.platform.api.dto.resp.SmtDormitoryRepairsRespDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * @description: 宿舍报修
 * @date: 2020-07-21 14:38
 * @author: wuling
 * @version: 1.0
 */
@FeignClient(value = ServiceNameConstants.PLATFORM_SERVICE)
public interface RemoteSmtDormitoryRepairsService {

	/**
	 * 添加宿舍报修
	 * @param smtDormitoryRepairsReqDTO 报修信息
	 * @return Result
	 */
	@PostMapping("/dormitory/repair/add")
	Result<Boolean> add(@RequestBody SmtDormitoryRepairsReqDTO smtDormitoryRepairsReqDTO, @RequestHeader(SecurityConstants.FROM) String from);


	/**
	 * 查询员工报修记录
	 * @param staffId 员工Id
	 * @return Result
	 */
	@GetMapping("/dormitory/repair/query/record")
	Result<Page<SmtDormitoryRepairsRespDTO>> queryByStaffId(@RequestBody Page page, @RequestParam("staffId") Long staffId, @RequestHeader(SecurityConstants.FROM) String from);

	/**
	 * 查询员工报修记录详细
	 * @param id 记录Id
	 * @return Result
	 */
	@GetMapping("/dormitory/repair/query/detail/{id}")
	Result<SmtDormitoryRepairsRespDTO> getStaffReportDetail(@PathVariable("id") Long id, @RequestHeader(SecurityConstants.FROM) String from);

	@GetMapping("/dormitory/repair/enum/range")
	Result<List<Map<String, Object>>> getRange(@RequestHeader(SecurityConstants.FROM) String from);
}
