package com.tce.smart.platform.wrapper.leavecount;

import com.tce.smart.common.core.util.BeanUtils;
import com.tce.smart.common.core.wrapper.BaseWrapper;
import com.tce.smart.platform.api.dto.req.leavecount.SettlementInfoQueryReqDTO;
import com.tce.smart.platform.api.dto.resp.leavecount.SettlementLogRespDTO;
import com.tce.smart.platform.core.entity.leavecount.SmtSettlementInfo;
import com.tce.smart.platform.core.entity.leavecount.SmtSettlementLog;
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
public class SettlementLogWrapper extends BaseWrapper<SmtSettlementLog, SettlementLogRespDTO> {


    @Override
    protected SettlementLogRespDTO warp(SmtSettlementLog bean) throws IOException {
		SettlementLogRespDTO resp = BeanUtils.transform(SettlementLogRespDTO.class, bean);
        return resp;
    }
}
