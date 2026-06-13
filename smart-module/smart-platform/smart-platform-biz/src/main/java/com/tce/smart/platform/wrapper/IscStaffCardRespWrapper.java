package com.tce.smart.platform.wrapper;

import cn.hutool.core.bean.BeanUtil;
import com.tce.smart.common.core.wrapper.BaseWrapper;
import com.tce.smart.platform.api.dto.resp.isc.IscStaffCardRespDTO;
import com.tce.smart.platform.core.entity.SmtIscStaffCard;
import org.springframework.stereotype.Component;

@Component
public class IscStaffCardRespWrapper extends BaseWrapper<SmtIscStaffCard, IscStaffCardRespDTO> {

	@Override
	protected IscStaffCardRespDTO warp(SmtIscStaffCard card) {
		IscStaffCardRespDTO dto = new IscStaffCardRespDTO();
		BeanUtil.copyProperties(card, dto);
		return dto;
	}
}
