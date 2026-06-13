package com.tce.smart.platform.controller.dormitoryconfig;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tce.smart.common.log.annotation.SysLog;
import com.tce.smart.platform.core.entity.dormitoryconfig.SmtDormitoryPerson;
import com.tce.smart.platform.service.dormitoryconfig.SmtDormitoryPersonService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.core.wrapper.BaseController;

import java.math.BigDecimal;

/**
 *
 *
 * @author fushiping
 * @date 2021-09-14 20:14:59
 */
@RestController
@AllArgsConstructor
@RequestMapping("/dormitory/person")
public class SmtDormitoryPersonController extends BaseController {

  private final SmtDormitoryPersonService smtDormitoryPersonService;

  /**
   * 分页查询
   * @param page 分页对象
   * @param smtDormitoryPerson
   * @return
   */
  @GetMapping("/page")
  public Result getSmtDormitoryPersonPage(Page page, SmtDormitoryPerson smtDormitoryPerson) {
    return success(smtDormitoryPersonService.page(page,Wrappers.query(smtDormitoryPerson)));
  }


  /**
   * 通过id查询
   * @param id id
   * @return Result
   */
  @GetMapping("/{id}")
  public Result getById(@PathVariable("id") BigDecimal id){
    return success(smtDormitoryPersonService.getById(id));
  }

  /**
   * 新增
   * @param smtDormitoryPerson
   * @return Result
   */
  @SysLog("新增")
  @PostMapping("/save")
  public Result save(@RequestBody SmtDormitoryPerson smtDormitoryPerson){
    return success(smtDormitoryPersonService.save(smtDormitoryPerson));
  }

  /**
   * 修改
   * @param smtDormitoryPerson
   * @return Result
   */
  @SysLog("修改")
  @PostMapping("/update")
  public Result updateById(@RequestBody SmtDormitoryPerson smtDormitoryPerson){
    return success(smtDormitoryPersonService.updateById(smtDormitoryPerson));
  }

  /**
   * 通过id删除
   * @param id id
   * @return Result
   */
  @SysLog("删除")
  @PostMapping("/{id}")
  public Result removeById(@PathVariable BigDecimal id){
    return success(smtDormitoryPersonService.removeById(id));
  }

}
