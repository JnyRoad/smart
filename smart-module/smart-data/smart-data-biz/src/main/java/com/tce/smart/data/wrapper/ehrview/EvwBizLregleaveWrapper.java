package com.tce.smart.data.wrapper.ehrview;

import com.tce.smart.common.core.wrapper.BaseWrapper;
import com.tce.smart.data.api.dto.ehrview.resp.EvwBizLregleaveRespDTO;
import com.tce.smart.ehrview.core.entity.EvwBizLregleave;
import com.tce.smart.ehrview.core.entity.LvwLcdLeavetype;
import com.tce.smart.ehrview.core.entity.LvwLcdUnit;
import com.tce.smart.ehrview.core.entity.OvwYsdep;
import com.tce.smart.ehrview.core.service.ILvwLcdLeavetypeService;
import com.tce.smart.ehrview.core.service.ILvwLcdUnitService;
import com.tce.smart.ehrview.core.service.IOvwYsdepService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * @Auther: guohongtai
 * @Date: 2020-07-20 19:11
 */
@Component
public class EvwBizLregleaveWrapper extends BaseWrapper<EvwBizLregleave, EvwBizLregleaveRespDTO> {
	@Autowired
	private ILvwLcdLeavetypeService iLvwLcdLeavetypeService;
	@Autowired
	private IOvwYsdepService iOvwYsdepService;
	@Autowired
	private ILvwLcdUnitService iLvwLcdUnitService;
	@Override
	protected EvwBizLregleaveRespDTO warp(EvwBizLregleave model) {
		// TODO Auto-generated method stub
		EvwBizLregleaveRespDTO evwBizLregleaveRespDTO = new EvwBizLregleaveRespDTO();
		BeanUtils.copyProperties(model,evwBizLregleaveRespDTO);
		OvwYsdep ovwYsdep = iOvwYsdepService.getByDepId(model.getDEPID());
		if(Objects.nonNull(ovwYsdep)) evwBizLregleaveRespDTO.setDEPName(ovwYsdep.getDepname());
		LvwLcdLeavetype lvwLcdLeavetype = iLvwLcdLeavetypeService.getById(model.getTWID());
		evwBizLregleaveRespDTO.setTWIDName(lvwLcdLeavetype.getTitle());
		LvwLcdUnit lvwLcdUnit = iLvwLcdUnitService.getById(model.getUnit());
		 evwBizLregleaveRespDTO.setUnitName(lvwLcdUnit.getTitle());
		return evwBizLregleaveRespDTO;
	}
}
