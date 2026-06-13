package com.tce.smart.platform.controller;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.core.wrapper.BaseController;
import com.tce.smart.common.log.annotation.SysLog;
import com.tce.smart.platform.api.dto.resp.SmtApplicationWorkRespDTO;
import com.tce.smart.platform.core.entity.SmtApplicationWork;
import com.tce.smart.platform.service.SmtApplicationWorkService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;


/**
 * 应聘者工作经验
 *
 * @author 齐佩
 * @date 2019-04-22 15:25:10
 */
@RestController
@AllArgsConstructor
@RequestMapping("/application/work")
public class SmtApplicationWorkController extends BaseController {

  private final  SmtApplicationWorkService smtApplicationWorkService;

  /**
   * 分页查询
   * @param page 分页对象
   * @param smtApplicationWork 应聘者工作经验
   * @return
   */
  @GetMapping("/page")
  public Result getSmtApplicationWorkPage(Page page, SmtApplicationWork smtApplicationWork) {
    return  new Result<>(smtApplicationWorkService.page(page,Wrappers.query(smtApplicationWork)));
  }


  /**
   * 通过id查询应聘者工作经验
   * @param id id
   * @return Result
   */
  @GetMapping("/{id}")
  public Result getById(@PathVariable("id") Integer id){
    return new Result<>(smtApplicationWorkService.getById(id));
  }

  /**
   * 新增应聘者工作经验
   * @param smtApplicationWork 应聘者工作经验
   * @return Result
   */
  @SysLog("新增应聘者工作经验")
  @PostMapping("addApplicationWork")
  public Result addApplicationWork(@RequestBody SmtApplicationWork smtApplicationWork){
    return smtApplicationWorkService.addApplicationWork(smtApplicationWork);
  }

  /**
   * 修改应聘者工作经验
   * @param smtApplicationWork 应聘者工作经验
   * @return Result
   */
  @SysLog("修改应聘者工作经验")
  @PostMapping("updateApplicationWork")
  public Result updateById(@RequestBody SmtApplicationWork smtApplicationWork){
    return smtApplicationWorkService.updateApplicationWork(smtApplicationWork);
  }

  /**
   * 通过id删除应聘者工作经验
   * @param id id
   * @return Result
   */
  @SysLog("删除应聘者工作经验")
  @PostMapping("/{id}")
  public Result removeById(@PathVariable Integer id){
    return new Result<>(smtApplicationWorkService.removeById(id));
  }
  @SysLog("公众号接口获取工作经历")
  @GetMapping("/list/{applicationId}")
  public Result<List<SmtApplicationWorkRespDTO>> getSmtApplicationWorkList(@PathVariable("applicationId") String applicationId ) {
    return  success(smtApplicationWorkService.getSmtApplicationWorkList(applicationId), SmtApplicationWorkRespDTO.class);
  }

  @SysLog("公众号接口删除工作经历")
  @GetMapping("/delete/{applicationId}")
  public Result<Boolean> deleteApplicationWorkList(@PathVariable("applicationId") String applicationId ) {
    return success(smtApplicationWorkService.deleteApplicationWorkList(applicationId));
  }




}
