package com.tce.smart.data.wrapper.ehrview;

import com.tce.smart.common.core.wrapper.BaseWrapper;
import com.tce.smart.data.api.dto.ehrview.resp.EvwBizCallowanceFoodRespDTO;
import com.tce.smart.ehrview.core.entity.CvwCcdAllowance;
import com.tce.smart.ehrview.core.entity.EvwBizCallowanceFood;
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
 * @Date: 2020-07-21 11:13
 */
@Component
public class EvwBizCallowanceFoodWrapper extends BaseWrapper<EvwBizCallowanceFood, EvwBizCallowanceFoodRespDTO> {
	@Autowired
	private CvwCcdAllowanceService cvwCcdAllowanceService;
	@Autowired
	private IOvwYsdepService iOvwYsdepService;
	@Override
	protected EvwBizCallowanceFoodRespDTO warp(EvwBizCallowanceFood model) {
		// TODO Auto-generated method stub
		EvwBizCallowanceFoodRespDTO evwCallowanceAllRespDTO = new EvwBizCallowanceFoodRespDTO();
		BeanUtils.copyProperties(model,evwCallowanceAllRespDTO);
		OvwYsdep ovwYsdep = iOvwYsdepService.getByDepId(model.getDEPID());
		if(Objects.nonNull(ovwYsdep)) evwCallowanceAllRespDTO.setDEPName(ovwYsdep.getDepname());
		CvwCcdAllowance cvwCcdAllowance = cvwCcdAllowanceService.getById(model.getXTYPE());
		evwCallowanceAllRespDTO.setXTYPEName(cvwCcdAllowance.getTitle());
		if(Objects.nonNull(model.getFormState()))
			evwCallowanceAllRespDTO.setFormStateDesc(FormStateEnum.desc(model.getFormState()).getDesc());
		return evwCallowanceAllRespDTO;
	}
}
