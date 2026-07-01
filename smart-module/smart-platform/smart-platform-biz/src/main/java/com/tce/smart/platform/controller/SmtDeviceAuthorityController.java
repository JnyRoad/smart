package com.tce.smart.platform.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.log.annotation.SysLog;
import com.tce.smart.common.security.util.SecurityUtils;
import com.tce.smart.platform.api.dto.req.AreaTypeSwitchReqDTO;
import com.tce.smart.platform.api.dto.req.AuthDetailQueryDTO;
import com.tce.smart.platform.api.dto.req.DeviceAuthRelationAddReqDTO;
import com.tce.smart.platform.api.dto.req.DeviceAuthRelationDelReqDTO;
import com.tce.smart.platform.api.dto.resp.AreaTypeSwitchRespDTO;
import com.tce.smart.platform.api.dto.resp.AuthDetailRespDTO;
import com.tce.smart.platform.core.dto.SmtDeviceAuthorityDTO;
import com.tce.smart.platform.core.entity.SmtDeviceAuthority;
import com.tce.smart.platform.service.SmtDeviceAuthorityService;
import io.swagger.annotations.Api;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;


/**
 * 设备权限表
 *
 * @author 王艳勇
 * @date 2019-04-15 15:15:34
 */
@RestController
@AllArgsConstructor
@Api(tags = "platform-设备权限表")
@RequestMapping("/device/authority")
public class SmtDeviceAuthorityController {

	private final SmtDeviceAuthorityService smtDeviceAuthorityService;

	/**
	 * 分页查询
	 *
	 * @param page               分页对象
	 * @param smtDeviceAuthority 设备权限表
	 * @return
	 */
	@GetMapping("/page")
	public Result getSmtDeviceAuthorityPage(Page page, SmtDeviceAuthority smtDeviceAuthority) {
		return new Result<>(smtDeviceAuthorityService.page(page, smtDeviceAuthority));
	}


	/**
	 * 通过id查询设备权限表
	 *
	 * @param id id
	 * @return Result
	 */
	@GetMapping("/{id}")
	public Result getById(@PathVariable("id") Integer id) {
		List<Integer> parkIds = SecurityUtils.getUser().getParkIdList();
		return new Result<>(smtDeviceAuthorityService.getDeviceAuthorityById(id, parkIds));
	}

	/**
	 * 新增设备权限表
	 *
	 * @param smtDeviceAuthorityDTO 设备权限表
	 * @return Result
	 */
	@SysLog("新增设备权限表")
	@PostMapping("/save")
	public Result save(@RequestBody SmtDeviceAuthorityDTO smtDeviceAuthorityDTO) {
		return new Result<>(smtDeviceAuthorityService.saveDeviceAuthority(smtDeviceAuthorityDTO));
	}

	/**
	 * 修改设备权限表
	 *
	 * @param smtDeviceAuthorityDTO 设备权限表
	 * @return Result
	 */
	@SysLog("修改设备权限表")
	@PostMapping("/update")
	public Result updateById(@RequestBody SmtDeviceAuthorityDTO smtDeviceAuthorityDTO) {
		List<Integer> parkIds = SecurityUtils.getUser().getParkIdList();
		return new Result<>(smtDeviceAuthorityService.updateDeviceAuthority(smtDeviceAuthorityDTO, parkIds));
	}

	/**
	 * 通过id删除设备权限表
	 *
	 * @param id id
	 * @return Result
	 */
	@SysLog("删除设备权限表")
	@GetMapping("/delete/{id}")
	public Result removeById(@PathVariable Integer id) {
		List<Integer> parkIds = SecurityUtils.getUser().getParkIdList();
		return smtDeviceAuthorityService.deleteDeviceAuthority(id, parkIds);
	}

	/**
	 * 通过区域id查询设备信息表
	 *
	 * @param type 区域ID
	 * @return Result
	 */
	@GetMapping("/tree/{type}")
	public Result getTree(@PathVariable("type") Integer type) {
		List<Integer> parkIds = SecurityUtils.getUser().getParkIdList();
		return new Result<>(smtDeviceAuthorityService.getTree(type, parkIds));
	}

