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
import com.tce.smart.platform.core.entity.SmtParkingCorrection;
import com.tce.smart.platform.service.SmtParkingCorrectionService;

import lombok.AllArgsConstructor;


/**
 * 停车场车位校正表
 *
 * @author 王艳勇
 * @date 2019-04-15 11:31:55
 */
@RestController
@AllArgsConstructor
@RequestMapping("/parking/correction")
public class SmtParkingCorrectionController {

  private final  SmtParkingCorrectionService smtParkingCorrectionService;

  /**
   * 分页查询
   * @param page 分页对象
   * @param smtParkingCorrection 停车场车位校正表
   * @return
   */
  @GetMapping("/page")
  public Result getSmtParkingCorrectionPage(Page page, SmtParkingCorrection smtParkingCorrection) {
    return  new Result <>(smtParkingCorrectionService.page(page,Wrappers.query(smtParkingCorrection)));
  }


  /**
   * 通过id查询停车场车位校正表
   * @param id id
   * @return Result
   */
  @GetMapping("/{id}")
  public Result getById(@PathVariable("id") Integer id){
    return new Result <>(smtParkingCorrectionService.getById(id));
  }

  /**
   * 新增停车场车位校正表
   * @param smtParkingCorrection 停车场车位校正表
   * @return Result
   */
  @SysLog("新增停车场车位校正表")
  @PostMapping("/save")
  public Result save(@RequestBody SmtParkingCorrection smtParkingCorrection){
    return new Result <>(smtParkingCorrectionService.save(smtParkingCorrection));
  }

  /**
   * 修改停车场车位校正表
   * @param smtParkingCorrection 停车场车位校正表
   * @return Result
   */
  @SysLog("修改停车场车位校正表")
  @PostMapping("/update")
  public Result updateById(@RequestBody SmtParkingCorrection smtParkingCorrection){
    return new Result <>(smtParkingCorrectionService.updateById(smtParkingCorrection));
  }

  /**
   * 通过id删除停车场车位校正表
   * @param id id
   * @return Result
   */
  @SysLog("删除停车场车位校正表")
  @PostMapping("/{id}")
  public Result removeById(@PathVariable Integer id){
    return new Result <>(smtParkingCorrectionService.removeById(id));
  }

  /**
   * 新增或更新停车场车位校正表
   * @param smtParkingCorrection 停车场车位校正表
   * @return Result
   */
  @SysLog("新增或更新停车场车位校正表")
  @PostMapping("/saveorupdate")
  public Result saveOrUpdate(@RequestBody SmtParkingCorrection smtParkingCorrection){
    return new Result <>(smtParkingCorrectionService.saveOrUpdateParkingCorrection(smtParkingCorrection));
  }



  /**
   * 获取车位统计信息
   * @return Result
   */
//  @SysLog("获取车位统计信息")
//  @GetMapping("/count")
//  public Result getParkingCountInfo(@RequestParam Integer parkId) {
//	return new Result <>(smtParkingCorrectionService.getParkingCountInfo(parkId));
//  }

}
