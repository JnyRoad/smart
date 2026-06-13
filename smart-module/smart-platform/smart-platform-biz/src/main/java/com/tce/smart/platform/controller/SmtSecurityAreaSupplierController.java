package com.tce.smart.platform.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.core.util.BeanUtils;
import com.tce.smart.common.security.annotation.Inner;
import com.tce.smart.platform.api.dto.req.SmtSecurityAreaSupplierReqDTO;
import com.tce.smart.platform.api.dto.req.badge.QueryLossInfoReqDTO;
import com.tce.smart.platform.api.dto.req.securityarea.*;
import com.tce.smart.platform.api.dto.resp.SmtSecurityAreaSupplierRespDTO;
import com.tce.smart.platform.api.dto.resp.securityarea.SecurityAreaSupplierDTO;
import com.tce.smart.platform.api.dto.resp.securityarea.SecurityAreaSupplierDetailDTO;
import com.tce.smart.platform.api.dto.resp.securityarea.SecurityAreaSupplierListDTO;
import com.tce.smart.platform.core.entity.badge.SmtBadgeLoss;
import com.tce.smart.platform.service.SmtSecurityAreaSupplierService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @description: SmtSecurityAreaSupplierController
 * @date: 2020-07-21 9:20
 * @author: wuling
 * @version: 1.0
 */
@Api(tags = "保密区供应商管理")
@RestController
@AllArgsConstructor
@RequestMapping("/securityarea/supplier")
public class SmtSecurityAreaSupplierController {

	private final SmtSecurityAreaSupplierService smtSecurityAreaSupplierService;

//	/**
//	 * 分页查询
//	 * @param page 分页对象
//	 * @param smtSecurityAreaSupplierReqDTO 保密区供应商
//	 * @return
//	 */
//	@ApiOperation("分页查询")
//	@GetMapping("/page")
//	public Result getSecurityAreaSupplierPage(Page page, SmtSecurityAreaSupplierReqDTO smtSecurityAreaSupplierReqDTO) {
//		return new Result<>(smtSecurityAreaSupplierService.getSecurityAreaSupplierPage(page, smtSecurityAreaSupplierReqDTO));
//	}

	/**
	 * 查询所有供应商信息
	 * @return
	 */
	@ApiOperation("查询所有供应商信息")
	@GetMapping("/list")
	public Result<List<SecurityAreaSupplierListDTO>> getSecurityAreaSupplierList(@ApiParam(name = "supplierName",value = "供应商名称",required = false)@RequestParam(value = "supplierName",required = false) String supplierName) {
		return new Result<>(smtSecurityAreaSupplierService.getSecurityAreaSupplier(supplierName,null));
	}

	/**
	 * 供应商信息导出
	 * @param
	 * @return
	 */
	@ApiOperation("供应商信息导出")
	@GetMapping("/excel")
	public ResponseEntity<byte[]> excel(@RequestParam Integer parkId) {
		return smtSecurityAreaSupplierService.downLoadExcel(parkId);
	}

	/**
	 * 保存保密区供应商记录信息
	 * @param supplierAddReqDTO 保密区供应商信息
	 * @return Result
	 */
	@ApiOperation("添加保密区供应商")
	@PostMapping("/save")
	public Result<Boolean> saveSecurityAreaSupplier(@RequestBody SecurityAreaSupplierAddReqDTO supplierAddReqDTO){
		return new Result<>(smtSecurityAreaSupplierService.saveSecurityAreaSupplier(supplierAddReqDTO));
	}

	/**
	 * 批量导入保密区供应商
	 * @param supplierUploadReqDTO 保密区供应商信息
	 * @return Result
	 */
	@ApiOperation("批量导入保密区供应商")
	@PostMapping("/upload")
	public Result<Boolean> uploadSecurityAreaSupplier(@RequestBody SecurityAreaSupplierUploadReqDTO supplierUploadReqDTO){
		return new Result<>(smtSecurityAreaSupplierService.uploadSecurityAreaSupplier(supplierUploadReqDTO));
	}

