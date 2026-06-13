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
import com.tce.smart.platform.core.entity.SmtPerson;
import com.tce.smart.platform.service.SmtPersonService;

import lombok.AllArgsConstructor;


/**
 * 警报人员表
 *
 * @author 王艳勇
 * @date 2019-04-15 14:43:28
 */
@RestController
@AllArgsConstructor
@RequestMapping("/smtperson")
public class SmtPersonController {

  private final  SmtPersonService smtPersonService;

  /**
   * 分页查询
   * @param page 分页对象
   * @param smtPerson 警报人员表
   * @return
   */
  @GetMapping("/page")
  public Result getSmtPersonPage(Page page, SmtPerson smtPerson) {
    return  new Result <>(smtPersonService.page(page,Wrappers.query(smtPerson)));
  }


  /**
   * 通过id查询警报人员表
   * @param id id
   * @return Result
   */
  @GetMapping("/{id}")
  public Result getById(@PathVariable("id") Long id){
    return new Result <>(smtPersonService.getById(id));
  }

  /**
   * 新增警报人员表
   * @param smtPerson 警报人员表
   * @return Result
   */
  @SysLog("新增警报人员表")
  @PostMapping("/save")
  public Result save(@RequestBody SmtPerson smtPerson){
    return new Result <>(smtPersonService.save(smtPerson));
  }

  /**
   * 修改警报人员表
   * @param smtPerson 警报人员表
   * @return Result
   */
  @SysLog("修改警报人员表")
  @PostMapping("/update")
  public Result updateById(@RequestBody SmtPerson smtPerson){
    return new Result <>(smtPersonService.updateById(smtPerson));
  }

  /**
   * 通过id删除警报人员表
   * @param id id
   * @return Result
   */
  @SysLog("删除警报人员表")
  @PostMapping("/{id}")
  public Result removeById(@PathVariable Long id){
    return new Result <>(smtPersonService.removeById(id));
  }

}
