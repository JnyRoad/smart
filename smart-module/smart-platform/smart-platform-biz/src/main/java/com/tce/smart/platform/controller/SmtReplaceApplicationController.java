package com.tce.smart.platform.controller;

import com.tce.smart.common.security.util.SecurityUtils;
import com.tce.smart.platform.api.dto.req.PatchStatisticsReqDTO;
import com.tce.smart.platform.api.dto.resp.PatchStatisticsRespDTO;
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
import com.tce.smart.common.security.annotation.Inner;
import com.tce.smart.platform.core.dto.AddReplaceApplicationDTO;
import com.tce.smart.platform.core.dto.SearchAttendanceDTO;
import com.tce.smart.platform.core.dto.SearchPatchDTO;
import com.tce.smart.platform.core.dto.SearchReplaceDTO;
import com.tce.smart.platform.core.entity.SmtReplaceApplication;
import com.tce.smart.platform.service.SmtReplaceApplicationService;
import lombok.AllArgsConstructor;

import java.util.List;

/**
 * 考勤、补卡申请表

 *
 * @author 梁圆
 * @date 2019-04-13 18:19:37
 */
@RestController
@AllArgsConstructor
@RequestMapping("/application/attendance")
public class SmtReplaceApplicationController extends BaseController{

  private final  SmtReplaceApplicationService smtReplaceApplicationService;

  /**
   * 考勤记录查询
   * @param searchAttendanceDTO
   * @return
   */
  @PostMapping("/getAttendance")
  public Result getAttendance(@RequestBody SearchAttendanceDTO searchAttendanceDTO) {
	  return  new Result<>(smtReplaceApplicationService.getAttendance(searchAttendanceDTO));
  }
  /**
   * 考勤记录详情查询
   * @param searchAttendanceDTO

   * @return
   */
  @PostMapping("/detail")
  public Result getAttendanceDetail(@RequestBody SearchAttendanceDTO searchAttendanceDTO) {
	  return  new Result<>(smtReplaceApplicationService.getAttendanceDetail(searchAttendanceDTO));
  }
  /**
   * 考勤正常记录详情查询
   * @param searchAttendanceDTO

   * @return
   */
  @PostMapping("/success/detail")
  public Result getAttendanceSuccessDetail(@RequestBody SearchAttendanceDTO searchAttendanceDTO) {
	  return  new Result<>(smtReplaceApplicationService.getAttendanceSuccessDetail(searchAttendanceDTO));
  }
  /**
   * 补卡记录分页查询
   * @param page 分页对象
   * @param smtReplaceApplication 职工申请表

   * @return
   */
  @GetMapping("/replace/page")
  public Result getSmtReplaceApplicationPage(Page page, SmtReplaceApplication smtReplaceApplication) {
    return  new Result<>(smtReplaceApplicationService.getSmtReplaceApplicationPage(page,smtReplaceApplication));
  }


  @GetMapping("/replace/detail/{recordId}")
  public Result getSmtReplaceApplicationDetail(@PathVariable("recordId") Integer recordId ) {
    return  new Result<>(smtReplaceApplicationService.getSmtReplaceApplicationDetail(recordId ));
  }

  /**
   * 补卡当月次数信息查询，根据补卡的时间
   * @return
   */
  @PostMapping("/replace/patchCount")
  public Result getPatchCount(@RequestBody SearchPatchDTO searchPatchDTO) {
	  return  new Result<>(smtReplaceApplicationService.getPatchCount(searchPatchDTO));
  }

  /**
   * 补卡查询
   * @return
   */
  @PostMapping("/replace/patch")
  public Result getPatchApplication(@RequestBody SearchPatchDTO searchPatchDTO) {
	  return  new Result<>(smtReplaceApplicationService.getPatchApplication(searchPatchDTO));
  }

  /**
   * 通过id查询职工补卡申请表

   * @param id id
   * @return Result
   */
  @GetMapping("/replace/{id}")
  public Result getById(@PathVariable("id") Integer id){
    return new Result<>(smtReplaceApplicationService.getById(id));
  }

  /**
   * 新增补卡申请表
   * @param addReplaceApplicationDTO 补卡申请表
   * @return Result
   */
  @SysLog("新增补卡申请表")
  @PostMapping("/replace/add")
  public Result save(@RequestBody AddReplaceApplicationDTO addReplaceApplicationDTO){
	  smtReplaceApplicationService.add(addReplaceApplicationDTO);
	  return success();
  }

  /**
   * 获取补卡原因
   * @return Result
   */
  @SysLog("获取补卡原因")
  @GetMapping("/replace/reason")
  public Result getPatchCardReason(){
    return new Result<>(smtReplaceApplicationService.getPatchCardReason());
  }
  /**
   * 通过id查询出差流程
   * @param id id
   * @return Result
   */
  @SysLog("通过id查询补卡流程")
  @GetMapping("/infoFlow/{id}")
  public Result getInfoFlow(@PathVariable("id") Integer id){
	  return new Result<>(smtReplaceApplicationService.getInfoFlow(id));
  }

	/**
	 *考勤异常消息提醒
	 * @return
	 */
	@Inner
	@GetMapping("/patchErrorPushMsg")
	public Result patchErrorPushMsg() {
		smtReplaceApplicationService.patchErrorPushMsg();
		return success();
	}

	 @GetMapping("/replace/page/list")
	  public Result getSmtReplaceApplicationPageList(Page page, SearchReplaceDTO searchReplaceDTO) {
		 List<Integer> parkIds = SecurityUtils.getUser().getParkIdList();
		 searchReplaceDTO.setParkIds(parkIds);
	    return  new Result<>(smtReplaceApplicationService.getSmtReplaceApplicationPageList(page,searchReplaceDTO));
	  }

	/**
	 *
	 * @return
	 */
	@Inner
	@PostMapping("/patch/statistics")
	public Result patchCountStatistics(Page page, @RequestBody(required = false) PatchStatisticsReqDTO reqDTO) {
		return success(smtReplaceApplicationService.patchCountStatistics(page, reqDTO), PatchStatisticsRespDTO.class);
	}
}
