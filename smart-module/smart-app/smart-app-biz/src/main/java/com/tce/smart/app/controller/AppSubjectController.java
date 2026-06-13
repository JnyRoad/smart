package com.tce.smart.app.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tce.smart.app.dto.AppQuestionDto;
import com.tce.smart.app.entity.AppSubject;
import com.tce.smart.app.service.AppSubjectService;
import com.tce.smart.app.vo.AppQuestionVo;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.core.wrapper.BaseController;
import com.tce.smart.common.log.annotation.SysLog;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;


/**
 * 主题信息
 *
 * @author fushiping
 * @date 2019-04-25 09:44:43
 */
@RestController
@AllArgsConstructor
@RequestMapping("/appsubject")
public class AppSubjectController extends BaseController {

  private final  AppSubjectService appSubjectService;

  /**
   * 分页查询
   * @param page 分页对象
   * @param appSubject 主题信息
   * @return
   */
  @GetMapping("/page")
  public Result getAppSubjectPage(Page page, AppSubject appSubject) {
    return  new Result<>(appSubjectService.page(page,Wrappers.query(appSubject)));
  }

  /**
   * 分页查询问题信息
   * @param page 分页对象
   * @param appQuestionDto 传入条件，问题类（问题名字和时间）
   * @return
   */
  @GetMapping("/questionPage")
  public Result getAppQuestionPage(Page page, AppQuestionDto appQuestionDto) {
	  IPage<AppSubject> iPage = appSubjectService.getAppQuestionPage(page,appQuestionDto);
	  return  success(iPage, AppQuestionVo.class);
  }

  /**
   * 通过id删除问题信息
   * @param id id
   * @return Result
   */
  @GetMapping("/deleteQs/{id}")
  public Result deleteQuestionById(@PathVariable("id") Integer id){
	  appSubjectService.deleteQuestion(id);
	  return success();
  }

	/**
	 * 通过id查询问题信息
	 * @param id id
	 * @return Result
	 */
	@GetMapping("/detailQs/{id}")
	public Result detailQuestionById(@PathVariable("id") Integer id){
		AppSubject appSubject = appSubjectService.detailQuestionById(id);
		return success(appSubject,AppQuestionVo.class);
	}
  /**
   * 通过id查询主题信息
   * @param id id
   * @return Result
   */
  @GetMapping("/{id}")
  public Result getById(@PathVariable("id") Integer id){
	  return new Result<>(appSubjectService.getById(id));
  }

  /**
   * 新增主题信息
   * @param appSubject 主题信息
   * @return Result
   */
  @SysLog("新增主题信息")
  @PostMapping("/save")
  public Result save(@RequestBody AppSubject appSubject){
    return new Result<>(appSubjectService.save(appSubject));
  }

  /**
   * 修改主题信息
   * @param appSubject 主题信息
   * @return Result
   */
  @SysLog("修改主题信息")
  @PostMapping("/update")
  public Result updateById(@RequestBody AppSubject appSubject){
    return new Result<>(appSubjectService.updateById(appSubject));
  }

  /**
   * 通过id删除主题信息
   * @param id id
   * @return Result
   */
  @SysLog("删除主题信息")
  @PostMapping("/{id}")
  public Result removeById(@PathVariable Integer id){
    return new Result<>(appSubjectService.removeById(id));
  }

}
