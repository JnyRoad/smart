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
import com.tce.smart.platform.core.entity.SmtAlarmPerson;
import com.tce.smart.platform.service.SmtAlarmPersonService;

import lombok.AllArgsConstructor;


/**
 * 警报人员关联表
 *
 * @author 王艳勇
 * @date 2019-04-15 11:34:49
 */
@RestController
@AllArgsConstructor
@RequestMapping("/smtalarmperson")
public class SmtAlarmPersonController {

  private final  SmtAlarmPersonService smtAlarmPersonService;

  /**
   * 分页查询
   * @param page 分页对象
   * @param smtAlarmPerson 警报人员关联表
   * @return
   */
  @GetMapping("/page")
  public Result getSmtAlarmPersonPage(Page page, SmtAlarmPerson smtAlarmPerson) {
    return  new Result <>(smtAlarmPersonService.page(page,Wrappers.query(smtAlarmPerson)));
  }


  /**
   * 通过id查询警报人员关联表
   * @param id id
   * @return Result
   */
  @GetMapping("/{id}")
  public Result getById(@PathVariable("id") Integer id){
    return new Result <>(smtAlarmPersonService.getById(id));
  }

  /**
   * 新增警报人员关联表
   * @param smtAlarmPerson 警报人员关联表
   * @return Result
   */
  @SysLog("新增警报人员关联表")
  @PostMapping("/save")
  public Result save(@RequestBody SmtAlarmPerson smtAlarmPerson){
    return new Result <>(smtAlarmPersonService.save(smtAlarmPerson));
  }

  /**
   * 修改警报人员关联表
   * @param smtAlarmPerson 警报人员关联表
   * @return Result
   */
  @SysLog("修改警报人员关联表")
  @PostMapping("/update")
  public Result updateById(@RequestBody SmtAlarmPerson smtAlarmPerson){
    return new Result <>(smtAlarmPersonService.updateById(smtAlarmPerson));
  }

  /**
   * 通过id删除警报人员关联表
   * @param id id
   * @return Result
   */
  @SysLog("删除警报人员关联表")
  @PostMapping("/{id}")
  public Result removeById(@PathVariable Integer id){
    return new Result <>(smtAlarmPersonService.removeById(id));
  }

}
