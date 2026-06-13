package com.tce.smart.platform.wrapper.leavecount;

import com.tce.smart.common.core.util.BeanUtils;
import com.tce.smart.common.core.wrapper.BaseWrapper;
import com.tce.smart.platform.api.dto.resp.leavecount.SettlementInfoRespDTO;
import com.tce.smart.platform.core.entity.SmtPark;
import com.tce.smart.platform.core.entity.leavecount.SmtSettlementInfo;
import com.tce.smart.platform.service.SmtParkService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Objects;

/**
 * 离职水电生成记录
 */
@Component
@AllArgsConstructor
public class SettlementInfoWrapper extends BaseWrapper<SmtSettlementInfo, SettlementInfoRespDTO> {

	private final SmtParkService smtParkService;

    @Override
    protected SettlementInfoRespDTO warp(SmtSettlementInfo bean) throws IOException {
		SettlementInfoRespDTO resp = BeanUtils.transform(SettlementInfoRespDTO.class, bean);
		if (Objects.nonNull(bean.getParkId())) {
			SmtPark smtPark = smtParkService.getById(bean.getParkId());
			resp.setParkName(smtPark.getParkName());
		}
        return resp;
    }
}
