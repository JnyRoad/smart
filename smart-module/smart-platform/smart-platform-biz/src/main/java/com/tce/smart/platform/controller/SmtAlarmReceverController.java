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
import com.tce.smart.platform.core.entity.SmtAlarmRecever;
import com.tce.smart.platform.service.SmtAlarmReceverService;

import lombok.AllArgsConstructor;


/**
 * 警报推送人信息表
 *
 * @author 王艳勇
 * @date 2019-04-15 11:34:43
 */
@RestController
@AllArgsConstructor
@RequestMapping("/smtalarmrecever")
public class SmtAlarmReceverController {

  private final  SmtAlarmReceverService smtAlarmReceverService;

  /**
   * 分页查询
   * @param page 分页对象
   * @param smtAlarmRecever 警报推送人信息表
   * @return
   */
  @GetMapping("/page")
  public Result getSmtAlarmReceverPage(Page page, SmtAlarmRecever smtAlarmRecever) {
    return  new Result <>(smtAlarmReceverService.page(page,Wrappers.query(smtAlarmRecever)));
  }


  /**
   * 通过id查询警报推送人信息表
   * @param id id
   * @return Result
   */
  @GetMapping("/{id}")
  public Result getById(@PathVariable("id") Integer id){
    return new Result <>(smtAlarmReceverService.getById(id));
  }

  /**
   * 新增警报推送人信息表
   * @param smtAlarmRecever 警报推送人信息表
   * @return Result
   */
  @SysLog("新增警报推送人信息表")
  @PostMapping("/save")
  public Result save(@RequestBody SmtAlarmRecever smtAlarmRecever){
    return new Result <>(smtAlarmReceverService.save(smtAlarmRecever));
  }

  /**
   * 修改警报推送人信息表
   * @param smtAlarmRecever 警报推送人信息表
   * @return Result
   */
  @SysLog("修改警报推送人信息表")
  @PostMapping("/update")
  public Result updateById(@RequestBody SmtAlarmRecever smtAlarmRecever){
    return new Result <>(smtAlarmReceverService.updateById(smtAlarmRecever));
  }

  /**
   * 通过id删除警报推送人信息表
   * @param id id
   * @return Result
   */
  @SysLog("删除警报推送人信息表")
  @PostMapping("/{id}")
  public Result removeById(@PathVariable Integer id){
    return new Result <>(smtAlarmReceverService.removeById(id));
  }

}
