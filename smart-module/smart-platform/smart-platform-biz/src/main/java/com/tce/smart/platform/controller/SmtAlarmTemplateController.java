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
import com.tce.smart.platform.core.dto.AlarmTemplateDTO;
import com.tce.smart.platform.core.entity.SmtAlarmTemplate;
import com.tce.smart.platform.service.SmtAlarmTemplateService;

import lombok.AllArgsConstructor;


/**
 * 警报模板
 *
 * @author 王艳勇
 * @date 2019-04-15 11:34:58
 */
@RestController
@AllArgsConstructor
@RequestMapping("/alarm/template")
public class SmtAlarmTemplateController {

  private final  SmtAlarmTemplateService smtAlarmTemplateService;

  /**
   * 分页查询
   * @param page 分页对象
   * @param smtAlarm 警报信息记录
   * @return
   */
  @GetMapping("/page")
  public Result getSmtAlarmPage(Page page, SmtAlarmTemplate smtAlarmTemplate) {
    return  new Result <>(smtAlarmTemplateService.page(page,Wrappers.query(smtAlarmTemplate)));
  }


  /**
   * 通过id查询警报信息记录
   * @param id id
   * @return Result
   */
  @GetMapping("/{id}")
  public Result getById(@PathVariable("id") String id){
    return new Result <>(smtAlarmTemplateService.getById(id));
  }

  /**
   * 新增警报信息记录
   * @param smtAlarm 警报信息记录
   * @return Result
   */
  @SysLog("新增警报信息记录")
  @PostMapping("/save")
  public Result save(@RequestBody AlarmTemplateDTO smtAlarmTemplate){
    return new Result <>(smtAlarmTemplateService.saveSmtAlarmRecever(smtAlarmTemplate));
  }

  /**
   * 修改警报信息记录
   * @param smtAlarm 警报信息记录
   * @return Result
   */
  @SysLog("修改警报信息记录")
  @PostMapping("/update")
  public Result updateById(@RequestBody SmtAlarmTemplate smtAlarm){
    return new Result <>(smtAlarmTemplateService.updateById(smtAlarm));
  }

  /**
   * 通过id删除警报信息记录
   * @param id id
   * @return Result
   */
  @SysLog("删除警报信息记录")
  @PostMapping("/{id}")
  public Result removeById(@PathVariable String id){
    return new Result <>(smtAlarmTemplateService.removeById(id));
  }


  /**
   * 获取警报模板信息
   * @return Result
   */
  @GetMapping("/getAlarmTemplate")
  public Result getAlarmTemplate(){
    return new Result <>(smtAlarmTemplateService.getAlarmTemplate());
  }
}
