package com.tce.smart.data.wrapper.ehrview;

import java.util.Objects;

import com.tce.smart.common.core.wrapper.BaseWrapper;
import com.tce.smart.ehrview.core.entity.LvwLcdCardtype;
import com.tce.smart.ehrview.core.entity.OvwYsdep;
import com.tce.smart.ehrview.core.service.ILvwLcdCardtypeService;
import com.tce.smart.ehrview.core.service.IOvwYsdepService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.tce.smart.data.api.dto.ehrview.resp.EvwAcardlostAllRespDTO;
import com.tce.smart.ehrview.core.entity.EvwAcardlostAll;

@Component
public class EvwAcardlostAllWrapper extends BaseWrapper< EvwAcardlostAll,  EvwAcardlostAllRespDTO> {
	@Autowired
	private ILvwLcdCardtypeService iLvwLcdCardtypeService;
	@Autowired
	private IOvwYsdepService iOvwYsdepService;
	@Override
	protected EvwAcardlostAllRespDTO warp(EvwAcardlostAll model) {
		// TODO Auto-generated method stub
		EvwAcardlostAllRespDTO evwAcardlostAllRespDTO = new EvwAcardlostAllRespDTO();
		BeanUtils.copyProperties(model,evwAcardlostAllRespDTO);
		OvwYsdep ovwYsdep = iOvwYsdepService.getByDepId(model.getDEPID());
		if(Objects.nonNull(ovwYsdep))evwAcardlostAllRespDTO.setDEPName(ovwYsdep.getDepname());
		LvwLcdCardtype lvwLcdCardtype = iLvwLcdCardtypeService.getById(model.getREASON());
		evwAcardlostAllRespDTO.setREASONDes(lvwLcdCardtype.getTitle());
        return evwAcardlostAllRespDTO;
	}

}
