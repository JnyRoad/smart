package com.tce.smart.data.wrapper.ehrview;

import com.tce.smart.common.core.wrapper.BaseWrapper;
import com.tce.smart.data.api.dto.ehrview.resp.EvwBizCallowanceFoodCancelRespDTO;
import com.tce.smart.ehrview.core.entity.CvwCcdAllowance;
import com.tce.smart.ehrview.core.entity.EvwBizCallowanceFoodCancel;
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
 * @Date: 2020-07-21 11:14
 */
@Component
public class EvwBizCallowanceFoodCancelWrapper extends BaseWrapper<EvwBizCallowanceFoodCancel, EvwBizCallowanceFoodCancelRespDTO> {
	@Autowired
	private CvwCcdAllowanceService cvwCcdAllowanceService;
	@Autowired
	private IOvwYsdepService iOvwYsdepService;
	@Override
	protected EvwBizCallowanceFoodCancelRespDTO warp(EvwBizCallowanceFoodCancel model) {
		// TODO Auto-generated method stub
		EvwBizCallowanceFoodCancelRespDTO evwCallowanceCancelAllRespDTO = new EvwBizCallowanceFoodCancelRespDTO();
		BeanUtils.copyProperties(model,evwCallowanceCancelAllRespDTO);
		OvwYsdep ovwYsdep = iOvwYsdepService.getByDepId(model.getDEPID());
		if(Objects.nonNull(ovwYsdep))evwCallowanceCancelAllRespDTO.setDEPName(ovwYsdep.getDepname());
		CvwCcdAllowance cvwCcdAllowance = cvwCcdAllowanceService.getById(model.getXTYPE());
		evwCallowanceCancelAllRespDTO.setXTYPEName(cvwCcdAllowance.getTitle());
		if(Objects.nonNull(model.getFormState()))
			evwCallowanceCancelAllRespDTO.setFormStateDesc(FormStateEnum.desc(model.getFormState()).getDesc());
		return evwCallowanceCancelAllRespDTO;
	}
}
