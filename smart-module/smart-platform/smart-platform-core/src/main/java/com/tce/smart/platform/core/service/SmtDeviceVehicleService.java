package com.tce.smart.platform.core.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.tce.smart.platform.api.dto.req.DeviceAuthVehicleReqDTO;
import com.tce.smart.platform.api.dto.resp.DeviceAuthVehicleRespDTO;
import com.tce.smart.platform.core.dto.DeviceDataDTO;
import com.tce.smart.platform.core.entity.SmtDeviceTask;
import com.tce.smart.platform.core.entity.SmtDeviceVehicle;
import org.springframework.http.ResponseEntity;

import java.util.List;

/**
 * 设备车辆关联
 *
 * @author 王艳勇
 * @date 2019-04-16 16:06:14
 */
public interface SmtDeviceVehicleService extends IService<SmtDeviceVehicle> {

    /**
     * 查询设备绑定车辆信息
     * @param page 分页对象
     * @param deviceDataDTO 查询条件
     * @return 返回设备集合
     */
    IPage getDeviceVehicle(Page page, DeviceDataDTO deviceDataDTO);

	/**
	 * 授权车辆导出
	 * @return
	 */
	ResponseEntity<byte[]> exportAuthPerson();

	/**
	 * 分页查询授权车辆
	 * @param page
	 * @param reqDTO
	 * @return
	 */
	IPage<DeviceAuthVehicleRespDTO> getDeviceAuthVehicle(Page page, DeviceAuthVehicleReqDTO reqDTO);

	/**
	 * 获取绑定车辆信息
	 * @param smtDeviceTask 查询条件
	 * @return 车辆信息
	 */
	List<SmtDeviceTask> listSmtDeviceTask(SmtDeviceTask smtDeviceTask, List<Integer> list);
}
