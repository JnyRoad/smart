package com.tce.smart.platform.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.platform.api.dto.resp.device.DeviceFTDTO;
import com.tce.smart.platform.core.dto.DeviceDTO;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/**
 * 设备信息表
 *
 * @author 王艳勇
 * @date 2019-04-15 15:09:27
 */
public interface IDeviceService {

	/**
	 * 添加设备信息
	 * @param entity 设备信息
	 * @return 返回结果
	 */
	boolean saveDevice(DeviceDTO entity);

	/**
	 * 更新设备信息
	 * @param entity 设备信息
	 * @return 返回结果
	 */
	Result updateDevice(DeviceDTO entity);

	/**
	 * 删除设备信息
	 * @param id 设备ID
	 * @return 返回结果
	 */
	Result removeDevice(String id);

	/**
	 * 发起设备状态更新查询
	 */
	void initDeviceStatus();

	/**
	 * 查询设备体温检测设置
	 * @param parkId
	 * @return
	 */
	List<DeviceFTDTO> getDeviceFaceTemperature(Integer parkId);

	/**
	 * 设置设备体温检测
	 * @param deviceFTDTOs
	 * @return
	 */
	Boolean saveDeviceFaceTemperature(List<DeviceFTDTO> deviceFTDTOs);

}
