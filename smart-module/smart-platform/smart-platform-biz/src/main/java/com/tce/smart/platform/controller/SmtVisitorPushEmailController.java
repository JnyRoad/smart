package com.tce.smart.platform.controller;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.core.wrapper.BaseController;
import com.tce.smart.common.log.annotation.SysLog;
import com.tce.smart.platform.core.dto.VisitorPushEamilDTO;
import com.tce.smart.platform.core.entity.SmtVisitorPushEamil;
import com.tce.smart.platform.service.SmtVisitorPushEamilService;

import lombok.AllArgsConstructor;


/**
 * 访客信息推送接收email
 *
 * @author QIPEI
 * @date 2019-09-25 14:25:25
 */
@RestController
@AllArgsConstructor
@RequestMapping("/visitor/push/email")
public class SmtVisitorPushEmailController extends BaseController {

  @Autowired
  private final  SmtVisitorPushEamilService smtVisitorPushEamilService;


  /**
   * 查询所有的接收人列表
   * @return
   */
  @SysLog("查询email接收列表")
  @GetMapping(value = "/searchAll")
  public Result searchAll(SmtVisitorPushEamil smtVisitorPushEamil) {
	  return  new Result<>(smtVisitorPushEamilService.searchAll(smtVisitorPushEamil));
  }

  @SysLog("查询email接收列表")
  @GetMapping(value = "/searchEmail")
  public Result searchEmail() {
	  return  new Result<>(smtVisitorPushEamilService.list());
  }


  @SysLog("添加email接收列表")
  @PostMapping(value = "/add")
  public Result add( @RequestBody VisitorPushEamilDTO emails) {
	  return  new Result<>(smtVisitorPushEamilService.add(emails));
  }


  @SysLog("编辑email接收列表")
  @PostMapping(value = "/update")
  public Result update( @RequestBody VisitorPushEamilDTO emails) {
	  return  new Result<>(smtVisitorPushEamilService.update(emails));
  }



}