	/**
	 * 通过id查询保密区供应商详情
	 * @param id id
	 * @return Result
	 */
	@ApiOperation("通过id查询保密区供应商详情")
	@GetMapping("/query/{id}")
	public Result<SecurityAreaSupplierDetailDTO> getDetailById(@PathVariable("id") String id){
		return new Result<>(smtSecurityAreaSupplierService.getDetailById(Long.parseLong(id)));
	}

	/**
	 * 通过id删除保密区供应商
	 * @param id id
	 * @return Result
	 */
	@ApiOperation("通过id删除保密区供应商")
	@PostMapping("/del/{id}")
	public Result<Boolean> removeById(@PathVariable("id") String id){
		return new Result<>(smtSecurityAreaSupplierService.delSecurityAreaSupplier(Long.parseLong(id)));
	}

	/**
	 * 通过id批量删除供应商
	 * @param ids
	 * @return Result
	 */
	@ApiOperation("批量删除保密区供应商")
	@PostMapping("/del/batch")
	public Result<Boolean> removeBatchIds(@RequestBody List<String> ids){
		List<Long> longIds = ids.stream().map(Long::parseLong).collect(Collectors.toList());
		return new Result<>(smtSecurityAreaSupplierService.delBatchSupplier(longIds));
	}

	/**
	 * 批量设置供应商授权项目
	 * @param authorReqDTO
	 * @return Result
	 */
	@ApiOperation("批量设置供应商授权项目")
	@PostMapping("/batch/author")
	public Result<Boolean> batchSetAuthor(@RequestBody SecuritySupplierAddAuthorReqDTO authorReqDTO){
		return new Result<>(smtSecurityAreaSupplierService.batchSetAuthor(authorReqDTO));
	}

	/**
	 * 更新通知状态
	 * @param statusReqDTO
	 * @return Result
	 */
	@Inner
	@ApiOperation("更新通知状态")
	@PostMapping("/update/notify")
	public Result<Boolean> updateNotifyStatus(@RequestBody SupplierNotifyStatusReqDTO statusReqDTO){
		return new Result<>(smtSecurityAreaSupplierService.updateNotifyStatus(statusReqDTO));
	}

	/**
	 * 查询需要通知的供应商列表
	 * @param notifyListDTO
	 * @return Result
	 */
	@Inner
	@ApiOperation("查询需要通知的供应商列表")
	@PostMapping("/notify/list")
	public Result<List<SmtSecurityAreaSupplierRespDTO>> notifyList(@RequestBody SecurityAreaNotifyListDTO notifyListDTO){
		return new Result<>(smtSecurityAreaSupplierService.notifyList(notifyListDTO));
	}

	/**
	 * 保存保密区通知配置
	 * @param configReqDTO
	 * @return Result
	 */
	@ApiOperation("保存保密区通知配置")
	@PostMapping("/notify/config")
	public Result<Boolean> saveNotifyConfig(@RequestBody SecurityAreaNotifyConfigDTO configReqDTO){
		return new Result<>(smtSecurityAreaSupplierService.notifyConfig(configReqDTO));
	}

	/**
	 * 查询保密区通知配置
	 * @param parkId
	 * @return Result
	 */
	@ApiOperation("查询保密区通知配置")
	@GetMapping("/notify/config/{parkId}")
	public Result<SecurityAreaNotifyConfigDTO> getNotifyConfig(@ApiParam(name = "parkId",value = "园区Id",required = true)@PathVariable Integer parkId){
		return new Result<>(smtSecurityAreaSupplierService.getNotifyConfig(parkId));
	}

	/**
	 * 查询所有保密区通知配置
	 * @return Result
	 */
	@Inner
	@ApiOperation("查询所有保密区通知配置")
	@GetMapping("/notify/all/config")
	public Result<List<SecurityAreaNotifyConfigDTO>> getNotifyAllConfig(){
		return new Result<>(smtSecurityAreaSupplierService.getNotifyAllConfig());
	}
}
