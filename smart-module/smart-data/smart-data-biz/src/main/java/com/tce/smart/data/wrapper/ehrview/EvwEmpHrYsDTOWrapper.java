package com.tce.smart.data.wrapper.ehrview;

import cn.hutool.core.bean.BeanUtil;
import com.tce.smart.common.core.wrapper.BaseWrapper;
import com.tce.smart.data.api.dto.ehrview.EvwEmphrYsDTO;
import com.tce.smart.data.api.dto.ehrview.resp.EvwEmphrYsBlackRespDTO;
import com.tce.smart.ehrview.core.entity.EvwEmphrYs;
import org.springframework.stereotype.Component;

/**
 * @Description: TODO
 * @ProjectName smart-module
 * @ClassName: EvwEmphrYsWrapper
 * @Author jinbo
 * @Date 2019/5/2
 */
@Component
public class EvwEmpHrYsDTOWrapper extends BaseWrapper<EvwEmphrYs, EvwEmphrYsDTO> {
    @Override
    protected EvwEmphrYsDTO warp(EvwEmphrYs evwEmphrYs){
		EvwEmphrYsDTO evwEmphrYsDTO = new EvwEmphrYsDTO();
	BeanUtil.copyProperties(evwEmphrYs, evwEmphrYsDTO);
        return evwEmphrYsDTO;
    }
}
