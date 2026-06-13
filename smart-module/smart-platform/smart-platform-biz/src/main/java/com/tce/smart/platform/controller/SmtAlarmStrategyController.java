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
import com.tce.smart.platform.core.entity.SmtAlarmStrategy;
import com.tce.smart.platform.service.SmtAlarmStrategyService;

import lombok.AllArgsConstructor;


/**
 * 警报策略表
 *
 * @author 王艳勇
 * @date 2019-04-15 11:34:31
 */
@RestController
@AllArgsConstructor
@RequestMapping("/smtalarmstrategy")
public class  SmtAlarmStrategyController {

  private final  SmtAlarmStrategyService smtAlarmStrategyService;

  /**
   * 分页查询
   * @param page 分页对象
   * @param smtAlarmStrategy 警报策略表
   * @return
   */
  @GetMapping("/page")
  public Result getSmtAlarmStrategyPage(Page page, SmtAlarmStrategy smtAlarmStrategy) {
    return  new Result <>(smtAlarmStrategyService.page(page,Wrappers.query(smtAlarmStrategy)));
  }


  /**
   * 通过id查询警报策略表
   * @param id id
   * @return Result
   */
  @GetMapping("/{id}")
  public Result getById(@PathVariable("id") Integer id){
    return new Result <>(smtAlarmStrategyService.getById(id));
  }

  /**
   * 新增警报策略表
   * @param smtAlarmStrategy 警报策略表
   * @return Result
   */
  @SysLog("新增警报策略表")
  @PostMapping("/save")
  public Result save(@RequestBody SmtAlarmStrategy smtAlarmStrategy){
    return new Result <>(smtAlarmStrategyService.save(smtAlarmStrategy));
  }

  /**
   * 修改警报策略表
   * @param smtAlarmStrategy 警报策略表
   * @return Result
   */
  @SysLog("修改警报策略表")
  @PostMapping("/update")
  public Result updateById(@RequestBody SmtAlarmStrategy smtAlarmStrategy){
    return new Result <>(smtAlarmStrategyService.updateById(smtAlarmStrategy));
  }

  /**
   * 通过id删除警报策略表
   * @param id id
   * @return Result
   */
  @SysLog("删除警报策略表")
  @PostMapping("/{id}")
  public Result removeById(@PathVariable Integer id){
    return new Result <>(smtAlarmStrategyService.removeById(id));
  }

}
