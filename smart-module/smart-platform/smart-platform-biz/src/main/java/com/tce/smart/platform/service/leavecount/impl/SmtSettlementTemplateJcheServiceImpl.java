package com.tce.smart.platform.service.leavecount.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tce.smart.platform.api.dto.req.leavecount.SettlementTemplateJcheReqDTO;
import com.tce.smart.platform.core.entity.leavecount.SmtSettlementTemplateJche;
import com.tce.smart.platform.core.mapper.leavecount.SmtSettlementTemplateJcheMapper;
import com.tce.smart.platform.service.leavecount.SmtSettlementTemplateJcheService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 *
 *
 * @author fushiping
 * @date 2022-06-21 11:01:50
 */
@Service
public class SmtSettlementTemplateJcheServiceImpl extends ServiceImpl<SmtSettlementTemplateJcheMapper, SmtSettlementTemplateJche> implements SmtSettlementTemplateJcheService {

	@Override
	public Boolean saveJche(List<SettlementTemplateJcheReqDTO> reqDTO, Long itemId) {
		reqDTO.forEach(req-> {
			SmtSettlementTemplateJche jche = SmtSettlementTemplateJche.builder()
					.itemId(itemId).jcheId(req.getJcheId()).jcheName(req.getJcheName()).build();
			jche.insert();
		});
		return Boolean.TRUE;
	}
}
