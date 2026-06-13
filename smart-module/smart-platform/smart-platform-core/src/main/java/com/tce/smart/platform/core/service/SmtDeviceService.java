package com.tce.smart.platform.core.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.tce.smart.platform.core.dto.DeviceDTO;
import com.tce.smart.platform.core.entity.SmtAlarmRecord;
import com.tce.smart.platform.core.entity.SmtDevice;
import com.tce.smart.platform.core.entity.SmtPark;
import com.tce.smart.platform.core.model.AreaTree;
import com.tce.smart.platform.core.vo.DeviceVO;

import java.util.List;

/**
 * 设备信息表
 *
 * @author 王艳勇
 * @date 2019-04-15 15:09:27
 */
public interface SmtDeviceService extends IService<SmtDevice> {

	/**
	 * 查询设备信息
	 * @param page 分页对象
	 * @param deviceDTO 查询条件
	 * @return 返回设备集合
	 */
	IPage<DeviceVO> getDevice(Page page, DeviceDTO deviceDTO);

	/**
	 * 获取设备信息
	 * @param id 设备ID
	 * @return 返回结果
	 */
	DeviceVO getDeviceById(String id);

	/**
	 * 更新设备状态信息
	 * @param entity 设备信息
	 * @return 返回结果
	 */
	Boolean updateDeviceStatus(SmtDevice entity);

	// 园区分发
//	Boolean updateDeviceStatus(BridgeDTO<String> bridgeDTO);

	/**
	 * 警报记录补全设备信息
	 * @param entity 警报记录信息
	 * @return 返回结果
	 */
	void deviceHandle(SmtAlarmRecord entity);

	/**
	 * 根据区域子节点获取设备信息
	 *
	 * @param id 区域ID
	 * @return 设备集合
	 */
	List<SmtDevice> getByAreaId(Integer id);

	/**
	 * 根据区域信息
	 * @param parkIds 园区ID
	 * @return 区域集合信息
	 */
	List<AreaTree> getTree(List<Integer> parkIds);


	List<SmtDevice> selectDeviceByAuthId(Integer code);

	/**
	 * 获取园区信息
	 * @param ids
	 * @return
	 */
	List<SmtPark> getParkList(List<Integer> ids);

	int deleteDeviceArea(String deviceId);

	/**
	 * 获取所有设备用于初始化
	 * @return
	 */
	List<String> getDeviceIds(Integer parkId);

	/**
	 * 一键清空设备上的授权信息
	 * @param deviceId
	 * @return
	 */
	Boolean clearAuth(String deviceId);

	Boolean repeatAuth(String deviceId);

	Boolean deleteDevice(String deviceId);
}
