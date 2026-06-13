package com.tce.smart.platform.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.log.annotation.SysLog;
import com.tce.smart.platform.api.dto.req.securityarea.SecurityAreaPersonUpdateReqDTO;
import com.tce.smart.platform.core.dto.SmtSupplierPersonDTO;
import com.tce.smart.platform.core.dto.SmtSupplierPersonUploadDTO;
import com.tce.smart.platform.core.dto.SmtVisitorSupplierFindDTO;
import com.tce.smart.platform.core.entity.SmtSecurityAreaSupplier;
import com.tce.smart.platform.core.entity.SmtSupplierPerson;
import com.tce.smart.platform.service.SmtSupplierPersonService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @description: 保密区供应商人员管理
 * @date: 2020-07-21 11:09
 * @author: wuling
 * @version: 1.0
 */
@Api(tags = "保密区供应商人员管理")
@RestController
@AllArgsConstructor
@RequestMapping("/securityarea/supplier/person")
public class SmtSupplierPersonController {

	private final SmtSupplierPersonService smtSupplierPersonService;

	/**
	 * 分页查询
	 * @param page 分页对象
	 * @param smtSupplierPersonDTO 保密区供应商人员
	 * @return
	 */
	@ApiOperation("分页查询")
	@GetMapping("/page")
	public Result getSecurityAreaSupplierPage(Page page, SmtSupplierPersonDTO smtSupplierPersonDTO) {
		return new Result<>(smtSupplierPersonService.getSupplierPersonPage(page, smtSupplierPersonDTO));
	}

	@ApiOperation("根据访客信息查询是否是保密区供应商人员")
	@PostMapping("/visitor/find")
	public Result getVisitorSupplier(@RequestBody SmtVisitorSupplierFindDTO dto) {
		return new Result<>(smtSupplierPersonService.getVisitorSupplier(dto));
	}

	/**
	 * 保存保密区供应商人员信息
	 * @param smtSupplierPerson 保密区供应商信息
	 * @return Result
	 */
	@ApiOperation("保存保密区供应商人员信息")
	@PostMapping("/save")
	public Result saveSecurityAreaSupplier(@RequestBody SmtSupplierPerson smtSupplierPerson){
		return new Result<>(smtSupplierPersonService.saveSupplierPerson(smtSupplierPerson));
	}

	/**
	 * 保存批量上传的保密区供应商人员信息
	 * @param smtSupplierPersonUploadDTO 批量保密区人员信息
	 * @return Result
	 */
	@ApiOperation("保存批量上传的保密区供应商人员信息")
	@PostMapping("/uploadSave")
	public Result saveUploadSecurityAreaSupplier(@RequestBody SmtSupplierPersonUploadDTO smtSupplierPersonUploadDTO){
		return new Result<>(smtSupplierPersonService.saveUploadSupplierPerson(smtSupplierPersonUploadDTO));
	}

	/**
	 * 通过id删除保密区供应商人员信息
	 * @param id id
	 * @return Result
	 */
	@ApiOperation("通过id删除保密区供应商人员信息")
	@PostMapping("/{id}")
	public Result<Boolean> removeById(@PathVariable("id") Long id){
		return new Result<>(smtSupplierPersonService.delSupplierPerson(id));
	}

	/**
	 * 通过id批量删除保密区供应商人员信息
	 * @param ids
	 * @return Result
	 */
	@ApiOperation("通过id批量删除保密区供应商人员信息")
	@PostMapping("/del/batch")
	public Result<Boolean> removeBatchById(@RequestBody List<String> ids){
		List<Long> longIds = ids.stream().map(Long::parseLong).collect(Collectors.toList());
		return new Result<>(smtSupplierPersonService.removeBatchById(longIds));
	}

	/**
	 * 修改保密区人员信息
	 * @param personUpdateReqDTO
	 * @return Result
	 */
	@ApiOperation("修改保密区人员信息")
	@PostMapping("/update")
	public Result<Boolean> updateById(@RequestBody SecurityAreaPersonUpdateReqDTO personUpdateReqDTO){
		return new Result<>(smtSupplierPersonService.updateById(personUpdateReqDTO));
	}

}
