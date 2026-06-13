package com.tce.smart.platform.wrapper;

import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.tce.smart.common.core.wrapper.BaseWrapper;
import com.tce.smart.platform.core.dto.DevicePersonDTO;
import com.tce.smart.platform.core.vo.DevicePersonVO;
import com.tce.smart.platform.service.ImageService;
import com.tce.smart.tool.constant.DeviceTaskConstants;
import com.tce.smart.tool.constant.SymbolConstants;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 *
 */
@Component
@AllArgsConstructor
public class DevicePersonVOWrapper extends BaseWrapper<DevicePersonDTO, DevicePersonVO> {
	private final ImageService imageService;
    @Override
    protected DevicePersonVO warp(DevicePersonDTO devicePersonDTO) throws IOException {
		DevicePersonVO devicePerson = new DevicePersonVO();
		devicePerson.setCardNo(devicePersonDTO.getCardNo());
		String[] idsArray = StringUtils.split(devicePersonDTO.getPersonName(), SymbolConstants.MINUS);
		if(idsArray.length > 1) {
			devicePerson.setBadge(idsArray[0]);
			devicePerson.setName(idsArray[1]);
		}else {
			devicePerson.setName(idsArray[0]);
		}
		devicePerson.setFaceImage(imageService.buildImageUrl(devicePersonDTO.getImageId()));
		devicePerson.setStatus(DeviceTaskConstants.DEL.equals(devicePersonDTO.getAction()) ? DeviceTaskConstants.DELING : DeviceTaskConstants.NORMAL);
		devicePerson.setCreateTime(DateUtil.format(devicePersonDTO.getCreateTime(),"yyyy-MM-dd HH:mm"));
        return devicePerson;
    }
}
