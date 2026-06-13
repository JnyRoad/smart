package com.tce.smart.platform.wrapper;

import cn.hutool.core.bean.BeanUtil;
import com.tce.smart.common.core.wrapper.BaseWrapper;
import com.tce.smart.platform.api.dto.resp.isc.IscCardTaskRespDTO;
import com.tce.smart.platform.core.vo.IscCardTaskPageVO;
import com.tce.smart.tool.enums.DeviceTaskActionEnum;
import com.tce.smart.tool.enums.DeviceTaskStatusEnum;
import org.springframework.stereotype.Component;

@Component
public class IscCardTaskRespWrapper extends BaseWrapper<IscCardTaskPageVO, IscCardTaskRespDTO> {

	@Override
	protected IscCardTaskRespDTO warp(IscCardTaskPageVO task) {
		IscCardTaskRespDTO dto = new IscCardTaskRespDTO();
		BeanUtil.copyProperties(task, dto);
		dto.setActionDesc(actionDesc(task.getAction()));
		dto.setStatusDesc(DeviceTaskStatusEnum.desc(task.getStatus()));
		return dto;
	}

	private String actionDesc(Integer action) {
		if (DeviceTaskActionEnum.DOWN.getCode().equals(action)) {
			return "新增卡片";
		}
		if (DeviceTaskActionEnum.DEL.getCode().equals(action)) {
			return "删除卡片";
		}
		return DeviceTaskActionEnum.desc(action);
	}
}
