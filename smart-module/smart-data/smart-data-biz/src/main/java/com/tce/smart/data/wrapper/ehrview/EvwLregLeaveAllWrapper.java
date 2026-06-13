package com.tce.smart.data.wrapper.ehrview;

import java.io.IOException;

import com.tce.smart.common.core.wrapper.BaseWrapper;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import com.tce.smart.data.api.dto.ehrview.resp.EvwLregLeaveAllRespDTO;
import com.tce.smart.ehrview.core.entity.EvwLregLeaveAll;

@Component
public class EvwLregLeaveAllWrapper extends BaseWrapper< EvwLregLeaveAll,  EvwLregLeaveAllRespDTO> {

	@Override
	protected EvwLregLeaveAllRespDTO warp(EvwLregLeaveAll model) throws IOException {
		// TODO Auto-generated method stub
		EvwLregLeaveAllRespDTO evwLregLeaveAllRespDTO = new EvwLregLeaveAllRespDTO();
		BeanUtils.copyProperties(model,evwLregLeaveAllRespDTO);
        return evwLregLeaveAllRespDTO;
	}

}
