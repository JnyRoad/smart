package com.tce.smart.platform.controller;

import javax.validation.Valid;

import com.tce.smart.common.security.util.SecurityUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.log.annotation.SysLog;
import com.tce.smart.platform.core.dto.VehicleApplyDTO;
import com.tce.smart.platform.core.entity.SmtVehicleApply;
import com.tce.smart.platform.service.SmtVehicleApplyService;

import lombok.AllArgsConstructor;

import java.util.List;


/**
 * 入园申请信息表
 *
 * @author 王艳勇
 * @date 2019-04-15 11:33:02
 */
@RestController
@AllArgsConstructor
@RequestMapping("/vehicle/apply")
public class SmtVehicleApplyController {

  private final SmtVehicleApplyService smtVehicleApplyService;
  /**
   * 分页查询
   * @param page 分页对象
   * @param vehicleApplyDTO 车辆信息表
   * @return
   */
  @GetMapping("/page")
  public Result getSmtVehicleApplyPage(Page page, VehicleApplyDTO vehicleApplyDTO) {
	List<Integer> parkIds = SecurityUtils.getUser().getParkIdList();
    vehicleApplyDTO.setParkIds(parkIds);
    return  new Result <>(smtVehicleApplyService.getVehicleApply(page,vehicleApplyDTO));
  }


  /**
   * 通过id查询入园申请信息表
   * @param id id
   * @return Result
   */
  @GetMapping("/{id}")
  public Result getById(@PathVariable("id") Long id){
    return new Result <>(smtVehicleApplyService.getVehicleApplyDetail(id));
  }

  /**
   * 新增入园申请信息表
   * @param smtVehicleApply 入园申请信息表
   * @return Result
   */
  @SysLog("新增入园申请信息表")
  @PostMapping("/save")
  public Result save(@Valid @RequestBody SmtVehicleApply smtVehicleApply){
    return new Result <>(smtVehicleApplyService.save(smtVehicleApply));
  }

  /**
   * 修改入园申请信息表
   * @param vehicleApply 入园申请信息表
   * @return Result
   */
  @SysLog("修改入园申请信息表")
  @PostMapping("/update")
  public Result updateById(@Valid @RequestBody SmtVehicleApply vehicleApply){
    return new Result <>(smtVehicleApplyService.updateStatus(vehicleApply));
  }

  /**
   * 通过id删除入园申请信息表
   * @param id id
   * @return Result
   */
  @SysLog("删除入园申请信息表")
  @PostMapping("/{id}")
  public Result removeById(@PathVariable Long id){
    return new Result <>(smtVehicleApplyService.removeById(id));
  }

}
