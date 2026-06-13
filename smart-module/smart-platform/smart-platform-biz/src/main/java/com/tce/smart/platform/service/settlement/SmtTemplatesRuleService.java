package com.tce.smart.platform.service.settlement;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tce.smart.platform.api.dto.req.SmtTemplatesRuleReqDTO;
import com.tce.smart.platform.core.entity.SmtTemplatesRule;


/**
 * @description: SmtTemplatesRuleService
 * @date: 2020-07-13 17:26
 * @author: wuling
 * @version: 1.0
 */
public interface SmtTemplatesRuleService extends IService<SmtTemplatesRule> {

	/**
	 * 保存水电模板收费规则
	 * 这里有两个动作
	 * 	1。如果规则不存在，则添加
	 * 	2.如果规则存在，则修改
	 * @param smtTemplatesRuleReqDTO
	 * @return
	 */
	boolean saveSDTemplateRules(SmtTemplatesRuleReqDTO smtTemplatesRuleReqDTO);

	/**
	 * 查询模板收费规则
	 * @param tempId
	 * @return
	 */
	SmtTemplatesRuleReqDTO getSDTemplateRules(Long tempId);
}
