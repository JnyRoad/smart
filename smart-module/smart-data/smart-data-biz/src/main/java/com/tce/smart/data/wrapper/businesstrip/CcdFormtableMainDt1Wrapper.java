package com.tce.smart.data.wrapper.businesstrip;

import com.tce.smart.businesstrip.core.entity.CcdFormtableMainDt1;
import com.tce.smart.common.core.wrapper.BaseWrapper;
import com.tce.smart.data.api.dto.businesstrip.resp.CcdFormtableMainDt1RespDTO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

/**
 * @Description: TODO
 * @ProjectName smart-module
 * @ClassName: EvwEmphrYsWrapper
 * @Author jinbo
 * @Date 2019/5/2
 */
@Component
public class CcdFormtableMainDt1Wrapper extends BaseWrapper<CcdFormtableMainDt1, CcdFormtableMainDt1RespDTO> {
	@Override
	protected CcdFormtableMainDt1RespDTO warp(CcdFormtableMainDt1 ccdFormtableMainDt1) {
		CcdFormtableMainDt1RespDTO ccdFormtableMainDt1RespDTO = new CcdFormtableMainDt1RespDTO();
		BeanUtils.copyProperties(ccdFormtableMainDt1, ccdFormtableMainDt1RespDTO);
		return ccdFormtableMainDt1RespDTO;
	}
}
