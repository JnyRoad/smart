package com.tce.smart.app.controller.fore;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tce.smart.app.ao.fore.DeviceRegisterAo;
import com.tce.smart.common.core.constant.SecurityConstants;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.core.wrapper.BaseController;
import com.tce.smart.platform.api.dto.req.SmtDormitoryRepairsReqDTO;
import com.tce.smart.platform.api.dto.resp.SmtDormitoryRepairsRespDTO;
import com.tce.smart.platform.api.feign.RemoteSmtDormitoryRepairsService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import java.util.List;
import java.util.Map;

/**
 * @description: DormitoryRepairController
 * @date: 2020-07-21 15:07
 * @author: wuling
 * @version: 1.0
 */
@RestController
@AllArgsConstructor
@ApiIgnore
@Api(tags = "园区报修")
@RequestMapping("/dormitory/repair")
public class DormitoryRepairController extends BaseController {

	private final RemoteSmtDormitoryRepairsService smtDormitoryRepairsService;

	/**
	 * 添加宿舍报修记录
	 *
	 * @param smtDormitoryRepairsReqDTO 宿舍报修信息
	 * @return 成功-true
	 */
	@PostMapping("/add")
	@ApiOperation("申请报修")
	public Result<Boolean> addDormitoryRepair(@RequestBody SmtDormitoryRepairsReqDTO smtDormitoryRepairsReqDTO) {
		return smtDormitoryRepairsService.add(smtDormitoryRepairsReqDTO, SecurityConstants.FROM_IN);
	}

	/**
	 * 查询员工报修记录
	 *
	 * @param staffId 员工标识
	 * @return
	 */
	@GetMapping("/query/record")
	@ApiOperation("查询员工报修记录")
	public Result<Page<SmtDormitoryRepairsRespDTO>> queryByStaffId(@RequestParam("current") final Integer current, @RequestParam("size") final Integer size,@RequestParam("staffId") final Long staffId) {
		Page page = new Page(current,size);
		return smtDormitoryRepairsService.queryByStaffId(page,staffId, SecurityConstants.FROM_IN);
	}

	/**
	 * 查询员工报修记录
	 *
	 * @param id 员工标识
	 * @return
	 */
	@GetMapping("/query/detail/{id}")
	@ApiOperation("查询员工报修记录")
	public Result<SmtDormitoryRepairsRespDTO> getStaffReportDetail(@PathVariable("id") Long id) {
		return smtDormitoryRepairsService.getStaffReportDetail(id, SecurityConstants.FROM_IN);
	}

	@GetMapping("/enum/range")
	public Result<List<Map<String, Object>>> getRange(){
		return smtDormitoryRepairsService.getRange(SecurityConstants.FROM_IN);
	}
}
