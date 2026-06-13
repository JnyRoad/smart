package com.tce.smart.data.wrapper.ehrview;

import com.tce.smart.common.core.wrapper.BaseWrapper;
import com.tce.smart.ehrview.core.entity.LvwLcdOttype;
import com.tce.smart.ehrview.core.entity.OvwYsdep;
import com.tce.smart.ehrview.core.service.ILvwLcdOttypeService;
import com.tce.smart.ehrview.core.service.IOvwYsdepService;
import com.tce.smart.tool.enums.FormStateEnum;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.tce.smart.data.api.dto.ehrview.resp.EvwBizAregotRegisterRespDTO;
import com.tce.smart.ehrview.core.entity.EvwBizAregotRegister;

import java.util.Objects;

@Component
public class EvwBizAregotRegisterWrapper extends BaseWrapper<EvwBizAregotRegister, EvwBizAregotRegisterRespDTO> {
	@Autowired
	private IOvwYsdepService iOvwYsdepService;
	@Autowired
	private ILvwLcdOttypeService iLvwLcdOttypeService;
	@Override
	protected EvwBizAregotRegisterRespDTO warp(EvwBizAregotRegister model) {
		// TODO Auto-generated method stub
		EvwBizAregotRegisterRespDTO evwBizAregotRegisterRespDTO = new EvwBizAregotRegisterRespDTO();
		BeanUtils.copyProperties(model, evwBizAregotRegisterRespDTO);
		OvwYsdep ovwYsdep = iOvwYsdepService.getByDepId(model.getDEPID());
		if(Objects.nonNull(ovwYsdep))evwBizAregotRegisterRespDTO.setDEPName(ovwYsdep.getDepname());
		LvwLcdOttype lvwLcdOttyp = iLvwLcdOttypeService.getById(model.getOTTYPE());
		evwBizAregotRegisterRespDTO.setOTTYPEName(lvwLcdOttyp.getTitle());
		if(Objects.nonNull(model.getFormState()))
			evwBizAregotRegisterRespDTO.setFormStateDesc(FormStateEnum.desc(model.getFormState()).getDesc());
        return evwBizAregotRegisterRespDTO;
	}

}
