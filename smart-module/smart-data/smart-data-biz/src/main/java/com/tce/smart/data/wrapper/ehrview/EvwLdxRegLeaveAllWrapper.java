package com.tce.smart.data.wrapper.ehrview;

import com.tce.smart.common.core.wrapper.BaseWrapper;
import com.tce.smart.data.api.dto.ehrview.resp.EvwLdxRegLeaveAllRespDTO;
import com.tce.smart.ehrview.core.entity.EvwLdxRegLeaveAll;
import com.tce.smart.ehrview.core.entity.LvwLcdTxtype;
import com.tce.smart.ehrview.core.entity.OvwYsdep;
import com.tce.smart.ehrview.core.service.ILvwLcdTxtypeService;
import com.tce.smart.ehrview.core.service.IOvwYsdepService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * @Auther: guohongtai
 * @Date: 2020-07-27 11:27
 */
@Component
public class EvwLdxRegLeaveAllWrapper extends BaseWrapper<EvwLdxRegLeaveAll, EvwLdxRegLeaveAllRespDTO> {
	@Autowired
	private ILvwLcdTxtypeService iLvwLcdTxtypeService;
	@Autowired
	private IOvwYsdepService iOvwYsdepService;
	@Override
	protected EvwLdxRegLeaveAllRespDTO warp(EvwLdxRegLeaveAll model) {
		EvwLdxRegLeaveAllRespDTO evwLdxRegLeaveAllRespDTO = new EvwLdxRegLeaveAllRespDTO();
		BeanUtils.copyProperties(model, evwLdxRegLeaveAllRespDTO);
		OvwYsdep ovwYsdep = iOvwYsdepService.getByDepId(model.getDEPID());
		if(Objects.nonNull(ovwYsdep)) evwLdxRegLeaveAllRespDTO.setDEPName(ovwYsdep.getDepname());
		LvwLcdTxtype lvwLcdTxtype = iLvwLcdTxtypeService.getById(model.getTWID());
		evwLdxRegLeaveAllRespDTO.setTWIDName(lvwLcdTxtype.getTitle());
		return evwLdxRegLeaveAllRespDTO;
	}
}
