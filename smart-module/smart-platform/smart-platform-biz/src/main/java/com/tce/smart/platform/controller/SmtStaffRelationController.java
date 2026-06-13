package com.tce.smart.platform.controller;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.log.annotation.SysLog;
import com.tce.smart.platform.core.entity.SmtStaffRelation;
import com.tce.smart.platform.service.SmtStaffRelationService;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;


/**
 * 员工人际关系表
 *
 * @author 齐佩
 * @date 2019-04-22 15:25:26
 */
@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping("/staff/relation")
public class SmtStaffRelationController {

  private final  SmtStaffRelationService smtStaffRelationService;

  /**
   * 分页查询
   * @param page 分页对象
   * @param smtStaffRelation 员工人际关系表
   * @return
   */
  @GetMapping("/page")
  public Result getSmtStaffRelationPage(Page page, SmtStaffRelation smtStaffRelation) {
    return  new Result<>(smtStaffRelationService.page(page,Wrappers.query(smtStaffRelation)));
  }


  /**
   * 通过id查询员工人际关系表
   * @param id id
   * @return Result
   */
  @GetMapping("/{id}")
  public Result getById(@PathVariable("id") Integer id){
    return new Result<>(smtStaffRelationService.getById(id));
  }

  /**
   * 新增员工人际关系表
   * @param smtStaffRelation 员工人际关系表
   * @return Result
   */
  @SysLog("新增员工人际关系表 ")
  @PostMapping("addStaffRelation")
  public Result save(@RequestBody SmtStaffRelation smtStaffRelation){
    return new Result<>(smtStaffRelationService.addStaffRelation(smtStaffRelation));
  }

  /**
   * 修改员工人际关系表
   * @param smtStaffRelation 员工人际关系表
   * @return Result
   */
  @SysLog("修改员工人际关系表 ")
  @PostMapping("updateStaffRelation")
  public Result updateById(@RequestBody SmtStaffRelation smtStaffRelation){
    return new Result<>(smtStaffRelationService.updateStaffRelation(smtStaffRelation));
  }

  /**
   * 通过id删除员工人际关系表
   * @param id id
   * @return Result
   */
  @SysLog("删除员工人际关系表 ")
  @PostMapping("/{id}")
  public Result removeById(@PathVariable Integer id){
    return new Result<>(smtStaffRelationService.removeById(id));
  }

  /**
   * 通过工号查询员工人事关系
   * @param staffId
   * @return Result
   */
  @GetMapping("/getByStaffId/{staffId}")
  public Result getByStaffId(@PathVariable String staffId){
    Result result = Result.builder().build();
    try {
      result =  smtStaffRelationService.getByStaffId(staffId);
    } catch (Exception e) {
      result.setMsg("通过员工工号查询人事关系出错");
      log.warn("通过员工工号查询人事关系出错",e);
    }
    return result;
  }

  @SysLog("接口根据员工id删除人事关系表 ")
  @GetMapping("/deleteRelation/{staffId}")
  public Result removeRelationByStaffId(@PathVariable Integer staffId){
    return smtStaffRelationService.removeRelationByStaffId(staffId);
  }

}
