package com.tce.smart.platform.controller.admittance;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.core.wrapper.BaseController;
import com.tce.smart.common.log.annotation.SysLog;
import com.tce.smart.platform.core.entity.admittance.SmtAdmittanceFellow;
import com.tce.smart.platform.service.admittance.SmtAdmittanceFellowService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

/**
 * 入厂申请预约随行人员表
 *
 * @author fushiping
 * @date 2021-08-17 17:45:13
 */
@RestController
@AllArgsConstructor
@RequestMapping("/smtadmittancefellow")
public class SmtAdmittanceFellowController extends BaseController {

  private final SmtAdmittanceFellowService smtAdmittanceFellowService;

  /**
   * 分页查询
   * @param page 分页对象
   * @param smtAdmittanceFellow 入厂申请预约随行人员表
   * @return
   */
  @GetMapping("/page")
  public Result getSmtAdmittanceFellowPage(Page page, SmtAdmittanceFellow smtAdmittanceFellow) {
    return success(smtAdmittanceFellowService.page(page,Wrappers.query(smtAdmittanceFellow)));
  }


  /**
   * 通过id查询入厂申请预约随行人员表
   * @param id id
   * @return Result
   */
  @GetMapping("/{id}")
  public Result getById(@PathVariable("id") BigDecimal id){
    return success(smtAdmittanceFellowService.getById(id));
  }

  /**
   * 新增入厂申请预约随行人员表
   * @param smtAdmittanceFellow 入厂申请预约随行人员表
   * @return Result
   */
  @SysLog("新增入厂申请预约随行人员表")
  @PostMapping("/save")
  public Result save(@RequestBody SmtAdmittanceFellow smtAdmittanceFellow){
    return success(smtAdmittanceFellowService.save(smtAdmittanceFellow));
  }

  /**
   * 修改入厂申请预约随行人员表
   * @param smtAdmittanceFellow 入厂申请预约随行人员表
   * @return Result
   */
  @SysLog("修改入厂申请预约随行人员表")
  @PostMapping("/update")
  public Result updateById(@RequestBody SmtAdmittanceFellow smtAdmittanceFellow){
    return success(smtAdmittanceFellowService.updateById(smtAdmittanceFellow));
  }

  /**
   * 通过id删除入厂申请预约随行人员表
   * @param id id
   * @return Result
   */
  @SysLog("删除入厂申请预约随行人员表")
  @PostMapping("/{id}")
  public Result removeById(@PathVariable BigDecimal id){
    return success(smtAdmittanceFellowService.removeById(id));
  }

}
