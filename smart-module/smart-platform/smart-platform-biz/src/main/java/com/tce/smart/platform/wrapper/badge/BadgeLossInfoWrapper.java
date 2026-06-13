package com.tce.smart.platform.wrapper.badge;

import com.tce.smart.common.core.util.BeanUtils;
import com.tce.smart.common.core.wrapper.BaseWrapper;
import com.tce.smart.platform.api.dto.resp.badge.BadgeConfigListRespDTO;
import com.tce.smart.platform.api.dto.resp.badge.BadgeLossInfoRespDTO;
import com.tce.smart.platform.core.entity.badge.SmtBadgeConfig;
import com.tce.smart.platform.core.entity.badge.SmtBadgeLoss;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * @ProjectName smart-module
 * @ClassName: BadgeConfigListWrapper
 * @Author fushiping
 * @Date 2020/7/8
 */
@Component
@AllArgsConstructor
public class BadgeLossInfoWrapper extends BaseWrapper<SmtBadgeLoss, BadgeLossInfoRespDTO> {

    @Override
    protected BadgeLossInfoRespDTO warp(SmtBadgeLoss badgeLoss) throws IOException {
		BadgeLossInfoRespDTO respDTO = BeanUtils.transform(BadgeLossInfoRespDTO.class, badgeLoss);
        return respDTO;
    }
}
