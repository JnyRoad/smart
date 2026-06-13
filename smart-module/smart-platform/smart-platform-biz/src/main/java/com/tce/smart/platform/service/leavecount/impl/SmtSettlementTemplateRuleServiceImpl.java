package com.tce.smart.platform.service.leavecount.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tce.smart.common.core.util.BeanUtils;
import com.tce.smart.platform.api.dto.req.leavecount.SettlementTemplateRuleReqDTO;
import com.tce.smart.platform.core.entity.leavecount.SmtSettlementTemplateRule;
import com.tce.smart.platform.core.mapper.leavecount.SmtSettlementTemplateRuleMapper;
import com.tce.smart.platform.service.leavecount.SmtSettlementTemplateRuleService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author fushiping
 * @date 2022-06-21 11:01:40
 */
@Service
public class SmtSettlementTemplateRuleServiceImpl extends ServiceImpl<SmtSettlementTemplateRuleMapper, SmtSettlementTemplateRule> implements SmtSettlementTemplateRuleService {

	@Override
	public Boolean saveRule(List<SettlementTemplateRuleReqDTO> reqDTO, Long tempId, Long itemId) {
		reqDTO.forEach(req -> {
			SmtSettlementTemplateRule rule = BeanUtils.transform(SmtSettlementTemplateRule.class, req);
			rule.setTempId(tempId);
			rule.setItemId(itemId);
			rule.insert();
		});
		return Boolean.TRUE;
	}
}
