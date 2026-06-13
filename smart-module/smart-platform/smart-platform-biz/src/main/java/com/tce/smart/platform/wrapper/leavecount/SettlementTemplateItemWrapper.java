package com.tce.smart.platform.wrapper.leavecount;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.tce.smart.common.core.util.BeanUtils;
import com.tce.smart.common.core.wrapper.BaseWrapper;
import com.tce.smart.platform.api.dto.resp.leavecount.SettlementTemplateItemRespDTO;
import com.tce.smart.platform.api.dto.resp.leavecount.SettlementTemplateJcheRespDTO;
import com.tce.smart.platform.api.dto.resp.leavecount.SettlementTemplateRuleRespDTO;
import com.tce.smart.platform.core.entity.leavecount.SmtSettlementTemplateItem;
import com.tce.smart.platform.core.entity.leavecount.SmtSettlementTemplateJche;
import com.tce.smart.platform.core.entity.leavecount.SmtSettlementTemplateRule;
import com.tce.smart.platform.service.leavecount.SmtSettlementTemplateJcheService;
import com.tce.smart.platform.service.leavecount.SmtSettlementTemplateRuleService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

/**
 * @Description: TODO
 * @ProjectName smart-module
 * @ClassName:

 */
@Component
@AllArgsConstructor
public class SettlementTemplateItemWrapper extends BaseWrapper<SmtSettlementTemplateItem, SettlementTemplateItemRespDTO> {

	private final SmtSettlementTemplateRuleService smtSettlementTemplateRuleService;

	private final SmtSettlementTemplateJcheService smtSettlementTemplateJcheService;

    @Override
    protected SettlementTemplateItemRespDTO warp(SmtSettlementTemplateItem bean) throws IOException {
		List<SmtSettlementTemplateRule> ruleList = smtSettlementTemplateRuleService.list(Wrappers.<SmtSettlementTemplateRule>lambdaQuery()
				.eq(SmtSettlementTemplateRule::getItemId, bean.getId()));
		List<SettlementTemplateRuleRespDTO> ruleRespDTOS = BeanUtils.batchTransform(SettlementTemplateRuleRespDTO.class, ruleList);
		List<SmtSettlementTemplateJche> jcheList = smtSettlementTemplateJcheService.list(Wrappers.<SmtSettlementTemplateJche>lambdaQuery()
				.eq(SmtSettlementTemplateJche::getItemId, bean.getId()));
		List<SettlementTemplateJcheRespDTO> jcheRespDTOS = BeanUtils.batchTransform(SettlementTemplateJcheRespDTO.class, jcheList);
		SettlementTemplateItemRespDTO resp = new SettlementTemplateItemRespDTO();
		resp.setItemId(bean.getId());
		resp.setRules(ruleRespDTOS);
		resp.setJches(jcheRespDTOS);
        return resp;
    }
}
