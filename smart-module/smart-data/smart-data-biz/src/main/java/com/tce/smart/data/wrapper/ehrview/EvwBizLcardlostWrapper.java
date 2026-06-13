package com.tce.smart.data.wrapper.ehrview;

import com.tce.smart.common.core.wrapper.BaseWrapper;
import com.tce.smart.ehrview.core.entity.LvwLcdCardtype;
import com.tce.smart.ehrview.core.entity.OvwYsdep;
import com.tce.smart.ehrview.core.service.ILvwLcdCardtypeService;
import com.tce.smart.ehrview.core.service.IOvwYsdepService;
import com.tce.smart.tool.enums.FormStateEnum;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.tce.smart.data.api.dto.ehrview.resp.EvwBizLcardlostRespDTO;
import com.tce.smart.ehrview.core.entity.EvwBizLcardlost;

import java.util.Objects;

@Component
public class EvwBizLcardlostWrapper extends BaseWrapper<EvwBizLcardlost, EvwBizLcardlostRespDTO> {
	@Autowired
	private ILvwLcdCardtypeService iLvwLcdCardtypeService;
	@Autowired
	private IOvwYsdepService iOvwYsdepService;
	@Override
	protected EvwBizLcardlostRespDTO warp(EvwBizLcardlost model) {
		// TODO Auto-generated method stub
		EvwBizLcardlostRespDTO evwBizLcardlostRespDTO = new EvwBizLcardlostRespDTO();
		BeanUtils.copyProperties(model,evwBizLcardlostRespDTO);
		OvwYsdep ovwYsdep = iOvwYsdepService.getByDepId(model.getDEPID());
		if(Objects.nonNull(ovwYsdep)) evwBizLcardlostRespDTO.setDEPName(ovwYsdep.getDepname());
		LvwLcdCardtype lvwLcdCardtype = iLvwLcdCardtypeService.getById(model.getREASON());
		evwBizLcardlostRespDTO.setREASONDes(lvwLcdCardtype.getTitle());
		if(Objects.nonNull(model.getFormState()))
			evwBizLcardlostRespDTO.setFormStateDesc(FormStateEnum.desc(model.getFormState()).getDesc());
        return evwBizLcardlostRespDTO;
	}
}
