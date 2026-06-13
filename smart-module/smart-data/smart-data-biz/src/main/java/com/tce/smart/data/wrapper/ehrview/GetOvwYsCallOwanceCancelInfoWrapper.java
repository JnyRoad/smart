package com.tce.smart.data.wrapper.ehrview;

import com.tce.smart.common.core.wrapper.BaseWrapper;
import com.tce.smart.data.api.dto.ehrview.resp.OvwYsCallOwanceCancelAllRespDTO;
import com.tce.smart.ehrview.core.entity.OvwYsCallOwanceCancelAll;
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
public class GetOvwYsCallOwanceCancelInfoWrapper extends BaseWrapper<OvwYsCallOwanceCancelAll, OvwYsCallOwanceCancelAllRespDTO> {
    @Override
    protected OvwYsCallOwanceCancelAllRespDTO warp(OvwYsCallOwanceCancelAll ovwYsCallOwanceCancelAll){
		OvwYsCallOwanceCancelAllRespDTO ovwYsCallOwanceCancelAllDTO = new OvwYsCallOwanceCancelAllRespDTO();
		BeanUtils.copyProperties(ovwYsCallOwanceCancelAll,ovwYsCallOwanceCancelAllDTO);
        return ovwYsCallOwanceCancelAllDTO;
    }
}
