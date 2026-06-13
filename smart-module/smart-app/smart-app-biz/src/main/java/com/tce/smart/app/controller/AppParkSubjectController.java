package com.tce.smart.app.controller;

import com.tce.smart.common.core.wrapper.BaseController;
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
import com.tce.smart.app.entity.AppParkSubject;
import com.tce.smart.app.service.AppParkSubjectService;

import lombok.AllArgsConstructor;


/**
 * 园区主题
 *
 * @author mingkai.wu
 * @date 2019-04-25 09:44:25
 */
@RestController
@AllArgsConstructor
@RequestMapping("/appparksubject")
public class AppParkSubjectController extends BaseController{

  private final  AppParkSubjectService appParkSubjectService;

  /**
   * 分页查询
   * @param page 分页对象
   * @param appParkSubject 园区主题
   * @return
   */
  @GetMapping("/page")
  public Result getAppParkSubjectPage(Page page, AppParkSubject appParkSubject) {
    return  new Result<>(appParkSubjectService.page(page,Wrappers.query(appParkSubject)));
  }


  /**
   * 通过id查询园区主题
   * @param id id
   * @return Result
   */
  @GetMapping("/{id}")
  public Result getById(@PathVariable("id") Integer id){
    return new Result<>(appParkSubjectService.getById(id));
  }

  /**
   * 新增园区主题
   * @param appParkSubject 园区主题
   * @return Result
   */
  @SysLog("新增园区主题")
  @PostMapping("/save")
  public Result save(@RequestBody AppParkSubject appParkSubject){
    return new Result<>(appParkSubjectService.save(appParkSubject));
  }

  /**
   * 修改园区主题
   * @param appParkSubject 园区主题
   * @return Result
   */
  @SysLog("修改园区主题")
  @PostMapping("/update")
  public Result updateById(@RequestBody AppParkSubject appParkSubject){
    return new Result<>(appParkSubjectService.updateById(appParkSubject));
  }

  /**
   * 通过id删除园区主题
   * @param id id
   * @return Result
   */
  @SysLog("删除园区主题")
  @PostMapping("/{id}")
  public Result removeById(@PathVariable Integer id){
    return new Result<>(appParkSubjectService.removeById(id));
  }

}
