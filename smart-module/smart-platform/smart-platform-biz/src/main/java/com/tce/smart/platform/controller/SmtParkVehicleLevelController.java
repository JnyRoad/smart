package com.tce.smart.platform.controller;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.log.annotation.SysLog;
import com.tce.smart.platform.core.entity.SmtParkVehicleLevel;
import com.tce.smart.platform.service.SmtParkVehicleLevelService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import static com.tce.smart.common.core.model.Result.success;


/**
 * 园区车辆入园职层表
 *
 * @author mckaywu
 * @date 2019-11-20 10:36:48
 */
@RestController
@AllArgsConstructor
@RequestMapping("/parkvehiclelevel")
public class SmtParkVehicleLevelController {

  private final  SmtParkVehicleLevelService smtParkVehicleLevelService;

  /**
   * 分页查询
   * @param page 分页对象
   * @param smtParkVehicleLevel 园区车辆入园职层表
   * @return
   */
  @GetMapping("/page")
  public Result<Page<SmtParkVehicleLevel>> getSmtParkVehicleLevelPage(Page page, SmtParkVehicleLevel smtParkVehicleLevel) {
    return  success(smtParkVehicleLevelService.page(page,Wrappers.query(smtParkVehicleLevel)));
  }


  /**
   * 通过id查询园区车辆入园职层表
   * @param id id
   * @return Result
   */
  @GetMapping("/{id}")
  public Result<Boolean> getById(@PathVariable("id") Integer id){
    return success(smtParkVehicleLevelService.getById(id));
  }

  /**
   * 新增园区车辆入园职层表
   * @param smtParkVehicleLevel 园区车辆入园职层表
   * @return Result
   */
  @SysLog("新增园区车辆入园职层表")
  @PostMapping("/save")
  public Result<Boolean> save(@RequestBody SmtParkVehicleLevel smtParkVehicleLevel){
    return success(smtParkVehicleLevelService.save(smtParkVehicleLevel));
  }

  /**
   * 修改园区车辆入园职层表
   * @param smtParkVehicleLevel 园区车辆入园职层表
   * @return Result
   */
  @SysLog("修改园区车辆入园职层表")
  @PostMapping("/update")
  public Result<Boolean> updateById(@RequestBody SmtParkVehicleLevel smtParkVehicleLevel){
    return success(smtParkVehicleLevelService.updateById(smtParkVehicleLevel));
  }

  /**
   * 通过id删除园区车辆入园职层表
   * @param id id
   * @return Result
   */
  @SysLog("删除园区车辆入园职层表")
  @PostMapping("/{id}")
  public Result<Boolean> removeById(@PathVariable Integer id){
    return success(smtParkVehicleLevelService.removeById(id));
  }

}
