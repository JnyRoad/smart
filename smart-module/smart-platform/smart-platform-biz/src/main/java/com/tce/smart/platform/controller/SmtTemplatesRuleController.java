package com.tce.smart.platform.controller;

import cn.hutool.core.lang.Assert;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.platform.api.dto.req.SmtTemplatesRuleReqDTO;
import com.tce.smart.platform.service.settlement.SmtTemplatesRuleService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * @description: SmtTemplatesRuleController
 * @date: 2020-07-13 17:28
 * @author: wuling
 * @version: 1.0
 */
@RestController
@AllArgsConstructor
@RequestMapping("/dormitory/sdrule")
public class SmtTemplatesRuleController {

	private final SmtTemplatesRuleService smtTemplatesRuleService;

	/**
	 * 保存水电配置规则
	 * @param smtSdTemplates 水电配置规则
	 * @return Result
	 */
	@PostMapping("/add")
	public Result save(@RequestBody SmtTemplatesRuleReqDTO smtTemplatesRuleReqDTO){
		Assert.notNull(smtTemplatesRuleReqDTO.getTempId(),"水电模板ID不能为NULL");
		return new Result<>(smtTemplatesRuleService.saveSDTemplateRules(smtTemplatesRuleReqDTO));
	}


	/**
	 * 查询水电配置模板详细数据
	 * @param smtSdTemplates 配置模板ID
	 * @return Result
	 */
	@GetMapping("/{tempId}")
	public Result getRuleData(@PathVariable("tempId") Long tempId){
		return new Result<>(smtTemplatesRuleService.getSDTemplateRules(tempId));
	}
}
