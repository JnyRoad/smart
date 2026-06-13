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
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.core.wrapper.BaseController;
import com.tce.smart.common.log.annotation.SysLog;
import com.tce.smart.platform.core.entity.SmtSupplierStaff;
import com.tce.smart.platform.core.vo.SearchSupplierStaffVO;
import com.tce.smart.platform.service.SmtSupplierStaffService;

import lombok.AllArgsConstructor;


/**
 * 供应商员工控制器
 *
 * @author QIPEI
 * @date 2020/2/11
 */
@RestController
@AllArgsConstructor
@RequestMapping("/supplier/staff")
public class SmtSupplierStaffController extends BaseController {

  @Autowired
  private final  SmtSupplierStaffService smtSupplierStaffService;


  /**
   * 查询所有的接收人列表
   * @return
   */
  @SysLog("查询供应商员工列表")
  @GetMapping(value = "/page")
  public Result<IPage<SearchSupplierStaffVO>> page(Page page,SmtSupplierStaff smtSupplierStaff) {
		return new Result<>(smtSupplierStaffService.searchPage(page,smtSupplierStaff));
  }



  @SysLog("添加供应商员工列表")
  @PostMapping(value = "/add")
  public Result<Boolean> add( @RequestBody SmtSupplierStaff smtSupplierStaff) {
	  smtSupplierStaff.setCreateTime(LocalDateTime.now());
	  return  new Result<>(smtSupplierStaffService.save(smtSupplierStaff));
  }


  @SysLog("编辑供应商员工列表")
  @PostMapping(value = "/update")
  public Result<Boolean>  update( @RequestBody SmtSupplierStaff SmtSupplierStaff) {
	  return  new Result<>(smtSupplierStaffService.updateById(SmtSupplierStaff));
  }


  @SysLog("删除供应员工列表")
  @GetMapping(value = "/delete/{id}")
  public Result<Boolean>  update( @PathVariable("id") Integer id ) {
	  return  new Result<>(smtSupplierStaffService.removeById(id));
  }


}
