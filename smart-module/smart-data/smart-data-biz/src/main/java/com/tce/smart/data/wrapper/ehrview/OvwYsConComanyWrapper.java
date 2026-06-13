package com.tce.smart.data.wrapper.ehrview;

import com.tce.smart.common.core.wrapper.BaseWrapper;
import com.tce.smart.data.api.dto.ehrview.resp.OvwYsConComanyRespDTO;
import com.tce.smart.ehrview.core.entity.OvwYsConComany;
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
public class OvwYsConComanyWrapper extends BaseWrapper<OvwYsConComany, OvwYsConComanyRespDTO> {
	@Override
	protected OvwYsConComanyRespDTO warp(OvwYsConComany ovwYsConComany) {
		OvwYsConComanyRespDTO ovwYsConComanyRespDTO = new OvwYsConComanyRespDTO();
		BeanUtils.copyProperties(ovwYsConComany, ovwYsConComanyRespDTO);
		return ovwYsConComanyRespDTO;
	}
}
