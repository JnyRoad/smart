package com.tce.smart.platform.core.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tce.smart.common.core.exception.SmartException;
import com.tce.smart.platform.core.entity.SmtApproval;
import com.tce.smart.platform.core.mapper.SmtApprovalMapper;
import com.tce.smart.platform.core.service.SmtApprovalService;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 *
 *审批配置表
 * @author fushiping
 * @date 2021-04-08 16:25:32
 */
@Service
public class SmtApprovalServiceImpl extends ServiceImpl<SmtApprovalMapper, SmtApproval> implements SmtApprovalService {

	@Override
	public Boolean saveApproval(SmtApproval smtApproval) {
		SmtApproval reApproval = this.getOne(Wrappers.<SmtApproval>query().lambda()
				.eq(Objects.nonNull(smtApproval.getParkId()), SmtApproval::getParkId, smtApproval.getParkId())
				.eq(Objects.nonNull(smtApproval.getEventCode()), SmtApproval::getEventCode, smtApproval.getEventCode()));
		if(Objects.nonNull(reApproval)) {
			throw new SmartException("该园区审批配置已存在");
		}
		// 物品放行设置
		if (smtApproval.getEventCode() == 3) {
			// 默认设置不需要上传图片
			smtApproval.setIsUploadImg(1);
		}
		return this.save(smtApproval);
	}

	@Override
	public SmtApproval getApproval(Integer parkId, Integer eventCode) {
		return this.getOne(Wrappers.<SmtApproval>query().lambda()
				.eq(SmtApproval::getParkId, parkId).eq(SmtApproval::getEventCode,eventCode));
	}
}
