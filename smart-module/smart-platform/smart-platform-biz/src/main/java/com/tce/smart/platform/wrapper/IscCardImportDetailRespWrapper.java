package com.tce.smart.platform.wrapper;

import cn.hutool.core.bean.BeanUtil;
import com.tce.smart.common.core.wrapper.BaseWrapper;
import com.tce.smart.platform.api.dto.resp.isc.IscCardImportDetailRespDTO;
import com.tce.smart.platform.core.entity.SmtIscCardImportDetail;
import org.springframework.stereotype.Component;

@Component
public class IscCardImportDetailRespWrapper extends BaseWrapper<SmtIscCardImportDetail, IscCardImportDetailRespDTO> {

	@Override
	protected IscCardImportDetailRespDTO warp(SmtIscCardImportDetail detail) {
		IscCardImportDetailRespDTO dto = new IscCardImportDetailRespDTO();
		BeanUtil.copyProperties(detail, dto);
		return dto;
	}
}
