package com.tce.smart.platform.controller;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.log.annotation.SysLog;
import com.tce.smart.platform.core.entity.SmtParkingCount;
import com.tce.smart.platform.service.SmtParkingCountService;

import lombok.AllArgsConstructor;


/**
 * 车位统计表
 *
 * @author 王艳勇
 * @date 2019-04-15 11:30:56
 */
@RestController
@AllArgsConstructor
@RequestMapping("/parking/count")
public class SmtParkingCountController {

  private final  SmtParkingCountService smtParkingCountService;

  /**
   * 分页查询
   * @param page 分页对象
   * @param smtParkingCount 车位统计表
   * @return
   */
  @GetMapping("/page")
  public Result getSmtParkingCountPage(Page page, SmtParkingCount smtParkingCount) {
    return  new Result <>(smtParkingCountService.page(page,Wrappers.query(smtParkingCount)));
  }


  /**
   * 通过园区ID查询最新车位统计
   * @param parkingId 停车场ID
   * @return Result
   */
  @GetMapping("/{parkingId}")
  public Result getByparkId(@PathVariable("parkingId") String parkingId){
    return smtParkingCountService.getByParkingId(parkingId);
  }

  /**
   * 新增车位统计表
   * @param smtParkingCount 车位统计表
   * @return Result
   */
  @SysLog("新增车位统计表")
  @PostMapping("/save")
  public Result save(@RequestBody SmtParkingCount smtParkingCount){
    return new Result <>(smtParkingCountService.saveOrUpdate(smtParkingCount));
  }

  /**
   * 修改车位统计表
   * @param smtParkingCount 车位统计表
   * @return Result
   */
  @SysLog("修改车位统计表")
  @PostMapping
  public Result updateById(@RequestBody SmtParkingCount smtParkingCount){
    return new Result <>(smtParkingCountService.updateById(smtParkingCount));
  }

  /**
   * 通过id删除车位统计表
   * @param id id
   * @return Result
   */
  @SysLog("删除车位统计表")
  @PostMapping("/{id}")
  public Result removeById(@PathVariable Integer id){
    return new Result <>(smtParkingCountService.removeById(id));
  }

}
