package com.tce.smart.platform.wrapper;

import com.tce.smart.common.core.util.BeanUtils;
import com.tce.smart.common.core.util.StringUtils;
import com.tce.smart.common.core.wrapper.BaseWrapper;
import com.tce.smart.platform.api.dto.resp.DeviceTaskInfoRespDTO;
import com.tce.smart.platform.core.entity.SmtDevice;
import com.tce.smart.platform.core.entity.SmtDeviceTask;
import com.tce.smart.platform.core.entity.SmtDeviceTaskDetail;
import com.tce.smart.platform.core.entity.SmtIscDeviceTask;
import com.tce.smart.platform.core.service.SmtDeviceService;
import com.tce.smart.platform.core.service.SmtDeviceTaskService;
import com.tce.smart.platform.core.service.SmtIscDeviceTaskService;
import com.tce.smart.platform.core.vo.DeviceVO;
import com.tce.smart.tool.enums.DeviceTaskActionEnum;
import com.tce.smart.tool.enums.DeviceTaskStatusEnum;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import java.io.IOException;
import java.util.Objects;


@Component
@AllArgsConstructor
public class DeviceTaskInfoWrapper extends BaseWrapper<SmtDeviceTaskDetail, DeviceTaskInfoRespDTO> {

	private final SmtDeviceService smtDeviceService;

	private final SmtDeviceTaskService smtDeviceTaskService;

	private final SmtIscDeviceTaskService smtIscDeviceTaskService;

	@Override
	protected DeviceTaskInfoRespDTO warp(SmtDeviceTaskDetail smtDeviceTaskDetail) throws IOException {
		DeviceTaskInfoRespDTO deviceTaskInfoRespDTO = BeanUtils.transform(DeviceTaskInfoRespDTO.class, smtDeviceTaskDetail);
		deviceTaskInfoRespDTO.setStatusDesc(DeviceTaskStatusEnum.desc(smtDeviceTaskDetail.getStatus()));
		deviceTaskInfoRespDTO.setActionDesc(DeviceTaskActionEnum.DOWN.getDesc());
		String taskId = smtDeviceTaskDetail.getTaskId();
		if(StringUtils.isNotEmpty(taskId)) {
			SmtIscDeviceTask iscTask = smtIscDeviceTaskService.getById(taskId);
			if(Objects.isNull(iscTask)) {
				SmtDeviceTask task = smtDeviceTaskService.getById(taskId);
				if (Objects.isNull(task)) {
					return deviceTaskInfoRespDTO;
				}
				DeviceVO device = smtDeviceService.getDeviceById(task.getDeviceCode());
				if (Objects.nonNull(device)) {
					if (StringUtils.isNotEmpty(device.getDeviceName())) {
						deviceTaskInfoRespDTO.setDeviceName(device.getDeviceName());
					}
					if (StringUtils.isNotEmpty(device.getAreaName())) {
						deviceTaskInfoRespDTO.setAreaName(device.getAreaName());
					}
				}
				deviceTaskInfoRespDTO.setStatus(task.getStatus());
				deviceTaskInfoRespDTO.setStatusDesc(DeviceTaskStatusEnum.desc(task.getStatus()));
				deviceTaskInfoRespDTO.setActionDesc(DeviceTaskActionEnum.desc(task.getAction()));
				return deviceTaskInfoRespDTO;
			}
			DeviceVO device = smtDeviceService.getDeviceById(iscTask.getDeviceCode());
			if (Objects.nonNull(device)) {
				if (StringUtils.isNotEmpty(device.getDeviceName())) {
					deviceTaskInfoRespDTO.setDeviceName(device.getDeviceName());
				}
				if (StringUtils.isNotEmpty(device.getAreaName())) {
					deviceTaskInfoRespDTO.setAreaName(device.getAreaName());
				}
			}
			deviceTaskInfoRespDTO.setStatus(iscTask.getStatus());
			deviceTaskInfoRespDTO.setStatusDesc(DeviceTaskStatusEnum.desc(iscTask.getStatus()));
			deviceTaskInfoRespDTO.setActionDesc(DeviceTaskActionEnum.desc(iscTask.getAction()));
		}
		return deviceTaskInfoRespDTO;
	}
}
