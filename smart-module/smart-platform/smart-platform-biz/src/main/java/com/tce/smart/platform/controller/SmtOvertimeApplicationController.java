package com.tce.smart.platform.controller;

import com.tce.smart.common.security.util.SecurityUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.core.wrapper.BaseController;
import com.tce.smart.common.log.annotation.SysLog;
import com.tce.smart.platform.core.dto.AddOverTimeApplicationDTO;
import com.tce.smart.platform.core.dto.SearchOverTimeDTO;
import com.tce.smart.platform.core.entity.SmtOvertimeApplication;
import com.tce.smart.platform.service.SmtOvertimeApplicationService;

import lombok.AllArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;


/**
 * 职工加班申请表

 *
 * @author 梁圆
 * @date 2019-04-13 18:20:11
 */
@RestController
@AllArgsConstructor
@RequestMapping("/application/overTime")
public class SmtOvertimeApplicationController extends BaseController{

  private final  SmtOvertimeApplicationService smtOvertimeApplicationService;

  /**
   * 分页查询
   * @param page 分页对象
   * @param smtOvertimeApplication 加班申请表

   * @return
   */
  @SysLog(" 分页查询加班申请表")
  @GetMapping("/page")
  public Result getOvertimeApplicationPage(Page page, SmtOvertimeApplication smtOvertimeApplication) {
    return  new Result<>(smtOvertimeApplicationService.getOvertimeApplicationPage(page,smtOvertimeApplication));
  }


  /**
   * 通过id查询加班申请表

   * @param id id
   * @return Result
   */
  @SysLog("通过id查询加班申请表")
  @GetMapping("/detail/{id}")
  public Result getOverTimeById(@PathVariable("id") Integer id){
    return new Result<>(smtOvertimeApplicationService.getOverTimeById(id));
  }

  /**
   * 新增职工加班申请表

   * @param  addOverTimeApplicationDTO 加班申请表

   * @return Result
   */
  @SysLog("新增加班申请表")
  @PostMapping("/add")
  public Result save(@RequestBody AddOverTimeApplicationDTO addOverTimeApplicationDTO){
    smtOvertimeApplicationService.save(addOverTimeApplicationDTO);
    return success();
  }

  /**
   * 班别类型
   * @return Result
   */
  @SysLog("获取班别类型")
  @GetMapping("/classType")
  public Result getOverClassTypeList(){
    return new Result<>(smtOvertimeApplicationService.getOverClassTypeList());
  }
  /**
   * 加班类型
   * @return Result
   */
  @SysLog("获取加班类型")
  @GetMapping("/overTimeType")
  public Result getOverTypeList(){
	  return new Result<>(smtOvertimeApplicationService.getOverTypeList());
  }


  @SysLog("分页查询加班申请表")
  @GetMapping("/page/list")
  public Result getOvertimeApplicationPageList(Page page, SearchOverTimeDTO searchLeaveDTO) {
	  List<Integer> parkIds = SecurityUtils.getUser().getParkIdList();
	  searchLeaveDTO.setParkIds(parkIds);
    return  new Result<>(smtOvertimeApplicationService.getOvertimeApplicationPageList(page,searchLeaveDTO));
  }

  @SysLog("通过id查询加班申请表")
  @GetMapping("/list/detail/{id}")
  public Result getOverTimeByListId(@PathVariable("id") Integer id){
    return new Result<>(smtOvertimeApplicationService.getOverTimeByListId(id));
  }
}
