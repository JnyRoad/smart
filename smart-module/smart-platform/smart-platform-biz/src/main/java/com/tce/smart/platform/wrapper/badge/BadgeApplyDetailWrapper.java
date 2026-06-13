package com.tce.smart.platform.wrapper.badge;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.tce.smart.common.core.util.BeanUtils;
import com.tce.smart.common.core.wrapper.BaseWrapper;
import com.tce.smart.platform.api.dto.resp.badge.BadgeApplyDetailRespDTO;
import com.tce.smart.platform.api.dto.resp.badge.BadgeRecordRespDTO;
import com.tce.smart.platform.core.entity.badge.SmtBadgeApply;
import com.tce.smart.platform.core.entity.badge.SmtBadgeRecord;
import com.tce.smart.platform.service.badge.SmtBadgeRecordService;
import com.tce.smart.tool.enums.BadgeApplyReasonEnum;
import com.tce.smart.tool.enums.BadgeOperaStatusEnum;
import com.tce.smart.tool.enums.BadgeStatusEnum;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @ProjectName smart-module
 * @ClassName: BadgeApplyRecordWrapper
 * @Author fushiping
 * @Date 2020/7/8
 */
@Component
@AllArgsConstructor
public class BadgeApplyDetailWrapper extends BaseWrapper<SmtBadgeApply, BadgeApplyDetailRespDTO> {

	@Autowired
	private SmtBadgeRecordService smtBadgeRecordService;

    @Override
    protected BadgeApplyDetailRespDTO warp(SmtBadgeApply smtBadgeApply) throws IOException {
		BadgeApplyDetailRespDTO respDTO = BeanUtils.transform(BadgeApplyDetailRespDTO.class, smtBadgeApply);
		respDTO.setStateDesc(BadgeOperaStatusEnum.desc(smtBadgeApply.getState()));
		respDTO.setReason(BadgeApplyReasonEnum.desc(smtBadgeApply.getReason()));
		//添加操作记录
		List<SmtBadgeRecord> records = smtBadgeRecordService.list(Wrappers.<SmtBadgeRecord>query()
				.lambda().eq(SmtBadgeRecord::getApplyId, smtBadgeApply.getId()).orderByAsc(SmtBadgeRecord::getCreateTime));
		List<BadgeRecordRespDTO> detailOperas = new ArrayList<>();
		records.forEach(smtBadgeRecord -> {
			BadgeRecordRespDTO detailOpera =
					BeanUtils.transform(BadgeRecordRespDTO.class, smtBadgeRecord);
			Integer status = smtBadgeRecord.getOperateType();
			detailOpera.setOperateTypeDesc(BadgeOperaStatusEnum.remark(status));
			detailOpera.setOperateTitleDesc(BadgeOperaStatusEnum.title(status));
			if(status.equals(BadgeOperaStatusEnum.APPLY.getCode())) {
				detailOpera.setCreaterName(smtBadgeApply.getName());
			}
			if(status.equals(BadgeOperaStatusEnum.AGREE.getCode())) {
				detailOpera.setRemark(smtBadgeApply.getAddress());
			}
			if(status.equals(BadgeOperaStatusEnum.REFUSE.getCode())) {
				detailOpera.setRemark(smtBadgeApply.getRefuseReason());
			}
			detailOperas.add(detailOpera);
		});
		respDTO.setOperaList(detailOperas);
        return respDTO;
    }
}
