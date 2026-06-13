package com.tce.smart.app.controller.fore;

import java.util.List;

import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.tce.smart.app.ao.fore.DeviceRegisterAo;
import com.tce.smart.app.api.entity.AppUserDevice;
import com.tce.smart.app.service.fore.DeviceManageService;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.core.wrapper.BaseController;

import lombok.AllArgsConstructor;

/**
 * 设备管理控制器
 *
 * @author mkwu
 * @date 2019-07-03
 */
@RestController
@AllArgsConstructor
@RequestMapping("/device")
@Slf4j
public class DeviceManageController extends BaseController {

	private DeviceManageService deviceManageService;

	/**
	 * 设备注册
	 *
	 * @param deviceRegisterAo 设备注册消息
	 * @return 成功-true
	 */
	@PostMapping("/register")
	public Result<Boolean> getBaseinfo(@RequestBody DeviceRegisterAo deviceRegisterAo) {
		return success(deviceManageService.handleBaseinfo(deviceRegisterAo));
	}

	/**
	 * 查找用户设备信息
	 *
	 * @param badge 员工号
	 * @return 用户设备信息
	 */
	@HystrixCommand(fallbackMethod = "queryUserDeviceFallback")
	@GetMapping("/query/list")
	public Result<List<AppUserDevice>> queryUserDevice(@RequestParam("badge") String badge) {
		return new Result<>(deviceManageService.getDeviceByBadge(badge));
	}

	/**
	 * 查找用户设备信息-熔断回退方法
	 * @param badge 员工号
	 * @return 用户设备信息
	 */
	public Result<List<AppUserDevice>> queryUserDeviceFallback(@RequestParam("badge") String badge) {
		log.warn("请求queryUserDevice()异常，执行回退方式");
		return new Result<>(deviceManageService.getDeviceByBadge(badge));
	}

	/**
	 * 查找用户默认设备信息
	 *
	 * @param badge 员工号
	 * @return 用户设备信息
	 */
	@GetMapping("/query/list/bind")
	public Result<List<AppUserDevice>> queryBindDevice(@RequestParam("badge") String badge) {
		return new Result<List<AppUserDevice>>(deviceManageService.queryBindDevice(badge));
	}

	/**
	 * 根据设备号查找设备信息
	 *
	 * @param deviceNo 设备号
	 * @return 用户设备信息
	 */
	@GetMapping("/query/list/deviceno")
	public Result<List<AppUserDevice>> queryByDeviceNo(@RequestParam("deviceNo") String deviceNo) {
		return new Result<List<AppUserDevice>>(deviceManageService.queryByDeviceNo(deviceNo));
	}
}
