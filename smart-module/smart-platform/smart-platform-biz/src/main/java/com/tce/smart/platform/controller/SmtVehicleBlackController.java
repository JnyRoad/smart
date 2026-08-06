package com.tce.smart.platform.controller;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.core.wrapper.BaseController;
import com.tce.smart.common.log.annotation.SysLog;
import com.tce.smart.common.security.util.SecurityUtils;
import com.tce.smart.platform.core.dto.VehicleBlackDTO;
import com.tce.smart.platform.core.entity.SmtVehicleBlack;
import com.tce.smart.platform.core.vo.VehicleBlackVO;
import com.tce.smart.platform.service.SmtVehicleBlackService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;


/**
 * 车辆黑名单
 *
 * @author 王艳勇
 * @date 2019-04-15 11:34:58
 */
@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping("/vehicle/black")
public class SmtVehicleBlackController extends BaseController {

  private final SmtVehicleBlackService smtVehicleBlackService;

  /**
   * 分页查询
   * @param page 分页对象
   * @param smtVehicleBlack 车辆信息
   * @return
   */
  @GetMapping("/page")
  public Result<IPage<VehicleBlackVO> > getSmtAlarmPage(Page page, SmtVehicleBlack
		  smtVehicleBlack) {
	  List<Integer> parkIdList = SecurityUtils.getUser().getParkIdList();
	  IPage<SmtVehicleBlack> pageRe= smtVehicleBlackService.page(page,Wrappers.<SmtVehicleBlack>query().lambda()
					  .eq(Objects.nonNull(smtVehicleBlack.getParkId()), SmtVehicleBlack::getParkId, smtVehicleBlack.getParkId())
					  .in(CollectionUtils.isNotEmpty(parkIdList), SmtVehicleBlack::getParkId, parkIdList));
	  return success(pageRe, VehicleBlackVO.class);
  }


  /**
   * 通过id查询车辆黑名单
   * @param id id
   * @return Result
   */
  @GetMapping("/{id}")
  public Result getById(@PathVariable("id") Integer id){
    return new Result <>(smtVehicleBlackService.getById(id));
  }

  /**
   * 新增车辆黑名单
   * @param smtVehicleBlack 车辆信息
   * @return Result
   */
  @SysLog("新增车辆黑名单")
  @PostMapping("/save")
  public Result save(@RequestBody SmtVehicleBlack smtVehicleBlack){
	smtVehicleBlack.setCreateTime(LocalDateTime.now());
    return new Result <>(smtVehicleBlackService.save(smtVehicleBlack));
  }

  /**
   * 修改车辆黑名单
   * @param smtVehicleBlack 车辆信息
   * @return Result
   */
  @SysLog("修改车辆黑名单")
  @PostMapping("/update")
  public Result updateById(@RequestBody SmtVehicleBlack smtVehicleBlack){
	log.info("smtVehicleBlack:{}",smtVehicleBlack);
    return new Result <>(smtVehicleBlackService.updateById(smtVehicleBlack));
  }

  /**
   * 通过id删除车辆黑名单
   * @param id id
   * @return Result
   */
  @SysLog("删除车辆黑名单")
  @GetMapping("/delete/{id}")
  public Result removeById(@PathVariable Integer id){
    return new Result <>(smtVehicleBlackService.removeById(id));
  }

  /**
   * 根据车牌号查询黑名单车辆
   * @param smtVehicleBlack 车辆信息
   * @return Result
   */
  @SysLog("根据车牌号查询黑名单车辆")
  @PostMapping("/plate")
  public Result plate(@RequestBody SmtVehicleBlack smtVehicleBlack){
	  List<SmtVehicleBlack> smtVehicleList = smtVehicleBlackService.list(Wrappers.<SmtVehicleBlack>query().lambda()
			  .eq(SmtVehicleBlack::getVehiclePlate, StrUtil.removeAll(smtVehicleBlack.getVehiclePlate(), " ").toUpperCase())
			  .eq(ObjectUtil.isNotNull(smtVehicleBlack.getParkId()),SmtVehicleBlack::getParkId,smtVehicleBlack.getParkId())
			  .ne(ObjectUtil.isNotNull(smtVehicleBlack.getId()), SmtVehicleBlack::getId,smtVehicleBlack.getId()));
	  return new Result<>(CollUtil.isNotEmpty(smtVehicleList));
  }

	/**
	 * 批量删除
	 * @param vehicleBlackDTO ids
	 * @return
	 */
	@SysLog("批量删除黑名单车辆")
	@PostMapping("/delete/batch")
  public Result deleteBatch(@RequestBody VehicleBlackDTO vehicleBlackDTO){
	  return new Result<>(smtVehicleBlackService.removeByIds(vehicleBlackDTO.getIds()));
  }

}
