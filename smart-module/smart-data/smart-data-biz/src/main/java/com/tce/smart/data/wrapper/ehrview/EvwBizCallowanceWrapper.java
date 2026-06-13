package com.tce.smart.data.wrapper.ehrview;

import com.tce.smart.common.core.wrapper.BaseWrapper;
import com.tce.smart.data.api.dto.ehrview.resp.EvwBizCallowanceRespDTO;
import com.tce.smart.ehrview.core.entity.CvwCcdAllowance;
import com.tce.smart.ehrview.core.entity.EvwBizCallowance;
import com.tce.smart.ehrview.core.entity.OvwYsdep;
import com.tce.smart.ehrview.core.service.CvwCcdAllowanceService;
import com.tce.smart.ehrview.core.service.IOvwYsdepService;
import com.tce.smart.tool.enums.FormStateEnum;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * @Auther: guohongtai
 * @Date: 2020-07-22 20:11
 */
@Component
public class EvwBizCallowanceWrapper extends BaseWrapper<EvwBizCallowance, EvwBizCallowanceRespDTO> {
	@Autowired
	private CvwCcdAllowanceService cvwCcdAllowanceService;
	@Autowired
	private IOvwYsdepService iOvwYsdepService;
	@Override
	protected EvwBizCallowanceRespDTO warp(EvwBizCallowance model) {
		// TODO Auto-generated method stub
		EvwBizCallowanceRespDTO evwBizCallowanceRespDTO = new EvwBizCallowanceRespDTO();
		BeanUtils.copyProperties(model,evwBizCallowanceRespDTO);
		OvwYsdep ovwYsdep = iOvwYsdepService.getByDepId(model.getDEPID());
		if(Objects.nonNull(ovwYsdep)) evwBizCallowanceRespDTO.setDEPName(ovwYsdep.getDepname());
		CvwCcdAllowance cvwCcdAllowance = cvwCcdAllowanceService.getById(model.getXTYPE());
		evwBizCallowanceRespDTO.setXTYPEName(cvwCcdAllowance.getTitle());
		if(Objects.nonNull(model.getFormState()))
			evwBizCallowanceRespDTO.setFormStateDesc(FormStateEnum.desc(model.getFormState()).getDesc());
		return evwBizCallowanceRespDTO;
	}
}
