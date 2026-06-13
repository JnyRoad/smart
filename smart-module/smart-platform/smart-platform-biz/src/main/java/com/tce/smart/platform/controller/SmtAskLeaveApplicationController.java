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
import com.tce.smart.platform.core.dto.AddAskLeavelApplicationDTO;
import com.tce.smart.platform.core.dto.SearchLeaveDTO;
import com.tce.smart.platform.core.entity.SmtAskLeaveApplication;
import com.tce.smart.platform.service.SmtAskLeaveApplicationService;

import lombok.AllArgsConstructor;

import java.util.List;


/**
 * 请假申请表
 *
 * @author 梁圆
 * @date 2019-04-13 18:26:36
 */
@RestController
@AllArgsConstructor
@RequestMapping("/application/askLeave")
public class SmtAskLeaveApplicationController extends BaseController{

  private final  SmtAskLeaveApplicationService smtAskLeaveApplicationService;

  /**
   * 分页查询
   * @param page 分页对象
   * @param smtAskLeaveApplication 请假申请表
   * @return
   */
  @SysLog(" 分页查询请假申请表")
  @GetMapping("/page")
  public Result getAskLeavePage(Page page, SmtAskLeaveApplication smtAskLeaveApplication) {
    return  new Result<>(smtAskLeaveApplicationService.getAskLeavePage(page,smtAskLeaveApplication));
  }


  /**
   * 通过id查询请假申请表
   * @param id id
   * @return Result
   */
  @SysLog("通过id查询请假申请表")
  @GetMapping("/detail/{id}")
  public Result getAskLeaveById(@PathVariable("id") Integer id){
    return new Result<>(smtAskLeaveApplicationService.getAskLeaveById(id));
  }

  /**
   * 新增请假申请表
   * @param addAskLeavelApplicationDTO 职工请假申请表
   * @return Result
   */
  @SysLog("新增请假申请表")
  @PostMapping("/add")
  public Result add(@RequestBody AddAskLeavelApplicationDTO addAskLeavelApplicationDTO){
	  smtAskLeaveApplicationService.add(addAskLeavelApplicationDTO);
	  return success();
  }

  /**
   * 获取请假类型
   * @return Result
   */
  @SysLog("获取请假类型")
  @GetMapping("/type")
  public Result getAskTypeList(){
    return new Result<>(smtAskLeaveApplicationService.getAskTypeList());
  }


  /**
   * 根据页面条件分局查询请假
   * @param page
   * @param searchLeaveDTO
   * @return
   */
  @SysLog("分页查询请假申请表")
  @GetMapping("/page/list")
  public Result getAskLeavePageList(Page page, SearchLeaveDTO searchLeaveDTO) {
	  List<Integer> parkIds = SecurityUtils.getUser().getParkIdList();
	  searchLeaveDTO.setParkIds(parkIds);
    return  new Result<>(smtAskLeaveApplicationService.getAskLeavePageList(page,searchLeaveDTO));
  }


  @SysLog("通过id查询请假申请表")
  @GetMapping("/list/detail/{id}")
  public Result getAskLeaveByListId(@PathVariable("id") Integer id){
    return new Result<>(smtAskLeaveApplicationService.getAskLeaveByListId(id));
  }

}
