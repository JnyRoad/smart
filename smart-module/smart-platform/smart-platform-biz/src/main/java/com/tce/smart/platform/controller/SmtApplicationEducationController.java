package com.tce.smart.platform.controller;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.core.wrapper.BaseController;
import com.tce.smart.common.log.annotation.SysLog;
import com.tce.smart.platform.api.dto.resp.EducationRespDTO;
import com.tce.smart.platform.core.entity.SmtApplicationEducation;
import com.tce.smart.platform.service.SmtApplicationEducationService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;


/**
 * 应聘者教育经验
 *
 * @author 齐佩
 * @date 2019-04-22 15:25:38
 */
@RestController
@AllArgsConstructor
@RequestMapping("/application/education")
public class SmtApplicationEducationController extends BaseController {

  private final  SmtApplicationEducationService smtApplicationEducationService;

  /**
   * 分页查询
   * @param page 分页对象
   * @param smtApplicationEducation 应聘者教育经验
   * @return
   */
  @GetMapping("/page")
  public Result getSmtApplicationEducationPage(Page page, SmtApplicationEducation smtApplicationEducation) {
    return  new Result<>(smtApplicationEducationService.page(page,Wrappers.query(smtApplicationEducation)));
  }



  /**
   * 通过id查询应聘者教育经验
   * @param id id
   * @return Result
   */
  @GetMapping("/{id}")
  public Result getById(@PathVariable("id") Integer id){
    return new Result<>(smtApplicationEducationService.getById(id));
  }

  /**
   * 新增应聘者教育经验
   * @param smtApplicationEducation 应聘者教育经验
   * @return Result
   */
  @SysLog("新增应聘者教育经验")
  @PostMapping("addApplicationeEducation")
  public Result save(@RequestBody SmtApplicationEducation smtApplicationEducation){
    return smtApplicationEducationService.saveEducation(smtApplicationEducation);
  }

  /**
   * 修改应聘者教育经验
   * @param smtApplicationEducation 应聘者教育经验
   * @return Result
   */
  @SysLog("修改应聘者教育经验")
  @PostMapping("updateApplicationeEducation")
  public Result updateById(@RequestBody SmtApplicationEducation smtApplicationEducation){
    return smtApplicationEducationService.updateApplicationeEducation(smtApplicationEducation);
  }

  /**
   * 通过id删除应聘者教育经验
   * @param id id
   * @return Result
   */
  @SysLog("删除应聘者教育经验")
  @PostMapping("/{id}")
  public Result removeById(@PathVariable Integer id){
    return new Result<>(smtApplicationEducationService.removeById(id));
  }


  @SysLog("公众号接口获取教育经历")
  @GetMapping("/list/{applicationId}")
  public Result<List<EducationRespDTO>> getSmtApplicationEducationList(@PathVariable("applicationId") String applicationId) {
    return  success(smtApplicationEducationService.getSmtApplicationEducationList(applicationId), EducationRespDTO.class);
  }

  @SysLog("公众号删除教育经历")
  @GetMapping("/delete/{applicationId}")
  public Result<Integer> deleteEducationList(@PathVariable("applicationId") String applicationId) {
    return success(smtApplicationEducationService.deleteEducationList(applicationId));
  }





}
