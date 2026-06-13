package com.tce.smart.data.wrapper.ehrview;

import com.tce.smart.common.core.wrapper.BaseWrapper;
import com.tce.smart.data.api.dto.ehrview.EvwEmphrYsDTO;
import com.tce.smart.data.api.dto.ehrview.resp.EvwEmphrYsRespDTO;
import com.tce.smart.ehrview.core.entity.EvwEmphrYs;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

/**
 * @Description: TODO
 * @ProjectName smart-module
 * @ClassName: EvwEmphrYsWrapper
 * @Author jinbo
 * @Date 2019/5/2
 */
@Component
public class EvwEmphrYsVOWrapper extends BaseWrapper<EvwEmphrYs, EvwEmphrYsRespDTO> {
    @Override
    protected EvwEmphrYsRespDTO warp(EvwEmphrYs evwEmphrYs){
        EvwEmphrYsRespDTO evwEmphrYsVO = new EvwEmphrYsRespDTO();
		EvwEmphrYsDTO evwEmphrYsDTO = new EvwEmphrYsDTO();
		BeanUtils.copyProperties(evwEmphrYs,evwEmphrYsDTO);
        return evwEmphrYsVO.change(evwEmphrYsDTO);
    }
}
