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
import com.tce.smart.platform.core.dto.AddBreakOffApplicationDTO;
import com.tce.smart.platform.core.dto.SearchBreakOffDTO;
import com.tce.smart.platform.core.dto.SearchPatchDTO;
import com.tce.smart.platform.core.entity.SmtBreakoffApplication;
import com.tce.smart.platform.core.model.SearchBreakoffApplicationDetail;
import com.tce.smart.platform.service.SmtBreakoffApplicationService;

import lombok.AllArgsConstructor;

import java.util.List;


/**
 * 职工调休申请表
 *
 * @author 梁圆
 * @date 2019-04-13 18:30:08
 */
@RestController
@AllArgsConstructor
@RequestMapping("/application/breakOff")
public class SmtBreakoffApplicationController extends BaseController  {

  private final  SmtBreakoffApplicationService smtBreakoffApplicationService;

  /**
   * 分页查询
   * @param page 分页对象
   * @param smtBreakoffApplication 职工调休申请表
   * @return
   */
  @SysLog("分页查询调休申请表")
  @GetMapping("/page")
  public Result getSmtBreakOffApplicationPage(Page page, SmtBreakoffApplication smtBreakoffApplication) {
    return  new Result<>(smtBreakoffApplicationService.getSmtBreakoffApplicationPage(page, smtBreakoffApplication));
  }


  /**
   * 通过id查询职工调休申请表
   * @param id id
   * @return Result
   */
  @SysLog("通过id查询调休申请表")
  @GetMapping("/detail/{id}")
  public Result getById(@PathVariable("id") Integer id){
	  SearchBreakoffApplicationDetail breakoffApplicationById = smtBreakoffApplicationService.getBreakoffApplicationById(id);
	    return  new Result<>(breakoffApplicationById);
/*	  return success(breakoffApplicationById, SearchBreakoffApplicationDetailVO.class);
*/  }

  /**
   * 新增调休申请表
   * @param addBreakoffApplicationDTO 调休申请表
   * @return Result
   */
  @SysLog("新增调休申请表")
  @PostMapping("/add")
  public Result saveBreakOffApplication(@RequestBody AddBreakOffApplicationDTO addBreakoffApplicationDTO){
    smtBreakoffApplicationService.saveBreakoffApplication(addBreakoffApplicationDTO);
	return success();
  }

  /**
   * 获取调休类型
   * @return Result
   */
  @SysLog("获取调休类型")
  @GetMapping("/type")
  public Result getBreakOffTypeList(){
    return new Result<>(smtBreakoffApplicationService.getBreakOffTypeList());
  }

  /**
   * 获取可以调休天数
   * @return Result
   */
  @SysLog("获取可以调休天数")
  @PostMapping("/getRestCount")
  public Result getRestCountList(@RequestBody SearchPatchDTO searchPatchDTO){
    return new Result<>(smtBreakoffApplicationService.getRestCountList(searchPatchDTO));
  }


  @SysLog("页面分页查询调休申请表")
  @GetMapping("/page/list")
  public Result getSmtBreakOffApplicationPageList(Page page, SearchBreakOffDTO searchBreakOffDTO) {
	  List<Integer> parkIds = SecurityUtils.getUser().getParkIdList();
	  searchBreakOffDTO.setParkIds(parkIds);
    return  new Result<>(smtBreakoffApplicationService.getSmtBreakoffApplicationPageList(page, searchBreakOffDTO));
  }

}
