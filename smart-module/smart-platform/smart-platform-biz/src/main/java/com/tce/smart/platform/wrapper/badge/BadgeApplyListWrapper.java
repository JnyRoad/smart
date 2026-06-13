package com.tce.smart.platform.wrapper.badge;

import com.tce.smart.common.core.util.BeanUtils;
import com.tce.smart.common.core.wrapper.BaseWrapper;
import com.tce.smart.platform.api.dto.resp.badge.BadgeApplyListRespDTO;
import com.tce.smart.platform.api.dto.resp.badge.BadgeApplyRecordRespDTO;
import com.tce.smart.platform.core.entity.badge.SmtBadgeApply;
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
public class BadgeApplyListWrapper extends BaseWrapper<SmtBadgeApply, BadgeApplyListRespDTO> {

    @Override
    protected BadgeApplyListRespDTO warp(SmtBadgeApply smtBadgeApply) throws IOException {
		BadgeApplyListRespDTO respDTO = BeanUtils.transform(BadgeApplyListRespDTO.class, smtBadgeApply);
		respDTO.setStateDesc(BadgeOperaStatusEnum.desc(smtBadgeApply.getState()));
		respDTO.setReason(BadgeApplyReasonEnum.desc(smtBadgeApply.getReason()));
        return respDTO;
    }
}
