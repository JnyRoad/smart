package com.tce.smart.data.wrapper.ehrview;

import com.tce.smart.common.core.wrapper.BaseWrapper;
import com.tce.smart.data.api.dto.ehrview.resp.EvwCallowanceCancelAlltRespDTO;
import com.tce.smart.ehrview.core.entity.CvwCcdAllowance;
import com.tce.smart.ehrview.core.entity.EvwCallowanceCancelAllt;
import com.tce.smart.ehrview.core.entity.OvwYsdep;
import com.tce.smart.ehrview.core.service.CvwCcdAllowanceService;
import com.tce.smart.ehrview.core.service.IOvwYsdepService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * @Auther: guohongtai
 * @Date: 2020-07-21 11:14
 */
@Component
public class EvwCallowanceCancelAlltWrapper extends BaseWrapper<EvwCallowanceCancelAllt, EvwCallowanceCancelAlltRespDTO> {
	@Autowired
	private CvwCcdAllowanceService cvwCcdAllowanceService;
	@Autowired
	private IOvwYsdepService iOvwYsdepService;
	@Override
	protected EvwCallowanceCancelAlltRespDTO warp(EvwCallowanceCancelAllt model) {
		// TODO Auto-generated method stub
		EvwCallowanceCancelAlltRespDTO evwCallowanceCancelAlltRespDTO = new EvwCallowanceCancelAlltRespDTO();
		BeanUtils.copyProperties(model,evwCallowanceCancelAlltRespDTO);
		OvwYsdep ovwYsdep = iOvwYsdepService.getByDepId(model.getDEPID());
		if(Objects.nonNull(ovwYsdep)) evwCallowanceCancelAlltRespDTO.setDEPName(ovwYsdep.getDepname());
		CvwCcdAllowance cvwCcdAllowance = cvwCcdAllowanceService.getById(model.getXTYPE());
		evwCallowanceCancelAlltRespDTO.setXTYPEName(cvwCcdAllowance.getTitle());
		return evwCallowanceCancelAlltRespDTO;
	}
}
