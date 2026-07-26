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
import com.tce.smart.common.security.service.SmartUser;
import com.tce.smart.platform.core.dto.VehicleBlackDTO;
import com.tce.smart.platform.core.entity.SmtVehicleBlack;
import com.tce.smart.platform.core.vo.VehicleBlackVO;
import com.tce.smart.platform.service.SmtVehicleBlackService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

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
	@PreAuthorize("@pms.hasPermission('platform_vehicle_black')")
  public Result<IPage<VehicleBlackVO> > getSmtAlarmPage(Page page, SmtVehicleBlack
		  smtVehicleBlack) {
	  List<Integer> parkIdList = currentAuthorizedParkIds();
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
	@PreAuthorize("@pms.hasPermission('platform_vehicle_black')")
  public Result getById(@PathVariable("id") Integer id){
    SmtVehicleBlack vehicleBlack = requiredAuthorizedRecord(id);
    return new Result <>(vehicleBlack);
  }

  /**
   * 新增车辆黑名单
   * @param smtVehicleBlack 车辆信息
   * @return Result
   */
  @SysLog("新增车辆黑名单")
  @PostMapping("/save")
	@PreAuthorize("@pms.hasPermission('platform_vehicle_black')")
  public Result save(@RequestBody SmtVehicleBlack smtVehicleBlack){
	assertAuthorizedPark(smtVehicleBlack.getParkId());
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
	@PreAuthorize("@pms.hasPermission('platform_vehicle_black')")
  public Result updateById(@RequestBody SmtVehicleBlack smtVehicleBlack){
	SmtVehicleBlack existing = requiredAuthorizedRecord(smtVehicleBlack.getId());
	if (smtVehicleBlack.getParkId() != null && !existing.getParkId().equals(smtVehicleBlack.getParkId())) {
		throw new AccessDeniedException("车辆黑名单不允许跨园区迁移");
	}
	smtVehicleBlack.setParkId(existing.getParkId());
    return new Result <>(smtVehicleBlackService.updateById(smtVehicleBlack));
  }

  /**
   * 通过id删除车辆黑名单
   * @param id id
   * @return Result
   */
  @SysLog("删除车辆黑名单")
  @GetMapping("/delete/{id}")
	@PreAuthorize("@pms.hasPermission('platform_vehicle_black')")
  public Result removeById(@PathVariable Integer id){
	  requiredAuthorizedRecord(id);
    return new Result <>(smtVehicleBlackService.removeById(id));
  }

  /**
   * 根据车牌号查询黑名单车辆
   * @param smtVehicleBlack 车辆信息
   * @return Result
   */
  @SysLog("根据车牌号查询黑名单车辆")
  @PostMapping("/plate")
	@PreAuthorize("@pms.hasPermission('platform_vehicle_black')")
  public Result plate(@RequestBody SmtVehicleBlack smtVehicleBlack){
	  if (smtVehicleBlack.getParkId() != null) {
		  assertAuthorizedPark(smtVehicleBlack.getParkId());
	  }
	  List<Integer> parkIdList = currentAuthorizedParkIds();
	  List<SmtVehicleBlack> smtVehicleList = smtVehicleBlackService.list(Wrappers.<SmtVehicleBlack>query().lambda()
			  .eq(SmtVehicleBlack::getVehiclePlate, StrUtil.removeAll(smtVehicleBlack.getVehiclePlate(), " ").toUpperCase())
			  .eq(ObjectUtil.isNotNull(smtVehicleBlack.getParkId()),SmtVehicleBlack::getParkId,smtVehicleBlack.getParkId())
			  .in(CollectionUtils.isNotEmpty(parkIdList), SmtVehicleBlack::getParkId, parkIdList)
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
	@PreAuthorize("@pms.hasPermission('platform_vehicle_black')")
  public Result deleteBatch(@RequestBody VehicleBlackDTO vehicleBlackDTO){
	  for (Integer id : vehicleBlackDTO.getIds()) {
		  requiredAuthorizedRecord(id);
	  }
	  return new Result<>(smtVehicleBlackService.removeByIds(vehicleBlackDTO.getIds()));
  }

	/** 所有对象级操作均以持久化记录所属园区为准，客户端请求中的园区字段不可信。 */
	private SmtVehicleBlack requiredAuthorizedRecord(Integer id) {
		SmtVehicleBlack record = smtVehicleBlackService.getById(id);
		if (record == null) {
			throw new AccessDeniedException("车辆黑名单记录不存在或无权访问");
		}
		assertAuthorizedPark(record.getParkId());
		return record;
	}

	private void assertAuthorizedPark(String parkId) {
		if (parkId == null) {
			throw new AccessDeniedException("车辆黑名单园区未获授权");
		}
		try {
			if (!currentAuthorizedParkIds().contains(Integer.valueOf(parkId))) {
				throw new AccessDeniedException("车辆黑名单园区未获授权");
			}
		} catch (NumberFormatException exception) {
			throw new AccessDeniedException("车辆黑名单园区未获授权");
		}
	}

	/** 无登录态或无园区授权范围时必须拒绝，不能因空集合省略园区过滤条件。 */
	private List<Integer> currentAuthorizedParkIds() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication == null || !(authentication.getPrincipal() instanceof SmartUser)) {
			throw new AccessDeniedException("车辆黑名单园区未获授权");
		}
		SmartUser user = (SmartUser) authentication.getPrincipal();
		if (CollectionUtils.isEmpty(user.getParkIdList())) {
			throw new AccessDeniedException("车辆黑名单园区未获授权");
		}
		return user.getParkIdList();
	}

}
