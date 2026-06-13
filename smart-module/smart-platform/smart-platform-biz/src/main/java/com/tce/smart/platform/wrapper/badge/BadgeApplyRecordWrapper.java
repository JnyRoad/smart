package com.tce.smart.platform.wrapper.badge;

import com.tce.smart.common.core.util.BeanUtils;
import com.tce.smart.common.core.wrapper.BaseWrapper;
import com.tce.smart.platform.api.dto.resp.badge.BadgeApplyRecordRespDTO;
import com.tce.smart.platform.api.dto.resp.badge.BadgeConfigListRespDTO;
import com.tce.smart.platform.core.entity.badge.SmtBadgeApply;
import com.tce.smart.platform.core.entity.badge.SmtBadgeConfig;
import com.tce.smart.tool.enums.BadgeApplyReasonEnum;
import com.tce.smart.tool.enums.BadgeOperaStatusEnum;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * @ProjectName smart-module
 * @ClassName: BadgeApplyRecordWrapper
 * @Author fushiping
 * @Date 2020/7/8
 */
@Component
@AllArgsConstructor
public class BadgeApplyRecordWrapper extends BaseWrapper<SmtBadgeApply, BadgeApplyRecordRespDTO> {

    @Override
    protected BadgeApplyRecordRespDTO warp(SmtBadgeApply smtBadgeApply) throws IOException {
		BadgeApplyRecordRespDTO respDTO = BeanUtils.transform(BadgeApplyRecordRespDTO.class, smtBadgeApply);
		respDTO.setStateDesc(BadgeOperaStatusEnum.desc(smtBadgeApply.getState()));
		respDTO.setReason(BadgeApplyReasonEnum.desc(smtBadgeApply.getReason()));
        return respDTO;
    }
}
