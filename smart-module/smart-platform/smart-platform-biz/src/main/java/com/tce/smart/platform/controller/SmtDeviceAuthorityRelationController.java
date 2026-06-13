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
import com.tce.smart.platform.core.entity.SmtDeviceAuthorityRelation;
import com.tce.smart.platform.service.SmtDeviceAuthorityRelationService;

import lombok.AllArgsConstructor;


/**
 * 设备权限关联表
 *
 * @author 王艳勇
 * @date 2019-04-15 15:15:34
 */
@RestController
@AllArgsConstructor
@RequestMapping("/smtdeviceauthorityrelation")
public class SmtDeviceAuthorityRelationController {

  private final  SmtDeviceAuthorityRelationService smtDeviceAuthorityRelationService;

  /**
   * 分页查询
   * @param page 分页对象
   * @param smtDeviceAuthorityRelation 设备权限关联表
   * @return
   */
  @GetMapping("/page")
  public Result getSmtDeviceAuthorityPage(Page page, SmtDeviceAuthorityRelation smtDeviceAuthorityRelation) {
    return  new Result <>(smtDeviceAuthorityRelationService.page(page,Wrappers.query(smtDeviceAuthorityRelation)));
  }


  /**
   * 通过id查询设备权限关联表
   * @param id id
   * @return Result
   */
  @GetMapping("/{id}")
  public Result getById(@PathVariable("id") Integer id){
    return new Result <>(smtDeviceAuthorityRelationService.getById(id));
  }

  /**
   * 新增设备权限关联表
   * @param smtDeviceAuthorityRelation 设备权限关联表
   * @return Result
   */
  @SysLog("新增设备权限关联表")
  @PostMapping("/save")
  public Result save(@RequestBody SmtDeviceAuthorityRelation smtDeviceAuthorityRelation){
    return new Result <>(smtDeviceAuthorityRelationService.save(smtDeviceAuthorityRelation));
  }

  /**
   * 修改设备权限表
   * @param smtDeviceAuthorityRelation 设备权限表
   * @return Result
   */
  @SysLog("修改设备权限关联表")
  @PostMapping("/update")
  public Result updateById(@RequestBody SmtDeviceAuthorityRelation smtDeviceAuthorityRelation){
    return new Result <>(smtDeviceAuthorityRelationService.updateById(smtDeviceAuthorityRelation));
  }

  /**
   * 通过id删除设备权限表
   * @param id id
   * @return Result
   */
  @SysLog("删除设备权限关联表")
  @PostMapping("/{id}")
  public Result removeById(@PathVariable Integer id){
    return new Result <>(smtDeviceAuthorityRelationService.removeById(id));
  }

}
