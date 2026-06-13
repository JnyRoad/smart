package com.tce.smart.platform.controller;

import com.tce.smart.common.core.model.Result;
import com.tce.smart.platform.api.dto.req.StaffFamilyDormitoryReqDTO;
import com.tce.smart.platform.service.SmtStaffFamilyDormitoryService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @description: 员工家属宿舍入住管理
 * @date: 2020-12-08 9:57
 * @author: wuling
 * @version: 1.0
 */
@Api(tags = "员工家属宿舍入住管理")
@RestController
@AllArgsConstructor
@RequestMapping("/dormitory/staff/family")
public class SmtStaffFamilyDormitoryController {

	private final SmtStaffFamilyDormitoryService smtStaffFamilyDormitoryService;

	/**
	 * 添加家属
	 * @param staffFamilyDormitoryReqDTO
	 * @return
	 */
	@ApiOperation("添加家属")
	@PostMapping("/add")
	public Result<Boolean> addFamily(@RequestBody StaffFamilyDormitoryReqDTO staffFamilyDormitoryReqDTO) {
		return new Result<>(smtStaffFamilyDormitoryService.addFamily(staffFamilyDormitoryReqDTO));
	}

	/**
	 * 删除家属
	 * @return
	 */
	@ApiOperation("删除家属")
	@GetMapping("/del/{id}")
	public Result<Boolean> delFamily(@ApiParam(name = "id",value = "记录id",required = true) @PathVariable Long id){
		return new Result<>(smtStaffFamilyDormitoryService.delFamily(id));
	}

	/**
	 * 查询家属
	 * @return
	 */
	@ApiOperation("查询家属")
	@GetMapping("/query/{staffBadge}")
	public Result<List<StaffFamilyDormitoryReqDTO>> queryFamily(@ApiParam(name = "staffBadge",value = "记录id",required = true) @PathVariable String staffBadge){
		return new Result<>(smtStaffFamilyDormitoryService.queryFamily(staffBadge));
	}
}
