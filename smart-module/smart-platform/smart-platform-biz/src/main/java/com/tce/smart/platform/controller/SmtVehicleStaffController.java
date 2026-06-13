package com.tce.smart.platform.controller;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.log.annotation.SysLog;
import com.tce.smart.platform.core.entity.SmtVehicleStaff;
import com.tce.smart.platform.service.SmtVehicleStaffService;

import lombok.AllArgsConstructor;


/**
 * 车辆员工关联表
 *
 * @author 王艳勇
 * @date 2019-04-15 11:33:13
 */
@RestController
@AllArgsConstructor
@RequestMapping("/vehicle_staff")
public class SmtVehicleStaffController {

  private final  SmtVehicleStaffService smtVehicleStaffService;

  /**
   * 分页查询
   * @param page 分页对象
   * @param smtVehicleStaff 车辆员工关联表
   * @return
   */
  @GetMapping("/page")
  public Result getSmtVehicleStaffPage(Page page, SmtVehicleStaff smtVehicleStaff) {
    return  new Result <>(smtVehicleStaffService.page(page,Wrappers.query(smtVehicleStaff)));
  }


  /**
   * 通过id查询车辆员工关联表
   * @param id id
   * @return Result
   */
  @GetMapping("/{id}")
  public Result getById(@PathVariable("id") Integer id){
    return new Result <>(smtVehicleStaffService.getById(id));
  }

  /**
   * 新增车辆员工关联表
   * @param smtVehicleStaff 车辆员工关联表
   * @return Result
   */
  @SysLog("新增车辆员工关联表")
  @PostMapping("/save")
  public Result save(@RequestBody SmtVehicleStaff smtVehicleStaff){
    return new Result <>(smtVehicleStaffService.save(smtVehicleStaff));
  }

  /**
   * 修改车辆员工关联表
   * @param smtVehicleStaff 车辆员工关联表
   * @return Result
   */
  @SysLog("修改车辆员工关联表")
  @PostMapping("/update")
  public Result updateById(@RequestBody SmtVehicleStaff smtVehicleStaff){
    return new Result <>(smtVehicleStaffService.updateById(smtVehicleStaff));
  }

  /**
   * 通过id删除车辆员工关联表
   * @param id id
   * @return Result
   */
  @SysLog("删除车辆员工关联表")
  @PostMapping("/{id}")
  public Result removeById(@PathVariable Integer id){
    return new Result <>(smtVehicleStaffService.removeById(id));
  }

}