	@GetMapping("/list/{type}/{parkId}")
	public Result list(@PathVariable("type") Integer type, @PathVariable("parkId") Integer parkId,
					   @RequestParam(value = "authorityName", required = false) String authorityName) {
		return new Result<>(smtDeviceAuthorityService.list(type, parkId, authorityName));
	}
//
//	@GetMapping("/list/byName")
//	public Result listByName(@RequestParam("type") Integer type,
//							 @RequestParam("parkId") Integer parkId,@RequestParam(value = "name", required = false) String name) {
//		return new Result<>(smtDeviceAuthorityService.list(type, parkId));
//	}

	@GetMapping("/list/all/{parkId}")
	public Result list(@PathVariable("parkId") Integer parkId) {
		return new Result<>(smtDeviceAuthorityService.listAll(parkId));
	}

	//  /**
	//   * 通过区域id查询设备信息表
	//   * @param type 区域ID
	//   * @param parkId 园区ID
	//   * @return Result
	//   */
	@GetMapping("/tree/{type}/{parkId}")
	public Result getTree(@PathVariable("type") Integer type, @PathVariable("parkId") Integer parkId) {
		List<Integer> parkIds = new ArrayList<>();
		parkIds.add(parkId);
		return new Result<>(smtDeviceAuthorityService.getTree(type, parkIds));
	}

	/**
	 * 通过园区ID查询门禁和闸机设备
	 *
	 * @param parkId
	 *
	 * @return
	 */
	@GetMapping("/person/tree/{parkId}")
	public Result getPersonTree(@PathVariable("parkId") Integer parkId) {
		List<Integer> parkIds = new ArrayList<>();
		parkIds.add(parkId);
		return new Result<>(smtDeviceAuthorityService.getTrees(parkIds, null));
	}

	/**
	 * 通过园区ID查询门禁和闸机设备
	 *
	 * @param parkId
	 * @param areaType
	 *
	 * @return
	 */
	@GetMapping("/person/tree/{parkId}/{areaType}")
	public Result getPersonTree(@PathVariable("parkId") Integer parkId, @PathVariable(value = "areaType") Integer areaType) {
		List<Integer> parkIds = new ArrayList<>();
		parkIds.add(parkId);
		return new Result<>(smtDeviceAuthorityService.getTrees(parkIds, areaType));
	}

	/**
	 * 获取通关权限关联的人员及车辆信息
	 *
	 * @param page
	 * @param queryDTO
	 * @return
	 */
	@PostMapping("/detail/page")
	public Result<IPage<AuthDetailRespDTO>> getAuthDetailPage(Page page, @RequestBody AuthDetailQueryDTO queryDTO) {
		return new Result<>(smtDeviceAuthorityService.getAuthDetailPage(page, queryDTO));
	}

	/**
	 * 删除通关权限管理的人员及车辆信息
	 * @param reqDTO
	 * @return
	 */
	@PostMapping("/relation/del")
	public Result<Boolean> deviceAuthRelationDel(@RequestBody DeviceAuthRelationDelReqDTO reqDTO) {
		return new Result<>(smtDeviceAuthorityService.deviceAuthRelationDel(reqDTO));
	}

	/**
	 * 清空通关权限管理的人员及车辆信息
	 * @param id
	 * @return
	 */
	@PostMapping("/relation/clear/{id}")
	public Result<Boolean> deviceAuthRelationClear(@PathVariable("id") Integer id) {
		return new Result<>(smtDeviceAuthorityService.deviceAuthRelationClear(id));
	}

	/**
	 * 通关权限新增关联人员
	 * @param reqDTO
	 * @return
	 */
	@PostMapping("/relation/add")
	public Result<List<String>> deviceAuthRelationAdd(@RequestBody DeviceAuthRelationAddReqDTO reqDTO) {
		return new Result<>(smtDeviceAuthorityService.deviceAuthRelationAdd(reqDTO));
	}

	/**
	 * 变更通关权限性质（公共区域/保密区域）
	 * @param reqDTO 目标权限组ID + 目标性质
	 * @return success=false 时表示存在冲突设备，未写库，data.conflicts 里是冲突明细
	 */
	@SysLog("变更通关权限性质")
	@PostMapping("/areaType/switch")
	public Result<AreaTypeSwitchRespDTO> switchAreaType(@RequestBody AreaTypeSwitchReqDTO reqDTO) {
		List<Integer> parkIds = SecurityUtils.getUser().getParkIdList();
		return smtDeviceAuthorityService.switchAreaType(reqDTO, parkIds);
	}
}
