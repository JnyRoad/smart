package com.tce.smart.app.controller;

import com.tce.smart.app.dto.AppQuestionDto;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.log.annotation.SysLog;
import com.tce.smart.app.entity.AppSuggestInfo;
import com.tce.smart.app.service.AppSuggestInfoService;

import lombok.AllArgsConstructor;
import org.springframework.web.context.request.WebRequest;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;


/**
 * 意见反馈
 *
 * @author mingkai.wu
 * @date 2019-04-25 11:32:25
 */
@RestController
@AllArgsConstructor
@RequestMapping("/appsuggestinfo")
public class AppSuggestInfoController {

  private final  AppSuggestInfoService appSuggestInfoService;

  /**
   * 分页查询
   * @param page 分页对象
   * @param appQuestionDto 意见反馈
   * @return
   */
  @GetMapping("/page")
  public Result getAppSuggestInfoPage(Page page, AppQuestionDto appQuestionDto) {

    return  new Result<>(appSuggestInfoService.getAppSuggestInfoPage(page,appQuestionDto));
  }

  /**
   * 通过id查询意见反馈
   * @param id id
   * @return Result
   */
  @GetMapping("/{id}")
  public Result getById(@PathVariable("id") Integer id){
    return new Result<>(appSuggestInfoService.getById(id));
  }

  /**
   * 新增意见反馈
   * @param appSuggestInfo 意见反馈
   * @return Result
   */
  @SysLog("新增意见反馈")
  @PostMapping("/save")
  public Result save(@RequestBody AppSuggestInfo appSuggestInfo){
    return new Result<>(appSuggestInfoService.save(appSuggestInfo));
  }

  /**
   * 修改意见反馈
   * @param appSuggestInfo 意见反馈
   * @return Result
   */
  @SysLog("修改意见反馈")
  @PostMapping("/update")
  public Result updateById(@RequestBody AppSuggestInfo appSuggestInfo){
    return new Result<>(appSuggestInfoService.updateById(appSuggestInfo));
  }

  /**
   * 通过id删除意见反馈
   * @param id id
   * @return Result
   */
  @SysLog("删除意见反馈")
  @PostMapping("/{id}")
  public Result removeById(@PathVariable Integer id){
    return new Result<>(appSuggestInfoService.removeById(id));
  }

}
