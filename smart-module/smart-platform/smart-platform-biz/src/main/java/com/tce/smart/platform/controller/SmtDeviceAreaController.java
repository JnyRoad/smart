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
import com.tce.smart.platform.core.entity.SmtDeviceArea;
import com.tce.smart.platform.service.SmtDeviceAreaService;

import lombok.AllArgsConstructor;


/**
 * 设备区域关联
 *
 * @author 王艳勇
 * @date 2019-04-15 15:12:58
 */
@RestController
@AllArgsConstructor
@RequestMapping("/smtdevicearea")
public class SmtDeviceAreaController {

  private final  SmtDeviceAreaService smtDeviceAreaService;

  /**
   * 分页查询
   * @param page 分页对象
   * @param smtDeviceArea 设备区域关联
   * @return
   */
  @GetMapping("/page")
  public Result getSmtDeviceAreaPage(Page page, SmtDeviceArea smtDeviceArea) {
    return  new Result <>(smtDeviceAreaService.page(page,Wrappers.query(smtDeviceArea)));
  }


  /**
   * 通过id查询设备区域关联
   * @param id id
   * @return Result
   */
  @GetMapping("/{id}")
  public Result getById(@PathVariable("id") Integer id){
    return new Result <>(smtDeviceAreaService.getById(id));
  }

  /**
   * 新增设备区域关联
   * @param smtDeviceArea 设备区域关联
   * @return Result
   */
  @SysLog("新增设备区域关联")
  @PostMapping("/save")
  public Result save(@RequestBody SmtDeviceArea smtDeviceArea){
    return new Result <>(smtDeviceAreaService.saveArea(smtDeviceArea));
  }

  /**
   * 修改设备区域关联
   * @param smtDeviceArea 设备区域关联
   * @return Result
   */
  @SysLog("修改设备区域关联")
  @PostMapping("/update")
  public Result updateById(@RequestBody SmtDeviceArea smtDeviceArea){
    return new Result <>(smtDeviceAreaService.updateById(smtDeviceArea));
  }

  /**
   * 通过id删除设备区域关联
   * @param id id
   * @return Result
   */
  @SysLog("删除设备区域关联")
  @PostMapping("/{id}")
  public Result removeById(@PathVariable Integer id){
    return new Result <>(smtDeviceAreaService.removeById(id));
  }

}
