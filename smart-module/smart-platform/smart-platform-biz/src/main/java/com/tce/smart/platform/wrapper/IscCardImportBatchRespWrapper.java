package com.tce.smart.platform.wrapper;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.tce.smart.common.core.wrapper.BaseWrapper;
import com.tce.smart.platform.api.dto.req.isc.IscCardImportStartReqDTO;
import com.tce.smart.platform.api.dto.resp.isc.IscCardImportBatchRespDTO;
import com.tce.smart.platform.core.entity.SmtIscCardImportBatch;
import com.tce.smart.platform.core.enums.IscCardImportStaffScopeEnum;
import org.springframework.stereotype.Component;

@Component
public class IscCardImportBatchRespWrapper extends BaseWrapper<SmtIscCardImportBatch, IscCardImportBatchRespDTO> {

	@Override
	protected IscCardImportBatchRespDTO warp(SmtIscCardImportBatch batch) {
		IscCardImportBatchRespDTO dto = new IscCardImportBatchRespDTO();
		BeanUtil.copyProperties(batch, dto);
		IscCardImportStaffScopeEnum staffScope = staffScope(batch.getParamsJson());
		dto.setStaffScope(staffScope.getCode());
		dto.setStaffScopeDesc(staffScope.getDesc());
		return dto;
	}

	private IscCardImportStaffScopeEnum staffScope(String paramsJson) {
		if (StrUtil.isBlank(paramsJson)) {
			return IscCardImportStaffScopeEnum.ALL;
		}
		try {
			IscCardImportStartReqDTO params = JSONUtil.toBean(paramsJson, IscCardImportStartReqDTO.class);
			IscCardImportStaffScopeEnum staffScope = IscCardImportStaffScopeEnum.getByCode(params.getStaffScope());
			return staffScope == null ? IscCardImportStaffScopeEnum.ALL : staffScope;
		} catch (Exception e) {
			return IscCardImportStaffScopeEnum.ALL;
		}
	}
}
