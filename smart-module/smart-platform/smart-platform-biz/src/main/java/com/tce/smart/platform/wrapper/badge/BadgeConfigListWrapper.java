package com.tce.smart.platform.wrapper.badge;

import com.tce.smart.common.core.util.BeanUtils;
import com.tce.smart.common.core.wrapper.BaseWrapper;
import com.tce.smart.platform.api.dto.resp.badge.BadgeConfigListRespDTO;
import com.tce.smart.platform.core.entity.badge.SmtBadgeConfig;
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
public class BadgeConfigListWrapper extends BaseWrapper<SmtBadgeConfig, BadgeConfigListRespDTO> {

    @Override
    protected BadgeConfigListRespDTO warp(SmtBadgeConfig smtBadgeConfig) throws IOException {
	BadgeConfigListRespDTO respDTO = BeanUtils.transform(BadgeConfigListRespDTO.class, smtBadgeConfig);
        return respDTO;
    }
}
