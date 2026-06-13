package com.tce.smart.platform.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.core.wrapper.BaseController;
import com.tce.smart.common.log.annotation.SysLog;
import com.tce.smart.common.security.util.SecurityUtils;
import com.tce.smart.platform.core.dto.AddSnapVehicleDTO;
import com.tce.smart.platform.core.dto.SnapVehicleAccessDTO;
import com.tce.smart.platform.core.dto.SnapVehicleDTO;
import com.tce.smart.platform.core.entity.SmtSnapVehicle;
import com.tce.smart.platform.core.vo.SnapVehicleDetailVO;
import com.tce.smart.platform.core.vo.SnapVehicleVO;
import com.tce.smart.platform.service.SmtSnapVehicleService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;


/**
 * 车辆抓拍记录表
 *
 * @author 梁圆
 * @date 2019-04-13 18:18:20
 */
@RestController
@AllArgsConstructor
@RequestMapping("/snap/vehicle")
public class SmtSnapVehicleController extends BaseController{

  private final SmtSnapVehicleService smtSnapVehicleService;

  /**
   * 分页查询
   * @param page 分页对象
   * @param snapVehicleDTO 车辆抓拍记录表
   * @return
   */
  @GetMapping("/page")
  public Result getSmtSnapVehiclePage(Page page,SnapVehicleDTO snapVehicleDTO) {
	List<Integer> parkIds = SecurityUtils.getUser().getParkIdList();
	snapVehicleDTO.setParkIds(parkIds);
	IPage pageList = smtSnapVehicleService.getSnapVehicle(page, snapVehicleDTO);
    return success(pageList, SnapVehicleVO.class);
  }

  /**
   * 车辆统计
   * @return
   */
  @GetMapping("/count/{parkId}")
  public Result getVehicleCountBySnapTime(@PathVariable("parkId") Integer parkId) {
	  return success(smtSnapVehicleService.getVehicleCountBySnapTime(parkId));

  }

  /**
   * 通过id查询车辆抓拍记录表
   * @param id id
   * @return Result
   */
  @GetMapping("/{id}")
  public Result getById(@PathVariable("id") Integer id){
    return success(smtSnapVehicleService.getById(id));
  }

  /**
   * 新增车辆抓拍记录表
   * @param smtSnapVehicle 车辆抓拍记录表
   * @return Result
   */
  @SysLog("新增车辆抓拍记录表")
  @PostMapping("/save")
  public Result<Boolean> save(@Valid @RequestBody AddSnapVehicleDTO smtSnapVehicle){
    return new Result<>(smtSnapVehicleService.saveSnapVehicle(smtSnapVehicle));
  }

//  @SysLog("新增车辆抓拍记录表")
//  @PostMapping("/save")
//  public Result<Boolean> save(@RequestBody BridgeDTO<String> bridgeDTO){
//    return new Result<>(smtSnapVehicleService.saveSnapVehicle(bridgeDTO));
//  }

  /**
   * 修改车辆抓拍记录表
   * @param smtSnapVehicle 车辆抓拍记录表
   * @return Result
   */
  @SysLog("修改车辆抓拍记录表")
  @PostMapping
  public Result<Boolean> updateById(@RequestBody SmtSnapVehicle smtSnapVehicle){
    return new Result<>(smtSnapVehicleService.updateById(smtSnapVehicle));
  }

  /**
   * 通过id删除车辆抓拍记录表
   * @param id id
   * @return Result
   */
  @SysLog("删除车辆抓拍记录表")
  @PostMapping("/{id}")
  public Result<Boolean> removeById(@PathVariable Integer id){
    return new Result<>(smtSnapVehicleService.removeById(id));
  }

  /**
   * 车辆出入查询
   * @param page 分页对象
   * @param snapVehicleAccessDto 车辆抓拍记录表
   * @return
   */
  @SuppressWarnings("rawtypes")
  @GetMapping("/searchVehicleAccess")
  public Result searchVehicleAccess(Page page,SnapVehicleAccessDTO snapVehicleAccessDto,@RequestParam(value = "snapTime",required=false) String snapTime) {
	  return  new Result<>(smtSnapVehicleService.searchVehicleAccess(page,snapVehicleAccessDto,snapTime));
  }

  /**
   * 通过id查询车辆出入记录表
   * @param id id
   * @return Result
   */
  @GetMapping("/searchVehicleAccessById/{id}")
  public Result searchVehicleAccessById(@PathVariable("id") Integer id){
	  return new Result<>(smtSnapVehicleService.searchVehicleAccessById(id));
  }
}
