package com.tce.smart.platform.wrapper.leavecount;

import com.tce.smart.common.core.util.BeanUtils;
import com.tce.smart.common.core.wrapper.BaseWrapper;
import com.tce.smart.platform.api.dto.resp.leavecount.SettlementInfoRespDTO;
import com.tce.smart.platform.api.dto.resp.leavecount.SettlementTemplateRangeRespDTO;
import com.tce.smart.platform.core.entity.leavecount.SmtSettlementInfo;
import com.tce.smart.platform.core.entity.leavecount.SmtSettlementTemplateRange;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * @Description: TODO
 * @ProjectName smart-module
 * @ClassName:

 */
@Component
@AllArgsConstructor
public class SettlementRangeWrapper extends BaseWrapper<SmtSettlementTemplateRange, SettlementTemplateRangeRespDTO> {


    @Override
    protected SettlementTemplateRangeRespDTO warp(SmtSettlementTemplateRange bean) throws IOException {
		SettlementTemplateRangeRespDTO resp = BeanUtils.transform(SettlementTemplateRangeRespDTO.class, bean);
        return resp;
    }
}
