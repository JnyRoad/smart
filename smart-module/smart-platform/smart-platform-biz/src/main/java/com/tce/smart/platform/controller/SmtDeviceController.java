package com.tce.smart.platform.controller;

import cn.hutool.core.util.ArrayUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.core.wrapper.BaseController;
import com.tce.smart.common.log.annotation.SysLog;
import com.tce.smart.common.security.util.SecurityUtils;
import com.tce.smart.platform.api.dto.resp.DeviceListRespDTO;
import com.tce.smart.platform.api.dto.resp.device.DeviceFTDTO;
import com.tce.smart.platform.core.dto.DeviceDTO;
import com.tce.smart.platform.core.entity.SmtDevice;
import com.tce.smart.platform.core.service.SmtDeviceService;
import com.tce.smart.platform.service.IDeviceService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;


/**
 * 设备信息表
 *
 * @author 王艳勇
 * @date 2019-04-15 15:09:27
 */
@Api(tags = "设备管理")
@Slf4j
@RestController
@RequestMapping("/device")
public class SmtDeviceController extends BaseController {

	@Resource
	private SmtDeviceService smtDeviceService;

	@Resource
	private IDeviceService bizDeviceService;

	/**
	 * 分页查询
	 *
	 * @param page      分页对象
	 * @param deviceDTO 设备信息表
	 * @return
	 */
	@GetMapping("/page")
	public Result<IPage<DeviceListRespDTO>> getSmtDevicePage(Page page, DeviceDTO deviceDTO,
															 @RequestParam(value = "areaIdArray[]", required = false) Integer[] areaIdArray,
															 @RequestParam(value = "tagsArray[]", required = false) String[] tagsArray) {
		List<Integer> parkIds = SecurityUtils.getUser().getParkIdList();
		deviceDTO.setParkIds(parkIds);
		if(ArrayUtil.isNotEmpty(tagsArray) && tagsArray.length > 0) {
			List<Long> tagsList = new ArrayList<>();
			for(String str : tagsArray){
				tagsList.add(Long.parseLong(str));
			}
			deviceDTO.setTags(tagsList);
		}
		if (ArrayUtil.isNotEmpty(areaIdArray) && areaIdArray.length > 0) {
			switch (areaIdArray.length) {
				case 1:
					deviceDTO.setParkId(areaIdArray[0]);
					break;
				case 2:
					deviceDTO.setParkId(areaIdArray[0]);
					deviceDTO.setParAreaId(areaIdArray[1]);
					break;
				case 3:
					deviceDTO.setParkId(areaIdArray[0]);
					deviceDTO.setParAreaId(areaIdArray[1]);
					deviceDTO.setAreaId(areaIdArray[2]);
					break;
				default:
					break;
			}
		}
		return success(smtDeviceService.getDevice(page, deviceDTO), DeviceListRespDTO.class);
	}


	/**
	 * 通过id查询设备信息表
	 *
	 * @param id id
	 * @return Result
	 */
	@GetMapping("/{id}")
	public Result<DeviceListRespDTO> getById(@PathVariable("id") String id) {
		return success(smtDeviceService.getDeviceById(id), DeviceListRespDTO.class);
	}

	/**
	 * 新增设备信息表
	 *
	 * @param deviceDTO 设备信息表
	 * @return Result
	 */
	@SysLog("新增设备信息表")
	@PostMapping("/save")
	public Result<Boolean> save(@RequestBody DeviceDTO deviceDTO) {
		return success(bizDeviceService.saveDevice(deviceDTO));
	}

	/**
	 * 修改设备信息表
	 *
	 * @param smtDevice 设备信息表
	 * @return Result
	 */
	@SysLog("修改设备信息表")
	@PostMapping("/update")
	public Result updateById(@RequestBody DeviceDTO smtDevice) {
		return bizDeviceService.updateDevice(smtDevice);
	}

	/**
	 * 通过id删除设备信息表
	 *
	 * @param id id
	 * @return Result
	 */
	@SysLog("删除设备信息表")
	@GetMapping("/delete/{id}")
	public Result removeById(@PathVariable String id) {
		return new Result<>(smtDeviceService.deleteDevice(id));
	}


	/**
	 * 更新设备状态信息表
	 *
	 * @param entity 设备信息
	 * @return Result
	 */
	@SysLog("更新设备状态信息表")
	@PostMapping("/update/status")
	public Result updateDeviceStatus(@RequestBody SmtDevice entity) {
		return success(smtDeviceService.updateDeviceStatus(entity));
	}

	//园区分发
//  @SysLog("更新设备状态信息表")
//  @PostMapping("/update/status")
//  public Result updateDeviceStatus(@RequestBody BridgeDTO<String> bridgeDTO){
//	return success(smtDeviceService.updateDeviceStatus(bridgeDTO));
//  }


	/**
	 * 通过区域id查询设备信息表
	 *
	 * @param id 区域ID
	 * @return Result
	 */
	@GetMapping("/area/{id}")
	public Result getByAreaId(@PathVariable("id") Integer id) {
		return new Result<>(smtDeviceService.getByAreaId(id));
	}

	/**
	 * 通过区域id查询设备信息表
	 *
	 * @return Result
	 */
	@GetMapping("/area/tree")
	public Result getTree() {
		List<Integer> parkIds = SecurityUtils.getUser().getParkIdList();
		return new Result<>(smtDeviceService.getTree(parkIds));
	}


	/**
	 * 获取园区信息
	 *
	 * @return Result
	 */
	@GetMapping("/park")
	public Result park() {
		List<Integer> parkIds = SecurityUtils.getUser().getParkIdList();
		return new Result<>(smtDeviceService.getParkList(parkIds));
	}

	/**
	 * 发起设备状态更新查询
	 *
	 * @return Result
	 */
	@GetMapping("/query/deviceStatus")
	public Result deviceStatus() {
		bizDeviceService.initDeviceStatus();
		return new Result<>();
	}

	/**
	 * 查询设备体温检测配置
	 *
	 * @return Result
	 */
	@GetMapping("/query/deviceFT/{parkId}")
	@ApiOperation("查询设备体温检测配置")
	public Result<List<DeviceFTDTO>> getDeviceFaceTemperature(@ApiParam(name = "parkId", value = "园区id", required = true) @PathVariable Integer parkId) {
		return new Result<>(bizDeviceService.getDeviceFaceTemperature(parkId));
	}

	/**
	 * 设置设备体温检测配置
	 *
	 * @return Result
	 */
	@PostMapping("/set/deviceFT")
	@ApiOperation("设置设备体温检测配置")
	public Result<Boolean> saveDeviceFaceTemperature(@RequestBody List<DeviceFTDTO> deviceFTDTOs) {
		return new Result<>(bizDeviceService.saveDeviceFaceTemperature(deviceFTDTOs));
	}

	@PostMapping("/auth/clear/{deviceId}")
	@ApiOperation("一键清空设备上的授权信息")
	public Result<Boolean> clearAuth(@PathVariable("deviceId") String deviceId) {
		return success(smtDeviceService.clearAuth(deviceId));
	}

	@GetMapping("/auth/repeat/{deviceId}")
	@ApiOperation("重新下发")
	public Result<Boolean> repeatAuth(@PathVariable("deviceId") String deviceId) {
		return success(smtDeviceService.repeatAuth(deviceId));
	}
}
