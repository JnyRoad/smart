package com.tce.smart.data.wrapper.ehrview;

import com.tce.smart.common.core.wrapper.BaseWrapper;
import com.tce.smart.ehrview.core.entity.LvwLcdOttype;
import com.tce.smart.ehrview.core.entity.OvwYsdep;
import com.tce.smart.ehrview.core.service.ILvwLcdOttypeService;
import com.tce.smart.ehrview.core.service.IOvwYsdepService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.tce.smart.data.api.dto.ehrview.resp.EvwLergotAllRespDTO;
import com.tce.smart.ehrview.core.entity.EvwLergotAll;

import java.util.Objects;

@Component
public class EvwLergotAllWrapper extends BaseWrapper< EvwLergotAll,  EvwLergotAllRespDTO> {
	@Autowired
	private IOvwYsdepService iOvwYsdepService;
	@Autowired
	private ILvwLcdOttypeService iLvwLcdOttypeService;
	@Override
	protected EvwLergotAllRespDTO warp(EvwLergotAll model) {
		// TODO Auto-generated method stub
		EvwLergotAllRespDTO evwLergotAllRespDTO = new EvwLergotAllRespDTO();
		BeanUtils.copyProperties(model,evwLergotAllRespDTO);
		OvwYsdep ovwYsdep = iOvwYsdepService.getByDepId(model.getDEPID());
		if(Objects.nonNull(ovwYsdep)) evwLergotAllRespDTO.setDEPName(ovwYsdep.getDepname());
		LvwLcdOttype lvwLcdOttyp = iLvwLcdOttypeService.getById(model.getOTTYPE());
		evwLergotAllRespDTO.setOTTYPEName(lvwLcdOttyp.getTitle());
        return evwLergotAllRespDTO;
	}
}
