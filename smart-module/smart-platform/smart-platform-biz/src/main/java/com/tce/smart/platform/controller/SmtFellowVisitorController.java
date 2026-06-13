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
import com.tce.smart.platform.core.entity.SmtFellowVisitor;
import com.tce.smart.platform.service.SmtFellowVisitorService;

import lombok.AllArgsConstructor;


/**
 * 随行人员表
 *
 * @author 梁圆
 * @date 2019-04-13 18:19:44
 */
@RestController
@AllArgsConstructor
@RequestMapping("/smtfellowvisitor")
public class SmtFellowVisitorController {

  private final  SmtFellowVisitorService smtFellowVisitorService;

  /**
   * 分页查询
   * @param page 分页对象
   * @param smtFellowVisitor 随行人员表
   * @return
   */
  @GetMapping("/page")
  public Result getSmtFellowVisitorPage(Page page, SmtFellowVisitor smtFellowVisitor) {
    return  new Result<>(smtFellowVisitorService.page(page,Wrappers.query(smtFellowVisitor)));
  }


  /**
   * 通过id查询随行人员表
   * @param id id
   * @return Result
   */
  @GetMapping("/{id}")
  public Result getById(@PathVariable("id") Long id){
    return new Result<>(smtFellowVisitorService.getById(id));
  }

  /**
   * 新增随行人员表
   * @param smtFellowVisitor 随行人员表
   * @return Result
   */
  @SysLog("新增随行人员表")
  @PostMapping("/save")
  public Result save(@RequestBody SmtFellowVisitor smtFellowVisitor){
    return new Result<>(smtFellowVisitorService.save(smtFellowVisitor));
  }

  /**
   * 修改随行人员表
   * @param smtFellowVisitor 随行人员表
   * @return Result
   */
  @SysLog("修改随行人员表")
  @PostMapping("/update")
  public Result updateById(@RequestBody SmtFellowVisitor smtFellowVisitor){
    return new Result<>(smtFellowVisitorService.updateById(smtFellowVisitor));
  }

  /**
   * 通过id删除随行人员表
   * @param id id
   * @return Result
   */
  @SysLog("删除随行人员表")
  @PostMapping("/{id}")
  public Result removeById(@PathVariable Long id){
    return new Result<>(smtFellowVisitorService.removeById(id));
  }

}
