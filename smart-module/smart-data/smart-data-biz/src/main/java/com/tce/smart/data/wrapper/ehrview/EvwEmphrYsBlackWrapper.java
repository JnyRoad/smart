package com.tce.smart.data.wrapper.ehrview;

import com.tce.smart.common.core.wrapper.BaseWrapper;
import com.tce.smart.data.api.dto.ehrview.resp.EvwEmphrYsBlackRespDTO;
import com.tce.smart.ehrview.core.entity.EvwEmphrYs;
import cn.hutool.core.bean.BeanUtil;
import org.springframework.stereotype.Component;

/**
 * @Description: TODO
 * @ProjectName smart-module
 * @ClassName: EvwEmphrYsWrapper
 * @Author jinbo
 * @Date 2019/5/2
 */
@Component
public class EvwEmphrYsBlackWrapper extends BaseWrapper<EvwEmphrYs, EvwEmphrYsBlackRespDTO> {
    @Override
    protected EvwEmphrYsBlackRespDTO warp(EvwEmphrYs evwEmphrYs){
	EvwEmphrYsBlackRespDTO evwEmphrYsBlackRespDTO=new EvwEmphrYsBlackRespDTO();
	BeanUtil.copyProperties(evwEmphrYs, evwEmphrYsBlackRespDTO);
	evwEmphrYsBlackRespDTO.setName(evwEmphrYs.getName());
	evwEmphrYsBlackRespDTO.setAlterTime(evwEmphrYs.getAltertime());
        return evwEmphrYsBlackRespDTO;
    }
}
