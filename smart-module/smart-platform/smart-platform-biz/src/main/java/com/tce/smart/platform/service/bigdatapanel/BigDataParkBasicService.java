package com.tce.smart.platform.service.bigdatapanel;

import com.tce.smart.platform.api.dto.resp.bigdatapanel.AreaDeviceSnapRespDTO;
import com.tce.smart.platform.api.dto.resp.bigdatapanel.ParkDormitoryRespDTO;
import com.tce.smart.platform.api.dto.resp.bigdatapanel.ParkParkingRespDTO;
import com.tce.smart.platform.api.dto.resp.bigdatapanel.ParkVisitorRespDTO;

import java.util.List;

/**
 * @description: 大数据面板-园区总览Service
 * @date: 2020-08-05 13:58
 * @author: wuling
 * @version: 1.0
 */
public interface BigDataParkBasicService {

	/**
	 * 大数据面板-获取宿舍动态
	 * @return
	 */
	ParkDormitoryRespDTO getParkDormitoryInfo(Integer parkId);


	/**
	 * 大数据面板-获取车位动态
	 * @return
	 */
	ParkParkingRespDTO getParkParkingInfo(Integer parkId);

	/**
	 * 获取今天的访客抓拍数据
	 * @return
	 */
	ParkVisitorRespDTO getParkVisitorInfo(Integer parkId);

	/**
	 * 获取区域设备最近抓拍数据
	 * @return
	 */
	List<AreaDeviceSnapRespDTO> getAreaDeviceSnapData(Integer parkId);
}
