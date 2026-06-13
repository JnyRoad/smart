package com.tce.smart.platform.controller;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.log.annotation.SysLog;
import com.tce.smart.platform.core.entity.SmtApplicationRelation;
import com.tce.smart.platform.service.SmtApplicationRelationService;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;


/**
 * 应聘者人际关系表
 *
 * @author 齐佩
 * @date 2019-04-22 15:25:26
 */
@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping("/application/relation")
public class 	SmtApplicationRelationController {

  private final  SmtApplicationRelationService smtApplicationRelationService;

  /**
   * 分页查询
   * @param page 分页对象
   * @param smtApplicationRelation 应聘者人际关系表
   * @return
   */
  @GetMapping("/page")
  public Result getSmtApplicationRelationPage(Page page, SmtApplicationRelation smtApplicationRelation) {
    return  new Result<>(smtApplicationRelationService.page(page,Wrappers.query(smtApplicationRelation)));
  }


  /**
   * 通过id查询应聘者人际关系表
   * @param id id
   * @return Result
   */
  @GetMapping("/{id}")
  public Result getById(@PathVariable("id") Integer id){
    return new Result<>(smtApplicationRelationService.getById(id));
  }

  /**
   * 新增应聘者人际关系表
   * @param smtApplicationRelation 应聘者人际关系表
   * @return Result
   */
  @SysLog("新增应聘者人际关系表 ")
  @PostMapping("addApplicationRelation")
  public Result save(@RequestBody SmtApplicationRelation smtApplicationRelation){
    return new Result<>(smtApplicationRelationService.addApplicationRelation(smtApplicationRelation));
  }

  /**
   * 修改应聘者人际关系表
   * @param smtApplicationRelation 应聘者人际关系表
   * @return Result
   */
  @SysLog("修改应聘者人际关系表 ")
  @PostMapping("updateApplicationRelation")
  public Result updateById(@RequestBody SmtApplicationRelation smtApplicationRelation){
    return new Result<>(smtApplicationRelationService.updateApplicationRelation(smtApplicationRelation));
  }

  /**
   * 通过id删除应聘者人际关系表
   * @param id id
   * @return Result
   */
  @SysLog("删除应聘者人际关系表 ")
  @PostMapping("/{id}")
  public Result removeById(@PathVariable Integer id){
    return new Result<>(smtApplicationRelationService.removeById(id));
  }

  /**
   * 通过工号查询应聘者人事关系
   * @param applicationId
   * @return Result
   */
  @GetMapping("/getByApplicationId/{applicationId}")
  public Result getByApplicationId(@PathVariable String applicationId){
    Result result = Result.builder().build();
    try {
      result =  smtApplicationRelationService.getByApplicationId(applicationId);
    } catch (Exception e) {
      result.setMsg("通过应聘者工号查询人事关系出错");
      log.warn("通过应聘者工号查询人事关系出错",e);
    }
    return result;
  }


  @GetMapping("/getApplicationInfo/{applicationId}")
  public Result getApplicationInfo(@PathVariable String applicationId){
    return new Result<>( smtApplicationRelationService.getApplicationInfo(applicationId));

  }

  @SysLog("接口根据应聘者id删除人事关系表 ")
  @GetMapping("/deleteRelation/{applicationId}")
  public Result removeRelationByApplicationId(@PathVariable Long applicationId){
    return smtApplicationRelationService.removeRelationByApplicationId(applicationId);
  }

}
