package com.tce.smart.data.wrapper.ehrview;

import com.tce.smart.common.core.wrapper.BaseWrapper;
import com.tce.smart.data.api.dto.ehrview.resp.EvwCotherAllowanceAllRespDTO;
import com.tce.smart.ehrview.core.entity.CvwCcdAllowance;
import com.tce.smart.ehrview.core.entity.EvwCotherAllowanceAll;
import com.tce.smart.ehrview.core.entity.OvwYsdep;
import com.tce.smart.ehrview.core.service.CvwCcdAllowanceService;
import com.tce.smart.ehrview.core.service.IOvwYsdepService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * @Auther: guohongtai
 * @Date: 2020-07-22 20:12
 */
@Component
public class EvwCotherAllowanceAllWrapper extends BaseWrapper<EvwCotherAllowanceAll, EvwCotherAllowanceAllRespDTO> {
	@Autowired
	private CvwCcdAllowanceService cvwCcdAllowanceService;
	@Autowired
	private IOvwYsdepService iOvwYsdepService;
	@Override
	protected EvwCotherAllowanceAllRespDTO warp(EvwCotherAllowanceAll model) {
		// TODO Auto-generated method stub
		EvwCotherAllowanceAllRespDTO evwCotherAllowanceAllRespDTO = new EvwCotherAllowanceAllRespDTO();
		BeanUtils.copyProperties(model,evwCotherAllowanceAllRespDTO);
		OvwYsdep ovwYsdep = iOvwYsdepService.getByDepId(model.getDEPID());
		if(Objects.nonNull(ovwYsdep)) evwCotherAllowanceAllRespDTO.setDEPName(ovwYsdep.getDepname());
		CvwCcdAllowance cvwCcdAllowance = cvwCcdAllowanceService.getById(model.getXTYPE());
		evwCotherAllowanceAllRespDTO.setXTYPEName(cvwCcdAllowance.getTitle());
		return evwCotherAllowanceAllRespDTO;
	}
}