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
import com.tce.smart.platform.core.entity.SmtAttendanceException;
import com.tce.smart.platform.service.SmtAttendanceExceptionService;

import lombok.AllArgsConstructor;


/**
 * 职工考勤异常申请表

 *
 * @author 梁圆
 * @date 2019-04-13 18:20:05
 */
@RestController
@AllArgsConstructor
@RequestMapping("/smtattendanceexception")
public class SmtAttendanceExceptionController {

  private final  SmtAttendanceExceptionService smtAttendanceExceptionService;

  /**
   * 分页查询
   * @param page 分页对象
   * @param smtAttendanceException 职工考勤异常申请表

   * @return
   */
  @GetMapping("/page")
  public Result getSmtAttendanceExceptionPage(Page page, SmtAttendanceException smtAttendanceException) {
    return  new Result<>(smtAttendanceExceptionService.page(page,Wrappers.query(smtAttendanceException)));
  }


  /**
   * 通过id查询职工考勤异常申请表

   * @param id id
   * @return Result
   */
  @GetMapping("/{id}")
  public Result getById(@PathVariable("id") Integer id){
    return new Result<>(smtAttendanceExceptionService.getById(id));
  }

  /**
   * 新增职工考勤异常申请表

   * @param smtAttendanceException 职工考勤异常申请表

   * @return Result
   */
  @SysLog("新增职工考勤异常申请表")
  @PostMapping("/save")
  public Result save(@RequestBody SmtAttendanceException smtAttendanceException){
    return new Result<>(smtAttendanceExceptionService.save(smtAttendanceException));
  }

  /**
   * 修改职工考勤异常申请表

   * @param smtAttendanceException 职工考勤异常申请表

   * @return Result
   */
  @SysLog("修改职工考勤异常申请表")
  @PostMapping("/update")
  public Result updateById(@RequestBody SmtAttendanceException smtAttendanceException){
    return new Result<>(smtAttendanceExceptionService.updateById(smtAttendanceException));
  }

  /**
   * 通过id删除职工考勤异常申请表

   * @param id id
   * @return Result
   */
  @SysLog("删除职工考勤异常申请表")
  @PostMapping("/{id}")
  public Result removeById(@PathVariable Integer id){
    return new Result<>(smtAttendanceExceptionService.removeById(id));
  }

}
