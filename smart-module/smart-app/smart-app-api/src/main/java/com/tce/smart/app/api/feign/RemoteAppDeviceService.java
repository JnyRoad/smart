package com.tce.smart.app.api.feign;

import java.util.List;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.tce.smart.app.api.entity.AppUserDevice;
import com.tce.smart.common.core.constant.ServiceNameConstants;
import com.tce.smart.common.core.model.Result;

/**
 * App设备信息
 *
 * @author mkwu
 * @date 2019-07-05
 */
@FeignClient(value = ServiceNameConstants.APP_SERVICE)
public interface RemoteAppDeviceService {

	/**
	 * 查找用户设备信息
	 *
	 * @param badge 员工号
	 * @return 用户设备信息
	 */
	@GetMapping("/device/query/list")
	Result<List<AppUserDevice>> queryUserDevice(@RequestParam("badge") String badge);

	/**
	 * 查找已绑定的设备信息
	 *
	 * @param badge 员工号
	 * @return 用户设备信息
	 */
	@GetMapping("/device/query/list/bind")
	Result<List<AppUserDevice>> queryBindDevice(@RequestParam("badge") String badge);

	/**
	 * 根据设备号查找设备信息
	 *
	 * @param deviceNo 设备号
	 * @return 用户设备信息
	 */
	@GetMapping("/device/query/list/deviceno")
	Result<List<AppUserDevice>> queryByDeviceNo(@RequestParam("deviceNo") String deviceNo);
}
