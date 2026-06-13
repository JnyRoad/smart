package com.tce.smart.platform.service.leavecount;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tce.smart.platform.api.dto.req.leavecount.SettlementTemplateRuleReqDTO;
import com.tce.smart.platform.core.entity.leavecount.SmtSettlementTemplateRule;

import java.util.List;

/**
 *
 *
 * @author fushiping
 * @date 2022-06-21 11:01:40
 */
public interface SmtSettlementTemplateRuleService extends IService<SmtSettlementTemplateRule> {

	/**
	 * 保存结算规则
	 * @param reqDTO
	 * @param tempId
	 * @param itemId
	 * @return
	 */
	Boolean saveRule(List<SettlementTemplateRuleReqDTO> reqDTO, Long tempId, Long itemId);

}
