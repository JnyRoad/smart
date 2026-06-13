package com.tce.smart.platform.wrapper;

import com.tce.smart.common.core.util.BeanUtils;
import com.tce.smart.common.core.util.StringUtils;
import com.tce.smart.common.core.wrapper.BaseWrapper;
import com.tce.smart.platform.api.dto.resp.ExternalDeptRespDTO;
import com.tce.smart.platform.api.dto.resp.WechatBandingPageRespDTO;
import com.tce.smart.platform.core.entity.SmtExternalDept;
import com.tce.smart.platform.core.entity.SmtPark;
import com.tce.smart.platform.core.entity.SmtStaff;
import com.tce.smart.platform.core.entity.SmtWechatBanding;
import com.tce.smart.platform.service.SmtParkService;
import com.tce.smart.platform.service.SmtStaffService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Objects;

/**
 *
 */
@Component
@AllArgsConstructor
public class WechatBandingPageWrapper extends BaseWrapper<SmtWechatBanding, WechatBandingPageRespDTO> {

	private final SmtStaffService smtStaffService;

	private final SmtParkService smtParkService;

    @Override
    protected WechatBandingPageRespDTO warp(SmtWechatBanding bean) throws IOException {
		WechatBandingPageRespDTO respDTO = BeanUtils.transform(WechatBandingPageRespDTO.class, bean);
		SmtStaff staff = smtStaffService.getSimpleSttaffByBadge(bean.getBadge());
		if(Objects.nonNull(staff)) {
			respDTO.setStaffName(staff.getName());
		}
		SmtPark park = smtParkService.getById(bean.getParkId());
		if(Objects.nonNull(park)) {
			respDTO.setParkName(park.getParkName());
		}
		respDTO.setFrom("微信");
        return respDTO;
    }
}
