package com.tce.smart.platform.controller.approval;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tce.smart.common.log.annotation.SysLog;
import com.tce.smart.platform.core.entity.SmtApprovalPerson;
import com.tce.smart.platform.core.service.SmtApprovalPersonService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.core.wrapper.BaseController;

/**
 *
 *
 * @author fushiping
 * @date 2021-04-08 16:25:00
 */
@RestController
@AllArgsConstructor
@Api(tags = "platform-审批人员")
@RequestMapping("/smtapprovalperson")
public class SmtApprovalPersonController extends BaseController {

  private final SmtApprovalPersonService smtApprovalPersonService;

  /**
   * 分页查询
   * @param page 分页对象
   * @param smtApprovalPerson
   * @return
   */
  @GetMapping("/page")
  @ApiOperation("分页查询")
  public Result getSmtApprovalPersonPage(Page page, SmtApprovalPerson smtApprovalPerson) {
    return success(smtApprovalPersonService.page(page,Wrappers.query(smtApprovalPerson)));
  }


  /**
   * 通过id查询
   * @param id id
   * @return Result
   */
  @GetMapping("/{id}")
  @ApiOperation("通过id查询")
  public Result getById(@PathVariable("id") Integer id){
    return success(smtApprovalPersonService.getById(id));
  }

  /**
   * 新增
   * @param smtApprovalPerson
   * @return Result
   */
  @SysLog("新增")
  @PostMapping("/save")
  @ApiOperation("新增")
  public Result save(@RequestBody SmtApprovalPerson smtApprovalPerson){
    return success(smtApprovalPersonService.save(smtApprovalPerson));
  }

  /**
   * 修改
   * @param smtApprovalPerson
   * @return Result
   */
  @SysLog("修改")
  @PostMapping("/update")
  @ApiOperation("修改")
  public Result updateById(@RequestBody SmtApprovalPerson smtApprovalPerson){
    return success(smtApprovalPersonService.updateById(smtApprovalPerson));
  }

  /**
   * 通过id删除
   * @param id id
   * @return Result
   */
  @SysLog("删除")
  @PostMapping("/{id}")
  @ApiOperation("删除")
  public Result removeById(@PathVariable Integer id){
    return success(smtApprovalPersonService.removeById(id));
  }

}
