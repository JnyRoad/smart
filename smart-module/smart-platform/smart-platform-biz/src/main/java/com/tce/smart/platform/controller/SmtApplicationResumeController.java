package com.tce.smart.platform.controller;

import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.core.wrapper.BaseController;
import com.tce.smart.common.log.annotation.SysLog;
import com.tce.smart.platform.core.entity.SmtApplicationResume;
import com.tce.smart.platform.service.SmtApplicationResumeService;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;


/**
 * 应聘者简历
 *
 * @author 齐佩
 * @date 2019-04-22 15:25:26
 */
@RestController
@AllArgsConstructor
@RequestMapping("/application/resume")
public class SmtApplicationResumeController extends BaseController {

  private final  SmtApplicationResumeService smtApplicationResumeService;


  /**
   * 通过应聘id查询简历
   * @param id id
   * @return Result
   */
  @GetMapping("/{id}")
  public Result getById(@PathVariable("id") Integer id){
    return smtApplicationResumeService.getResumeById(id);
  }

  /**
   * 新增应聘者简历表
   * @param smtApplicationResume 新增应聘者简历表
   * @return Result
   */
  @SysLog("新增应聘者简历 ")
  @PostMapping("addApplicationResume")
  public Result save(@RequestBody SmtApplicationResume smtApplicationResume){
	Boolean bo = smtApplicationResumeService.save(smtApplicationResume);
	if(!bo){
		return fail("简历保存失败");
	}
    return success(bo);
  }

  /**
   * 修改应聘者人际关系表
   * @param smtApplicationResume 应聘者人际关系表
   * @return Result
   */
  @SysLog("修改应聘者简历")
  @PostMapping("updateApplicationResume")
  public Result updateById(@RequestParam("resume") MultipartFile file,@RequestBody SmtApplicationResume smtApplicationResume){
    return smtApplicationResumeService.updateApplicationResume(file,smtApplicationResume);
  }


}
