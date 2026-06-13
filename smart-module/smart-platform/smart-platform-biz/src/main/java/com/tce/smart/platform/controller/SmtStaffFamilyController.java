package com.tce.smart.platform.controller;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.log.annotation.SysLog;
import com.tce.smart.platform.core.entity.SmtStaffFamily;
import com.tce.smart.platform.service.SmtStaffFamilyService;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;


/**
 * 员工家庭成员
 *
 * @author 齐佩
 * @date 2019-04-22 15:25:26
 */
@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping("/staff/family")
public class SmtStaffFamilyController {

  private final  SmtStaffFamilyService smtStaffFamilyService;

  /**
   * 分页查询
   * @param page 分页对象
   * @param smtStaffFamily 员工家庭成员表
   * @return
   */
  @GetMapping("/page")
  public Result getSmtStaffFamilyPage(Page page, SmtStaffFamily smtStaffFamily) {
    return  new Result<>(smtStaffFamilyService.page(page,Wrappers.query(smtStaffFamily)));
  }


  /**
   * 通过id查询员工家庭成员表
   * @param id id
   * @return Result
   */
  @GetMapping("/{id}")
  public Result getById(@PathVariable("id") Integer id){
    return new Result<>(smtStaffFamilyService.getById(id));
  }

  /**
   * 新增员工家庭成员表
   * @param smtStaffFamily 员工家庭成员表
   * @return Result
   */
  @SysLog("新增员工家庭成员表 ")
  @PostMapping("/addStaffFamily")
  public Result save(@RequestBody SmtStaffFamily smtStaffFamily){
    return smtStaffFamilyService.addStaffFamily(smtStaffFamily);
  }

  /**
   * 修改员工家庭成员表
   * @param smtStaffFamily 员工家庭成员表
   * @return Result
   */
  @SysLog("修改员工家庭成员表 ")
  @PostMapping("updateStaffFamily")
  public Result updateById(@RequestBody SmtStaffFamily smtStaffFamily){
    return smtStaffFamilyService.updateStaffFamily(smtStaffFamily);
  }

  /**
   * 通过id删除员工家庭成员表
   * @param id id
   * @return Result
   */
  @SysLog("删除员工家庭成员表 ")
  @PostMapping("/{id}")
  public Result removeById(@PathVariable Integer id){
    return new Result<>(smtStaffFamilyService.removeById(id));
  }

  /**
   * 通过工号查询员工家庭成员
   * @param staffId
   * @return Result
   */
  @GetMapping("/getByStaffId/{staffId}")
  public Result<List<SmtStaffFamily>> getByEmployeeId(@PathVariable String staffId){
    Result result = Result.builder().build();
    try {
      result =  smtStaffFamilyService.getByStaffId(staffId);
    } catch (Exception e) {
      result.setMsg("通过员工工号查询家庭成员出错");
      log.warn("通过员工工号查询家庭成员出错",e);
    }
    return result;
  }



  @SysLog("接口根据员工id删除家庭成员表 ")
  @GetMapping("/deleteFamily/{staffId}")
  public Result removeFamilyByStaffId(@PathVariable Long staffId){
    return smtStaffFamilyService.removeFamilyByStaffId(staffId);
  }
}
