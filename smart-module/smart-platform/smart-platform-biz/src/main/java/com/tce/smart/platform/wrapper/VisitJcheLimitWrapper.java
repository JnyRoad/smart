package com.tce.smart.platform.wrapper;

import com.tce.smart.common.core.util.BeanUtils;
import com.tce.smart.common.core.wrapper.BaseWrapper;
import com.tce.smart.platform.api.dto.resp.VisitJcheLimitDTO;
import com.tce.smart.platform.api.dto.resp.badge.BadgeConfigListRespDTO;
import com.tce.smart.platform.core.entity.SmtPark;
import com.tce.smart.platform.core.entity.SmtVisitJcheLimit;
import com.tce.smart.platform.core.entity.badge.SmtBadgeConfig;
import com.tce.smart.platform.service.SmtParkService;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Objects;

/**
 * @ProjectName smart-module
 * @ClassName: BadgeConfigListWrapper
 * @Author fushiping
 * @Date 2020/7/8
 */
@Component
@AllArgsConstructor
public class VisitJcheLimitWrapper extends BaseWrapper<SmtVisitJcheLimit, VisitJcheLimitDTO> {

	@Autowired
	private SmtParkService smtParkService;

    @Override
    protected VisitJcheLimitDTO warp(SmtVisitJcheLimit smtVisitJcheLimit) throws IOException {
		VisitJcheLimitDTO respDTO = BeanUtils.transform(VisitJcheLimitDTO.class, smtVisitJcheLimit);
		SmtPark park = smtParkService.getById(smtVisitJcheLimit.getParkId());
		if(Objects.nonNull(park)) {
			respDTO.setParkName(park.getParkName());
		}
        return respDTO;
    }
}
