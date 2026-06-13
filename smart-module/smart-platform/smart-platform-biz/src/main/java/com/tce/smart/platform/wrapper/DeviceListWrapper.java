package com.tce.smart.platform.wrapper;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import com.tce.smart.common.core.util.BeanUtils;
import com.tce.smart.common.core.wrapper.BaseWrapper;
import com.tce.smart.platform.api.dto.resp.DeviceListRespDTO;
import com.tce.smart.platform.api.dto.resp.DeviceTagListDTO;
import com.tce.smart.platform.core.entity.SmtDeviceTag;
import com.tce.smart.platform.core.vo.DeviceVO;
import com.tce.smart.platform.service.SmtDeviceTagService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author sunfujian
 * @date 2021/8/5 15:45
 */
@Component
@AllArgsConstructor
public class DeviceListWrapper extends BaseWrapper<DeviceVO, DeviceListRespDTO> {

	private final SmtDeviceTagService deviceTagService;

	@Override
	protected DeviceListRespDTO warp(DeviceVO deviceVO) throws IOException {
		DeviceListRespDTO deviceListRespDTO = BeanUtil.toBean(deviceVO, DeviceListRespDTO.class);
		List<SmtDeviceTag> deviceTagList = deviceTagService.getByDeviceId(deviceVO.getId());
		if (CollectionUtil.isNotEmpty(deviceTagList)) {
			deviceListRespDTO.setDeviceTagList(BeanUtils.batchTransform(DeviceTagListDTO.class, deviceTagList));
			deviceListRespDTO.setTagIds(deviceTagList.stream().map(SmtDeviceTag::getId).collect(Collectors.toList()));
		}
		return deviceListRespDTO;
	}
}
