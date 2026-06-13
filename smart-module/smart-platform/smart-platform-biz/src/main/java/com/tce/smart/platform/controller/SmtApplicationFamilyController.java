package com.tce.smart.platform.controller;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.log.annotation.SysLog;
import com.tce.smart.platform.core.entity.SmtApplicationFamily;
import com.tce.smart.platform.service.SmtApplicationFamilyService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import static com.tce.smart.common.core.model.Result.success;


/**
 * 应聘者家庭成员
 *
 * @author 齐佩
 * @date 2019-04-22 15:25:26
 */
@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping("/application/family")
public class SmtApplicationFamilyController {

  private final  SmtApplicationFamilyService smtApplicationFamilyService;

  /**
   * 分页查询
   * @param page 分页对象
   * @param smtApplicationFamily 应聘者家庭成员表
   * @return
   */
  @GetMapping("/page")
  public Result getSmtApplicationFamilyPage(Page page, SmtApplicationFamily smtApplicationFamily) {
    return  new Result<>(smtApplicationFamilyService.page(page,Wrappers.query(smtApplicationFamily)));
  }


  /**
   * 通过id查询应聘者家庭成员表
   * @param id id
   * @return Result
   */
  @GetMapping("/{id}")
  public Result getById(@PathVariable("id") Integer id){
    return new Result<>(smtApplicationFamilyService.getById(id));
  }

  /**
   * 新增应聘者家庭成员表
   * @param smtApplicationFamily 应聘者家庭成员表
   * @return Result
   */
  @SysLog("新增应聘者家庭成员表 ")
  @PostMapping("/addApplicationFamily")
  public Result<Boolean> save(@RequestBody SmtApplicationFamily smtApplicationFamily){
    return smtApplicationFamilyService.addApplicationFamily(smtApplicationFamily);
  }

  /**
   * 修改应聘者家庭成员表
   * @param smtApplicationFamily 应聘者家庭成员表
   * @return Result
   */
  @SysLog("修改应聘者家庭成员表 ")
  @PostMapping("updateApplicationFamily")
  public Result updateById(@RequestBody SmtApplicationFamily smtApplicationFamily){
    return smtApplicationFamilyService.updateApplicationFamily(smtApplicationFamily);
  }

  /**
   * 通过id删除应聘者家庭成员表
   * @param id id
   * @return Result
   */
  @SysLog("删除应聘者家庭成员表 ")
  @PostMapping("/{id}")
  public Result removeById(@PathVariable Integer id){
    return new Result<>(smtApplicationFamilyService.removeById(id));
  }

  /**
   * 通过工号查询应聘者家庭成员
   * @param applicationId
   * @return Result
   */
  @GetMapping("/getByApplicationId/{applicationId}")
  public Result getByEmployeeId(@PathVariable String applicationId){
    return success(smtApplicationFamilyService.getByApplicationId(applicationId));
  }



  @SysLog("接口根据应聘者id删除家庭成员表 ")
  @GetMapping("/deleteFamily/{applicationId}")
  public Result removeFamilyByApplicationId(@PathVariable Long applicationId){
    return smtApplicationFamilyService.removeFamilyByApplicationId(applicationId);
  }




}
