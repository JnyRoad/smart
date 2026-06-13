package com.tce.smart.platform.core.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tce.smart.platform.api.dto.req.DeviceAuthVehicleReqDTO;
import com.tce.smart.platform.api.dto.resp.DeviceAuthVehicleRespDTO;
import com.tce.smart.platform.core.dto.DeviceDataDTO;
import com.tce.smart.platform.core.dto.DeviceVehicleDTO;
import com.tce.smart.platform.core.entity.SmtDeviceVehicle;
import org.apache.ibatis.annotations.Param;

/**
 * 设备车辆关联
 *
 * @author 王艳勇
 * @date 2019-04-16 16:06:14
 */
public interface SmtDeviceVehicleMapper extends BaseMapper<SmtDeviceVehicle> {

	/**
	 * 关联车辆
	 * @param page
	 * @param deviceDataDTO
	 * @return
	 */
    IPage<DeviceVehicleDTO> getDeviceVehicle(Page page, @Param("query") DeviceDataDTO deviceDataDTO);

	/**
	 * 分页查询设备授权车辆
	 * @param page
	 * @param reqDTO
	 * @return
	 */
    IPage<DeviceAuthVehicleRespDTO> getDeviceAuthVehiclePage(Page page, @Param("query") DeviceAuthVehicleReqDTO reqDTO);
}
