package com.tce.smart.platform.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.log.annotation.SysLog;
import com.tce.smart.platform.core.entity.SmtApplicationEmail;
import com.tce.smart.platform.service.SmtApplicationEmailService;

import lombok.AllArgsConstructor;


/**
 * 应聘者邮箱信息
 *
 * @author 齐佩
 * @date 2019-04-22 15:25:10
 */
@RestController
@AllArgsConstructor
@RequestMapping("/application/email")
public class SmtApplicationEmailController {

  private final  SmtApplicationEmailService smtApplicationEmailService;


  @SysLog("公众号接口获取邮箱")
  @GetMapping("/list/{applicationId}")
  public Result<SmtApplicationEmail> getSmtApplicationEmailList(@PathVariable("applicationId") String applicationId ) {
    return smtApplicationEmailService.getSmtApplicationEmailList(applicationId);
  }

  @SysLog("公众号接口添加邮箱")
  @PostMapping("/add")
  public Result addApplicationEmailList(@RequestBody SmtApplicationEmail email ) {
    return smtApplicationEmailService.addApplicationEmailList(email);
  }

  @SysLog("公众号接口修改邮箱")
  @PostMapping("/update")
  public Result updateApplicationEmailList(@RequestBody SmtApplicationEmail email ) {
    return smtApplicationEmailService.updateApplicationEmailList(email);
  }


  @SysLog("公众号接口删除邮箱")
  @GetMapping("/delete/{applicationId}")
  public Result deleteApplicationEmailList(@PathVariable("applicationId") String applicationId ) {
    return smtApplicationEmailService.deleteApplicationEmailList(applicationId);
  }



}
