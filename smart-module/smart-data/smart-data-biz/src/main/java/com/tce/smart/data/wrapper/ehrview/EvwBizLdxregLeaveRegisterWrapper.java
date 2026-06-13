package com.tce.smart.data.wrapper.ehrview;

import com.tce.smart.common.core.wrapper.BaseWrapper;
import com.tce.smart.data.api.dto.ehrview.resp.EvwBizLdxregLeaveRegisterRespDTO;
import com.tce.smart.ehrview.core.entity.*;
import com.tce.smart.ehrview.core.service.ILvwLcdTxtypeService;
import com.tce.smart.ehrview.core.service.IOvwYsdepService;
import com.tce.smart.tool.enums.FormStateEnum;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * @Auther: guohongtai
 * @Date: 2020-07-23 09:09
 */
@Component
public class EvwBizLdxregLeaveRegisterWrapper extends BaseWrapper<EvwBizLdxregLeaveRegister, EvwBizLdxregLeaveRegisterRespDTO> {
	@Autowired
	private ILvwLcdTxtypeService iLvwLcdTxtypeService;
	@Autowired
	private IOvwYsdepService iOvwYsdepService;
	@Override
	protected EvwBizLdxregLeaveRegisterRespDTO warp(EvwBizLdxregLeaveRegister model) {
		// TODO Auto-generated method stub
		EvwBizLdxregLeaveRegisterRespDTO evwBizLdxregLeaveRegisterRespDTO = new EvwBizLdxregLeaveRegisterRespDTO();
		BeanUtils.copyProperties(model,evwBizLdxregLeaveRegisterRespDTO);
		OvwYsdep ovwYsdep = iOvwYsdepService.getByDepId(model.getDEPID());
		if(Objects.nonNull(ovwYsdep))evwBizLdxregLeaveRegisterRespDTO.setDEPName(ovwYsdep.getDepname());
		LvwLcdTxtype lvwLcdTxtype = iLvwLcdTxtypeService.getById(model.getTWID());
		evwBizLdxregLeaveRegisterRespDTO.setTWIDName(lvwLcdTxtype.getTitle());
		if(Objects.nonNull(model.getFormState()))
			evwBizLdxregLeaveRegisterRespDTO.setFormStateDesc(FormStateEnum.desc(model.getFormState()).getDesc());
		return evwBizLdxregLeaveRegisterRespDTO;
	}
}
