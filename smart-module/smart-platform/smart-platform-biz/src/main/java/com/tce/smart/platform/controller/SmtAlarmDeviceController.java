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
import com.tce.smart.platform.core.entity.SmtAlarmDevice;
import com.tce.smart.platform.service.SmtAlarmDeviceService;

import lombok.AllArgsConstructor;


/**
 * 警报设备表
 *
 * @author 王艳勇
 * @date 2019-04-15 11:34:54
 */
@RestController
@AllArgsConstructor
@RequestMapping("/smtalarmdevice")
public class SmtAlarmDeviceController {

  private final  SmtAlarmDeviceService smtAlarmDeviceService;

  /**
   * 分页查询
   * @param page 分页对象
   * @param smtAlarmDevice 警报设备表
   * @return
   */
  @GetMapping("/page")
  public Result getSmtAlarmDevicePage(Page page, SmtAlarmDevice smtAlarmDevice) {
    return  new Result <>(smtAlarmDeviceService.page(page,Wrappers.query(smtAlarmDevice)));
  }


  /**
   * 通过id查询警报设备表
   * @param id id
   * @return Result
   */
  @GetMapping("/{id}")
  public Result getById(@PathVariable("id") Integer id){
    return new Result <>(smtAlarmDeviceService.getById(id));
  }

  /**
   * 新增警报设备表
   * @param smtAlarmDevice 警报设备表
   * @return Result
   */
  @SysLog("新增警报设备表")
  @PostMapping("/save")
  public Result save(@RequestBody SmtAlarmDevice smtAlarmDevice){
    return new Result <>(smtAlarmDeviceService.save(smtAlarmDevice));
  }

  /**
   * 修改警报设备表
   * @param smtAlarmDevice 警报设备表
   * @return Result
   */
  @SysLog("修改警报设备表")
  @PostMapping("/update")
  public Result updateById(@RequestBody SmtAlarmDevice smtAlarmDevice){
    return new Result <>(smtAlarmDeviceService.updateById(smtAlarmDevice));
  }

  /**
   * 通过id删除警报设备表
   * @param id id
   * @return Result
   */
  @SysLog("删除警报设备表")
  @PostMapping("/{id}")
  public Result removeById(@PathVariable Integer id){
    return new Result <>(smtAlarmDeviceService.removeById(id));
  }

}
