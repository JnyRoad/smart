package com.tce.smart.platform.core.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.tce.smart.platform.api.dto.req.DeviceAuthPersonReqDTO;
import com.tce.smart.platform.api.dto.resp.DeviceAuthPersonRespDTO;
import com.tce.smart.platform.core.dto.DeviceDataDTO;
import com.tce.smart.platform.core.entity.SmtDevicePerson;
import com.tce.smart.platform.core.entity.SmtDeviceTask;
import com.tce.smart.platform.core.vo.DeviceTaskPersonVO;
import org.springframework.http.ResponseEntity;

import java.util.List;

/**
 * 设备人员关联
 *
 * @author 王艳勇
 * @date 2019-04-15 15:15:38
 */
public interface SmtDevicePersonService extends IService<SmtDevicePerson> {

    /**
     * 查询设备绑定人员信息
     * @param page 分页对象
     * @param devicePersonDTO 查询条件
     * @return 返回设备集合
     */
    IPage getDevicePerson(Page page, DeviceDataDTO devicePersonDTO);

	/**
	 * 分页查询授权人员
	 * @param page
	 * @param authPersonReqDTO
	 * @return
	 */
	IPage<DeviceAuthPersonRespDTO> getDeviceAuthPerson(Page page, DeviceAuthPersonReqDTO authPersonReqDTO);

	/**
	 * 导出授权人员
	 * @return
	 */
	ResponseEntity<byte[]> exportAuthPerson();

	/**
	 * 获取绑定人员信息
	 * @param smtDeviceTask 查询条件
	 * @return 人员信息
	 */
	List<SmtDeviceTask> listSmtDeviceTask(SmtDeviceTask smtDeviceTask, List<Integer> parkIds);

	/**
	 * 查询下发记录
	 * @param smtDeviceTask
	 * @return
	 */
	List<DeviceTaskPersonVO> listDownRecord(SmtDeviceTask smtDeviceTask,List<Integer> parkIds);
}
