package com.tce.smart.platform.controller.admittance;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.core.wrapper.BaseController;
import com.tce.smart.common.log.annotation.SysLog;
import com.tce.smart.platform.core.entity.admittance.SmtAdmittanceVehicle;
import com.tce.smart.platform.service.admittance.SmtAdmittanceVehicleService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

/**
 * 入厂申请预约车辆表
 *
 * @author fushiping
 * @date 2021-08-17 17:45:05
 */
@RestController
@AllArgsConstructor
@RequestMapping("/smtadmittancevehicle")
public class SmtAdmittanceVehicleController extends BaseController {

  private final SmtAdmittanceVehicleService smtAdmittanceVehicleService;

  /**
   * 分页查询
   * @param page 分页对象
   * @param smtAdmittanceVehicle 入厂申请预约车辆表
   * @return
   */
  @GetMapping("/page")
  public Result getSmtAdmittanceVehiclePage(Page page, SmtAdmittanceVehicle smtAdmittanceVehicle) {
    return success(smtAdmittanceVehicleService.page(page,Wrappers.query(smtAdmittanceVehicle)));
  }


  /**
   * 通过id查询入厂申请预约车辆表
   * @param id id
   * @return Result
   */
  @GetMapping("/{id}")
  public Result getById(@PathVariable("id") BigDecimal id){
    return success(smtAdmittanceVehicleService.getById(id));
  }

  /**
   * 新增入厂申请预约车辆表
   * @param smtAdmittanceVehicle 入厂申请预约车辆表
   * @return Result
   */
  @SysLog("新增入厂申请预约车辆表")
  @PostMapping("/save")
  public Result save(@RequestBody SmtAdmittanceVehicle smtAdmittanceVehicle){
    return success(smtAdmittanceVehicleService.save(smtAdmittanceVehicle));
  }

  /**
   * 修改入厂申请预约车辆表
   * @param smtAdmittanceVehicle 入厂申请预约车辆表
   * @return Result
   */
  @SysLog("修改入厂申请预约车辆表")
  @PostMapping("/update")
  public Result updateById(@RequestBody SmtAdmittanceVehicle smtAdmittanceVehicle){
    return success(smtAdmittanceVehicleService.updateById(smtAdmittanceVehicle));
  }

  /**
   * 通过id删除入厂申请预约车辆表
   * @param id id
   * @return Result
   */
  @SysLog("删除入厂申请预约车辆表")
  @PostMapping("/{id}")
  public Result removeById(@PathVariable BigDecimal id){
    return success(smtAdmittanceVehicleService.removeById(id));
  }

}
