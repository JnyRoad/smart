package com.tce.smart.platform.service.leavecount.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tce.smart.common.core.exception.SmartException;
import com.tce.smart.platform.api.dto.req.leavecount.SettlementTemplateEditReqDTO;
import com.tce.smart.platform.api.dto.req.leavecount.SettlementTemplateItemReqDTO;
import com.tce.smart.platform.api.dto.req.leavecount.SettlementTemplateJcheReqDTO;
import com.tce.smart.platform.core.entity.leavecount.SmtSettlementTemplateItem;
import com.tce.smart.platform.core.entity.leavecount.SmtSettlementTemplateJche;
import com.tce.smart.platform.core.entity.leavecount.SmtSettlementTemplateRule;
import com.tce.smart.platform.core.mapper.leavecount.SmtSettlementTemplateItemMapper;
import com.tce.smart.platform.service.leavecount.SmtSettlementTemplateItemService;
import com.tce.smart.platform.service.leavecount.SmtSettlementTemplateJcheService;
import com.tce.smart.platform.service.leavecount.SmtSettlementTemplateRuleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author fushiping
 * @date 2022-06-21 11:01:50
 */
@Service
public class SmtSettlementTemplateItemServiceImpl extends ServiceImpl<SmtSettlementTemplateItemMapper, SmtSettlementTemplateItem> implements SmtSettlementTemplateItemService {

	@Autowired
	private SmtSettlementTemplateJcheService smtSettlementTemplateJcheService;

	@Autowired
	private SmtSettlementTemplateRuleService smtSettlementTemplateRuleService;

	@Override
	@Transactional(rollbackFor = Exception.class)
	public Boolean editItem(SettlementTemplateEditReqDTO reqDTO) {
		if (Objects.nonNull(reqDTO.getItemId())) {
			this.removeItem(reqDTO.getItemId());
		}
		//新增项
		if (Objects.nonNull(reqDTO.getRule())) {
			return this.saveItem(reqDTO);
		}
		return Boolean.TRUE;
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public Boolean removeItem(Long itemId) {
		//删除级层
		smtSettlementTemplateJcheService.remove(Wrappers.<SmtSettlementTemplateJche>lambdaQuery().eq(SmtSettlementTemplateJche::getItemId, itemId));
		//删除规则
		smtSettlementTemplateRuleService.remove(Wrappers.<SmtSettlementTemplateRule>lambdaQuery().eq(SmtSettlementTemplateRule::getItemId, itemId));
		//删除原有项
		return this.removeById(itemId);
	}

	/**
	 * 保存配置项
	 *
	 * @param reqDTO
	 * @return
	 */
	private Boolean saveItem(SettlementTemplateEditReqDTO reqDTO) {
		SettlementTemplateItemReqDTO item = reqDTO.getRule();
		this.checkJche(reqDTO);
		if (CollUtil.isEmpty(item.getJches()) || CollUtil.isEmpty(item.getRules())) {
			return Boolean.TRUE;
		}
		SmtSettlementTemplateItem templateItem = new SmtSettlementTemplateItem();
		templateItem.setTempId(reqDTO.getTempId());
		templateItem.insert();
		smtSettlementTemplateJcheService.saveJche(item.getJches(), templateItem.getId());
		smtSettlementTemplateRuleService.saveRule(item.getRules(), reqDTO.getTempId(), templateItem.getId());
		return Boolean.TRUE;
	}

	/**
	 * 检查是否存在重复级层配置
	 *
	 * @return
	 */
	private Boolean checkJche(SettlementTemplateEditReqDTO reqDTO) {
		List<SettlementTemplateJcheReqDTO> jcheReqDTOS = new ArrayList<>(reqDTO.getRule().getJches());
		if (CollUtil.isEmpty(jcheReqDTOS)) {
			throw new SmartException("级层配置为空");
		}
		List<String> reList = jcheReqDTOS.stream()
				.map(SettlementTemplateJcheReqDTO::getJcheName).collect(Collectors.toMap(e -> e, e -> 1, Integer::sum))
				.entrySet().stream()
				.filter(entry -> entry.getValue() > 1)
				.map(Map.Entry::getKey)
				.collect(Collectors.toList());
		if (CollUtil.isNotEmpty(reList)) {
			throw new SmartException("级层：" + reList + "重复配置");
		}
		return Boolean.TRUE;
	}
}
