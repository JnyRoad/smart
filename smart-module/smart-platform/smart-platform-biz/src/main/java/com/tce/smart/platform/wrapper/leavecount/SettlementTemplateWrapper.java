package com.tce.smart.platform.wrapper.leavecount;

import com.tce.smart.common.core.util.BeanUtils;
import com.tce.smart.common.core.wrapper.BaseWrapper;
import com.tce.smart.platform.api.dto.resp.leavecount.SettlementInfoRespDTO;
import com.tce.smart.platform.api.dto.resp.leavecount.SettlementTemplateRespDTO;
import com.tce.smart.platform.core.entity.leavecount.SmtSettlementInfo;
import com.tce.smart.platform.core.entity.leavecount.SmtSettlementTemplate;
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
public class SettlementTemplateWrapper extends BaseWrapper<SmtSettlementTemplate, SettlementTemplateRespDTO> {


    @Override
    protected SettlementTemplateRespDTO warp(SmtSettlementTemplate bean) throws IOException {
		SettlementTemplateRespDTO resp = BeanUtils.transform(SettlementTemplateRespDTO.class, bean);
        return resp;
    }
}
