package com.tce.smart.app.service.fore;

import java.util.List;

import com.tce.smart.app.ao.fore.DeviceRegisterAo;
import com.tce.smart.app.api.entity.AppUserDevice;

/**
 * 设备注册接口
 *
 * @author fushiping
 * @date 2019/7/3 18:07
 **/
public interface DeviceManageService {

	Boolean handleBaseinfo(DeviceRegisterAo deviceRegisterAo);

	/**
	 * 根据员工号查询设备信息
	 *
	 * @param badge 员工号
	 * @return 设备信息列表
	 */
	List<AppUserDevice> getDeviceByBadge(String badge);

	/**
	 * 用户设备绑定
	 *
	 * @param badge    员工号
	 * @param deviceNo 设备编号
	 * @return true-成功,false-失败
	 */
	Boolean bindDevice(String badge, String deviceNo);

	/**
	 * 查询用户绑定设备信息
	 *
	 * @param badge 员工号
	 * @return 设备信息列表
	 */
	List<AppUserDevice> queryBindDevice(String badge);

	/**
	 * 根据设备号查找设备信息
	 * @param deviceNo 设备号
	 * @return 设备信息列表
	 */
	List<AppUserDevice> queryByDeviceNo(String deviceNo);
}
