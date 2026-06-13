package com.tce.smart.platform.controller;


import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.core.wrapper.BaseController;
import com.tce.smart.common.log.annotation.SysLog;
import com.tce.smart.platform.core.entity.SmtSupplier;
import com.tce.smart.platform.service.SmtSupplierService;
import lombok.AllArgsConstructor;


/**
 * 供应商控制器
 *
 * @author QIPEI
 * @date 2020/2/11
 */
@RestController
@AllArgsConstructor
@RequestMapping("/supplier")
public class SmtSupplierController extends BaseController {

  @Autowired
  private final  SmtSupplierService smtSupplierService;



  @SysLog("查询供应商列表")
  @GetMapping(value = "/page")
  public Result<IPage<SmtSupplier>> page(Page page,SmtSupplier smtSupplier) {
		return new Result<>(smtSupplierService.searchPage(page,smtSupplier));
  }



  @SysLog("添加供应商列表")
  @PostMapping(value = "/add")
  public Result<Boolean> add( @RequestBody SmtSupplier smtSupplie) {
	  smtSupplie.setCreateTime(LocalDateTime.now());
	  return  new Result<>(smtSupplierService.save(smtSupplie));
  }


  @SysLog("编辑供应商列表")
  @PostMapping(value = "/update")
  public Result<Boolean> update( @RequestBody SmtSupplier smtSupplie) {
	  return  new Result<>(smtSupplierService.updateById(smtSupplie));
  }


  @SysLog("删除供应商列表")
  @GetMapping(value = "/delete/{id}")
  public Result<Boolean> update( @PathVariable("id") Integer id ) {
	  return  new Result<>(smtSupplierService.removeById(id));
  }

  @SysLog("根据园区id查询所有供应商")
  @GetMapping(value = "/getList/{parkId}")
  public Result getList( @PathVariable("parkId") Integer parkId ) {
	  return  new Result<>(smtSupplierService.list(Wrappers.<SmtSupplier> query().lambda()
				.eq(SmtSupplier::getParkId, parkId)));
  }



}
