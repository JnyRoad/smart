package com.tce.smart.platform.controller.approval;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tce.smart.common.log.annotation.SysLog;
import com.tce.smart.platform.core.entity.SmtApprovalCondition;
import com.tce.smart.platform.core.service.SmtApprovalConditionService;
import com.tce.smart.tool.enums.ArticlesReleaseTypeEnum;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.core.wrapper.BaseController;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 *
 *
 * @author fushiping
 * @date 2021-04-08 16:25:24
 */
@RestController
@AllArgsConstructor
@Api(tags = "platform-审批条件")
@RequestMapping("/smtapprovalcondition")
public class SmtApprovalConditionController extends BaseController {

  private final SmtApprovalConditionService smtApprovalConditionService;

  /**
   * 分页查询
   * @param page 分页对象
   * @param smtApprovalCondition
   * @return
   */
  @GetMapping("/page")
  @ApiOperation("分页查询")
  public Result getSmtApprovalConditionPage(Page page, SmtApprovalCondition smtApprovalCondition) {
    return success(smtApprovalConditionService.page(page,Wrappers.query(smtApprovalCondition)));
  }


  /**
   * 通过id查询
   * @param id id
   * @return Result
   */
  @GetMapping("/{id}")
  @ApiOperation("通过id查询")
  public Result getById(@PathVariable("id") Integer id){
    return success(smtApprovalConditionService.getById(id));
  }

  /**
   * 新增
   * @param smtApprovalCondition
   * @return Result
   */
  @SysLog("新增")
  @PostMapping("/save")
  @ApiOperation("新增")
  public Result save(@RequestBody SmtApprovalCondition smtApprovalCondition){
    return success(smtApprovalConditionService.save(smtApprovalCondition));
  }

  /**
   * 修改
   * @param smtApprovalCondition
   * @return Result
   */
  @SysLog("修改")
  @PostMapping("/update")
  @ApiOperation("修改")
  public Result updateById(@RequestBody SmtApprovalCondition smtApprovalCondition){
    return success(smtApprovalConditionService.updateById(smtApprovalCondition));
  }

  /**
   * 通过id删除
   * @param id id
   * @return Result
   */
  @SysLog("删除")
  @PostMapping("/{id}")
  @ApiOperation("通过id删除")
  public Result removeById(@PathVariable Integer id){
    return success(smtApprovalConditionService.removeById(id));
  }


	@GetMapping("/type/list")
	@ApiOperation("物品放行可选类型")
	public Result<List<Map<String, Object>>> getCaseCreateTypeList(@RequestParam(value = "type",required = false) Integer type) {
		return success(ArticlesReleaseTypeEnum.list(type));
	}

}
