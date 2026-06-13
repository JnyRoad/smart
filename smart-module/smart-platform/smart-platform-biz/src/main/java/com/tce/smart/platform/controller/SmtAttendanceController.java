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
import com.tce.smart.platform.core.entity.SmtAttendance;
import com.tce.smart.platform.service.SmtAttendanceService;

import lombok.AllArgsConstructor;


/**
 * 职工考勤申请表

 *
 * @author 梁圆
 * @date 2019-04-13 18:20:17
 */
@RestController
@AllArgsConstructor
@RequestMapping("/smtattendance")
public class SmtAttendanceController {

  private final  SmtAttendanceService smtAttendanceService;

  /**
   * 分页查询
   * @param page 分页对象
   * @param smtAttendance 职工考勤申请表

   * @return
   */
  @GetMapping("/page")
  public Result getSmtAttendancePage(Page page, SmtAttendance smtAttendance) {
    return  new Result<>(smtAttendanceService.page(page,Wrappers.query(smtAttendance)));
  }


  /**
   * 通过id查询职工考勤申请表

   * @param id id
   * @return Result
   */
  @GetMapping("/{id}")
  public Result getById(@PathVariable("id") Integer id){
    return new Result<>(smtAttendanceService.getById(id));
  }

  /**
   * 新增职工考勤申请表

   * @param smtAttendance 职工考勤申请表

   * @return Result
   */
  @SysLog("新增职工考勤申请表")
  @PostMapping("/save")
  public Result save(@RequestBody SmtAttendance smtAttendance){
    return new Result<>(smtAttendanceService.save(smtAttendance));
  }

  /**
   * 修改职工考勤申请表

   * @param smtAttendance 职工考勤申请表

   * @return Result
   */
  @SysLog("修改职工考勤申请表")
  @PostMapping("/update")
  public Result updateById(@RequestBody SmtAttendance smtAttendance){
    return new Result<>(smtAttendanceService.updateById(smtAttendance));
  }

  /**
   * 通过id删除职工考勤申请表

   * @param id id
   * @return Result
   */
  @SysLog("删除职工考勤申请表")
  @PostMapping("/{id}")
  public Result removeById(@PathVariable Integer id){
    return new Result<>(smtAttendanceService.removeById(id));
  }

}
