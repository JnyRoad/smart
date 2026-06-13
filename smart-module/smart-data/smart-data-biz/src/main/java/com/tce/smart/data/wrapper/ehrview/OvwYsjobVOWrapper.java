package com.tce.smart.data.wrapper.ehrview;

import com.tce.smart.common.core.wrapper.BaseWrapper;
import com.tce.smart.data.api.dto.ehrview.resp.OvwYsjobRespDTO;
import com.tce.smart.ehrview.core.entity.OvwYsjob;
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
public class OvwYsjobVOWrapper extends BaseWrapper<OvwYsjob, OvwYsjobRespDTO> {
    @Override
    protected OvwYsjobRespDTO warp(OvwYsjob ovwYsjob){
		OvwYsjobRespDTO ovwYsjobRespDTO = new OvwYsjobRespDTO();
		BeanUtils.copyProperties(ovwYsjob,ovwYsjobRespDTO);
        return ovwYsjobRespDTO;
    }
}
