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
import com.tce.smart.platform.core.entity.SmtDownRecord;
import com.tce.smart.platform.service.SmtDownRecordService;

import lombok.AllArgsConstructor;


/**
 * 下发记录表
 *
 * @author ly
 * @date 2019-04-15 11:34:54
 */
@RestController
@AllArgsConstructor
@RequestMapping("/smsDownRecord")
public class SmtDownRecordController {

  private final  SmtDownRecordService smtDownRecordService;

  @GetMapping("/page")
  public Result getSmtFellowVisitorPage(Page page, SmtDownRecord smtDownRecord) {
    return  new Result<>(smtDownRecordService.page(page,Wrappers.query(smtDownRecord)));
  }



  @GetMapping("/{id}")
  public Result getById(@PathVariable("id") Long id){
    return new Result<>(smtDownRecordService.getById(id));
  }


  @PostMapping("/save")
  public Result save(@RequestBody SmtDownRecord smtDownRecord){
    return new Result<>(smtDownRecordService.save(smtDownRecord));
  }


  @PostMapping("/update")
  public Result updateById(@RequestBody SmtDownRecord smtDownRecord){
    return new Result<>(smtDownRecordService.updateById(smtDownRecord));
  }


  @PostMapping("/{id}")
  public Result removeById(@PathVariable Long id){
    return new Result<>(smtDownRecordService.removeById(id));
  }

}
