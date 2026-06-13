package com.tce.smart.platform.controller.badge;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tce.smart.common.log.annotation.SysLog;
import com.tce.smart.platform.core.entity.badge.SmtBadgeRecord;
import com.tce.smart.platform.service.badge.SmtBadgeRecordService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.core.wrapper.BaseController;

/**
 * 厂牌补领流程表
 *
 * @author fushiping
 * @date 2020-07-07 11:47:27
 */
@RestController
@AllArgsConstructor
@RequestMapping("/badge/record")
public class SmtBadgeRecordController extends BaseController {

  private final SmtBadgeRecordService smtBadgeRecordService;

  /**
   * 分页查询
   * @param page 分页对象
   * @param smtBadgeRecord 厂牌补领流程表
   * @return
   */
  @GetMapping("/page")
  public Result getSmtBadgeRecordPage(Page page, SmtBadgeRecord smtBadgeRecord) {
    return success(smtBadgeRecordService.page(page,Wrappers.query(smtBadgeRecord)));
  }


  /**
   * 通过id查询厂牌补领流程表
   * @param id id
   * @return Result
   */
  @GetMapping("/{id}")
  public Result getById(@PathVariable("id") Integer id){
    return success(smtBadgeRecordService.getById(id));
  }

  /**
   * 新增厂牌补领流程表
   * @param smtBadgeRecord 厂牌补领流程表
   * @return Result
   */
  @SysLog("新增厂牌补领流程表")
  @PostMapping("/save")
  public Result save(@RequestBody SmtBadgeRecord smtBadgeRecord){
    return success(smtBadgeRecordService.save(smtBadgeRecord));
  }

  /**
   * 修改厂牌补领流程表
   * @param smtBadgeRecord 厂牌补领流程表
   * @return Result
   */
  @SysLog("修改厂牌补领流程表")
  @PostMapping("/update")
  public Result updateById(@RequestBody SmtBadgeRecord smtBadgeRecord){
    return success(smtBadgeRecordService.updateById(smtBadgeRecord));
  }

  /**
   * 通过id删除厂牌补领流程表
   * @param id id
   * @return Result
   */
  @SysLog("删除厂牌补领流程表")
  @PostMapping("/{id}")
  public Result removeById(@PathVariable Integer id){
    return success(smtBadgeRecordService.removeById(id));
  }

}
