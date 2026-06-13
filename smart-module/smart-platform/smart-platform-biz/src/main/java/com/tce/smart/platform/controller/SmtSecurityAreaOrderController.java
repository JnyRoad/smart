package com.tce.smart.platform.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.log.annotation.SysLog;
import com.tce.smart.platform.api.dto.req.SmtStaffAppealReqDTO;
import com.tce.smart.platform.api.dto.req.securityarea.SmtSecurityAreaOrderReqDTO;
import com.tce.smart.platform.api.dto.resp.SmtSecurityAreaSupplierPersonRespDTO;
import com.tce.smart.platform.api.dto.resp.SmtSecurityAreaSupplierRespDTO;
import com.tce.smart.platform.api.dto.resp.securityarea.SecurityAreaOrderDetailDTO;
import com.tce.smart.platform.api.dto.resp.securityarea.SecurityAreaOrderListDTO;
import com.tce.smart.platform.api.dto.resp.securityarea.SecurityAreaSupplierDTO;
import com.tce.smart.platform.service.SmtSecurityAreaOrderService;
import com.tce.smart.platform.service.SmtSecurityAreaSupplierService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @description: 保密区预约控制器
 * @date: 2020-07-30 9:16
 * @author: wuling
 * @version: 1.0
 */
@Api(tags = "保密区预约")
@RestController
@AllArgsConstructor
@RequestMapping("/securityarea/order")
public class SmtSecurityAreaOrderController {

	private final SmtSecurityAreaOrderService smtSecurityAreaOrderService;

	private final SmtSecurityAreaSupplierService smtSecurityAreaSupplierService;

	/**
	 * 获取用户的保密区预约列表
	 * @return Result
	 */
	@ApiOperation("获取保密区预约列表")
	@GetMapping("/user/record")
	public Result<IPage<SecurityAreaOrderListDTO>> saveStaffAppealRecord(@ApiParam(name = "current",value = "当前页",required = true) @RequestParam long current,
																		 @ApiParam(name = "size",value = "大小",required = true) @RequestParam long size){
		return new Result<>(smtSecurityAreaOrderService.getOrderListByUser(new Page(current,size)));
	}

	/**
	 * 保密区预约
	 * @param smtSecurityAreaOrderReqDTO 保密区预约信息
	 * @return Result
	 */
	@ApiOperation("保密区预约")
	@PostMapping("/save")
	public Result<Boolean> saveStaffAppealRecord(@RequestBody SmtSecurityAreaOrderReqDTO smtSecurityAreaOrderReqDTO){
		return new Result<>(smtSecurityAreaOrderService.saveOrder(smtSecurityAreaOrderReqDTO));
	}

	/**
	 * 获取预约详情
	 * @return Result
	 */
	@ApiOperation("获取预约详情")
	@GetMapping("/detail/{id}")
	public Result<SecurityAreaOrderDetailDTO> saveStaffAppealRecord(@ApiParam(name = "id",value = "记录标识ID",required = true) @PathVariable("id") long id){
		return new Result<>(smtSecurityAreaOrderService.getOrderDetail(id));
	}


	/**
	 * 查询所有可用的供应商信息
	 * @return
	 */
	@ApiOperation("查询供应商信息")
	@GetMapping("/supplier/list/{parkId}")
	public Result<List<SecurityAreaSupplierDTO>> getSecurityAreaSupplierList(@ApiParam(name = "parkId",value = "园区Id",required = true) @PathVariable("parkId") Integer parkId) {
		return new Result<>(smtSecurityAreaSupplierService.getSecurityAreaSupplierList(null,parkId));
	}

	/**
	 * 查询保密区供应商的人员列表
	 * @return
	 */
	@ApiOperation("查询保密区供应商的人员列表")
	@GetMapping("/supplier/person/list/{spId}")
	public Result<List<SmtSecurityAreaSupplierPersonRespDTO>> getSecurityAreaSupplierPersonList(@ApiParam(name = "spId",value = "供应商Id",required = true) @PathVariable("spId") Long spId) {
		return new Result<>(smtSecurityAreaSupplierService.getSecurityAreaSupplierPersonList(spId));
	}
}
