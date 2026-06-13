package com.tce.smart.platform.service.leavecount.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tce.smart.common.core.exception.SmartException;
import com.tce.smart.common.core.util.BeanUtils;
import com.tce.smart.platform.api.dto.req.leavecount.SettlementTemplateReqDTO;
import com.tce.smart.platform.core.entity.leavecount.SmtSettlementTemplate;
import com.tce.smart.platform.core.mapper.leavecount.SmtSettlementTemplateMapper;
import com.tce.smart.platform.service.leavecount.SmtSettlementTemplateRangeService;
import com.tce.smart.platform.service.leavecount.SmtSettlementTemplateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 *
 *
 * @author fushiping
 * @date 2022-06-21 11:01:56
 */
@Service
public class SmtSettlementTemplateServiceImpl extends ServiceImpl<SmtSettlementTemplateMapper, SmtSettlementTemplate> implements SmtSettlementTemplateService {

	@Autowired
	private SmtSettlementTemplateRangeService templateRangeService;


	@Override
	@Transactional(rollbackFor = Exception.class)
	public Boolean editTemp(SettlementTemplateReqDTO reqDTO) {
		SmtSettlementTemplate template = BeanUtils.transform(SmtSettlementTemplate.class, reqDTO);
		this.saveOrUpdate(template);
		Integer count = this.count(Wrappers.<SmtSettlementTemplate>query()
				.lambda().eq(SmtSettlementTemplate::getTemplateName, reqDTO.getTemplateName()));
		if(count > 1) {
			throw new SmartException("该模板名已存在");
		}
		return Boolean.TRUE;
	}

	@Override
	public SmtSettlementTemplate getByRoomId(Integer roomId) {
		List<Long> ids = templateRangeService.getByRoomId(roomId);
		if (CollUtil.isNotEmpty(ids)) {
			List<SmtSettlementTemplate> templates = this.list(Wrappers.<SmtSettlementTemplate>lambdaQuery()
					.in(SmtSettlementTemplate::getId, ids)
			);
			if (CollUtil.isNotEmpty(templates)) {
				return templates.get(0);
			}
		}
		return null;
	}

}
