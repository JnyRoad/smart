package com.tce.smart.platform.service.settlement.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tce.smart.common.security.util.SecurityUtils;
import com.tce.smart.platform.api.dto.req.SmtTemplatesRuleReqDTO;
import com.tce.smart.platform.core.entity.SmtSdTemplates;
import com.tce.smart.platform.core.entity.SmtTemplatesRule;
import com.tce.smart.platform.core.mapper.SmtSDTemplatesMapper;
import com.tce.smart.platform.core.mapper.SmtTemplatesRuleMapper;
import com.tce.smart.platform.service.settlement.SmtTemplatesRuleService;
import com.tce.smart.tool.exception.TCEException;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @description: SmtTemplatesRuleServiceImpl
 * @date: 2020-07-13 17:27
 * @author: wuling
 * @version: 1.0
 */
@Service
@AllArgsConstructor
public class SmtTemplatesRuleServiceImpl extends ServiceImpl<SmtTemplatesRuleMapper, SmtTemplatesRule> implements SmtTemplatesRuleService {

	private final SmtSDTemplatesMapper smtSDTemplatesMapper;

    @Override
    public boolean saveSDTemplateRules(SmtTemplatesRuleReqDTO smtTemplatesRuleReqDTO) {
	//判断指定的模板是否存在
		List<Integer> parkIdList = SecurityUtils.getUser().getParkIdList();
		SmtSdTemplates smtSdTemplates = smtSDTemplatesMapper.selectById(smtTemplatesRuleReqDTO.getTempId());
		if(smtSdTemplates == null || !parkIdList.contains(smtSdTemplates.getParkId())){
			throw new TCEException("水电模板不存在");
		}

		//查询模板已配置的所有规则
		List<SmtTemplatesRule> smtTemplatesRuleList = this.list(new QueryWrapper<SmtTemplatesRule>().lambda()
				.eq(SmtTemplatesRule::getTempId,smtTemplatesRuleReqDTO.getTempId()));

		//对查询结果按categoryId和monthNum两个字段分组
		Map<String, List<SmtTemplatesRule>> stringListMap = smtTemplatesRuleList.stream().collect(Collectors.groupingBy(a -> MessageFormat.format("{0}_{1}", a.getCategoryId(), a.getMonthNum())));

		//待添加列表
		List<SmtTemplatesRule> addsmtTemplatesRuleList = new ArrayList<>();
		//待更新列表
		List<SmtTemplatesRule> updatesmtTemplatesRuleList = new ArrayList<>();

		for(SmtTemplatesRuleReqDTO.Rules rules : smtTemplatesRuleReqDTO.getRulesList()){
			for (SmtTemplatesRuleReqDTO.RulesData rulesData : rules.getRulesDataList()){
				SmtTemplatesRule templatesRule = SmtTemplatesRule.builder()
						.tempId(smtTemplatesRuleReqDTO.getTempId())
						.categoryId(rules.getCategoryId())
						.monthNum(rulesData.getMonthNum())
						.standardQty(rulesData.getStandardQty())
						.overFee(rulesData.getOverFee())
						.createTime(new Date())
						.build();

				String ekey = MessageFormat.format("{0}_{1}", rules.getCategoryId(), rulesData.getMonthNum());

				if(stringListMap.containsKey(ekey)) {
					//设置标识ID 更新时根据标识ID更新
					templatesRule.setId(stringListMap.get(ekey).get(0).getId());
					updatesmtTemplatesRuleList.add(templatesRule);
				} else {
					addsmtTemplatesRuleList.add(templatesRule);
				}
			}
		}

		if(addsmtTemplatesRuleList.size() > 0){
			//添加新数据
			this.saveBatch(addsmtTemplatesRuleList);
		}

		if(updatesmtTemplatesRuleList.size() > 0){
			//更新旧数据
			this.updateBatchById(updatesmtTemplatesRuleList);
		}

        return true;
    }

	@Override
	public SmtTemplatesRuleReqDTO getSDTemplateRules(Long tempId) {

		//判断指定的模板是否存在
		List<Integer> parkIdList = SecurityUtils.getUser().getParkIdList();
		SmtSdTemplates smtSdTemplates = smtSDTemplatesMapper.selectById(tempId);
		if(smtSdTemplates == null || !parkIdList.contains(smtSdTemplates.getParkId())){
			return null;
		}

	//把数据库中的数据展示到前端
	List<SmtTemplatesRule> smtTemplatesRuleList = this.list(new QueryWrapper<SmtTemplatesRule>().lambda().eq(SmtTemplatesRule::getTempId,tempId));
		SmtTemplatesRuleReqDTO smtTemplatesRuleReqDTO = new SmtTemplatesRuleReqDTO();
		smtTemplatesRuleReqDTO.setTempId(tempId);
		List<SmtTemplatesRuleReqDTO.Rules> rulesList = new ArrayList<>();
		//按category分组
		Map<Integer,List<SmtTemplatesRule>> listMap = smtTemplatesRuleList.stream().collect(Collectors.groupingBy(SmtTemplatesRule::getCategoryId));

		for(Map.Entry<Integer,List<SmtTemplatesRule>> entry : listMap.entrySet()){
			SmtTemplatesRuleReqDTO.Rules rules = new SmtTemplatesRuleReqDTO.Rules();
			rules.setCategoryId(entry.getKey());
			List<SmtTemplatesRuleReqDTO.RulesData> rulesDataList = new ArrayList<>();
			for(SmtTemplatesRule smtTemplatesRule : entry.getValue()){
				SmtTemplatesRuleReqDTO.RulesData rulesData =  new SmtTemplatesRuleReqDTO.RulesData();
				rulesData.setMonthNum(smtTemplatesRule.getMonthNum());
				rulesData.setStandardQty(smtTemplatesRule.getStandardQty());
				rulesData.setOverFee(smtTemplatesRule.getOverFee());
				rulesDataList.add(rulesData);
			}
			rules.setRulesDataList(rulesDataList);
			rulesList.add(rules);
		}
		smtTemplatesRuleReqDTO.setRulesList(rulesList);
		return smtTemplatesRuleReqDTO;
	}
}
