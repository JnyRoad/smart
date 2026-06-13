package com.tce.smart.platform.wrapper;

import cn.hutool.core.bean.BeanUtil;
import com.tce.smart.common.core.wrapper.BaseWrapper;
import com.tce.smart.platform.api.dto.resp.isc.IscParkConfigRespDTO;
import com.tce.smart.platform.core.entity.SmtIscParkConfig;
import org.springframework.stereotype.Component;

@Component
public class IscParkConfigRespWrapper extends BaseWrapper<SmtIscParkConfig, IscParkConfigRespDTO> {

	@Override
	protected IscParkConfigRespDTO warp(SmtIscParkConfig config) {
		IscParkConfigRespDTO dto = new IscParkConfigRespDTO();
		BeanUtil.copyProperties(config, dto);
		return dto;
	}
}
