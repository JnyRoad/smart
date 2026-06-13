package com.tce.smart.data.wrapper.ehrview;

import com.tce.smart.common.core.wrapper.BaseWrapper;
import com.tce.smart.ehrview.core.entity.LvwLcdLeavetype;
import com.tce.smart.ehrview.core.entity.LvwLcdUnit;
import com.tce.smart.ehrview.core.entity.OvwYsdep;
import com.tce.smart.ehrview.core.service.ILvwLcdLeavetypeService;
import com.tce.smart.ehrview.core.service.ILvwLcdUnitService;
import com.tce.smart.ehrview.core.service.IOvwYsdepService;
import com.tce.smart.tool.enums.FormStateEnum;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.tce.smart.data.api.dto.ehrview.resp.EvwBizLregleaveRegisterRespDTO;
import com.tce.smart.ehrview.core.entity.EvwBizLregleaveRegister;

import java.util.Objects;

@Component
public class EvwBizLregleaveRegisterWrapper extends BaseWrapper<EvwBizLregleaveRegister, EvwBizLregleaveRegisterRespDTO> {
	@Autowired
	private ILvwLcdLeavetypeService iLvwLcdLeavetypeService;
	@Autowired
	private IOvwYsdepService iOvwYsdepService;
	@Autowired
	private ILvwLcdUnitService iLvwLcdUnitService;

	@Override
	protected EvwBizLregleaveRegisterRespDTO warp(EvwBizLregleaveRegister model) {
		// TODO Auto-generated method stub
		EvwBizLregleaveRegisterRespDTO evwBizLregleaveRegisterRespDTO = new EvwBizLregleaveRegisterRespDTO();
		BeanUtils.copyProperties(model,evwBizLregleaveRegisterRespDTO);
		OvwYsdep ovwYsdep = iOvwYsdepService.getByDepId(model.getDEPID());
		if(Objects.nonNull(ovwYsdep)) evwBizLregleaveRegisterRespDTO.setDEPName(ovwYsdep.getDepname());
		LvwLcdLeavetype lvwLcdLeavetype = iLvwLcdLeavetypeService.getById(model.getTWID());
		evwBizLregleaveRegisterRespDTO.setTWIDName(lvwLcdLeavetype.getTitle());
		LvwLcdUnit lvwLcdUnit = iLvwLcdUnitService.getById(model.getTWID());
		evwBizLregleaveRegisterRespDTO.setUnitName(lvwLcdUnit.getTitle());
		if(Objects.nonNull(model.getFormState()))
			evwBizLregleaveRegisterRespDTO.setFormStateDesc(FormStateEnum.desc(model.getFormState()).getDesc());
		return evwBizLregleaveRegisterRespDTO;
	}
}
