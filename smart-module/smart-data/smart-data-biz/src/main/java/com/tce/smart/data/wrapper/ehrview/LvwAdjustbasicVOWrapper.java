package com.tce.smart.data.wrapper.ehrview;

import com.tce.smart.common.core.wrapper.BaseWrapper;
import com.tce.smart.data.api.dto.ehrview.resp.LvwAdjustbasicRespDTO;
import com.tce.smart.ehrview.core.entity.LvwAdjustbasic;
import org.springframework.stereotype.Component;

/**
 * @Description: TODO
 * @ProjectName smart-module
 * @ClassName: LvwAdjustbasicVOWrapper
 * @Author jinbo
 * @Date 2019/5/2
 */
@Component
public class LvwAdjustbasicVOWrapper extends BaseWrapper<LvwAdjustbasic, LvwAdjustbasicRespDTO> {
    @Override
    protected LvwAdjustbasicRespDTO warp(LvwAdjustbasic lvwAdjustbasic) {
        LvwAdjustbasicRespDTO lvwAdjustbasicVO = new LvwAdjustbasicRespDTO();
        lvwAdjustbasicVO.setAdjustTime(lvwAdjustbasic.getAdjustTime());
        return lvwAdjustbasicVO;
    }
}
