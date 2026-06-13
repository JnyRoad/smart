package com.tce.smart.platform.service.leavecount;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tce.smart.platform.api.dto.req.leavecount.SettlementTemplateJcheReqDTO;
import com.tce.smart.platform.core.entity.leavecount.SmtSettlementTemplateJche;
import com.tce.smart.platform.core.entity.leavecount.SmtSettlementTemplateRule;

import java.util.List;

/**
 *
 *
 * @author fushiping
 * @date 2022-06-21 11:01:40
 */
public interface SmtSettlementTemplateJcheService extends IService<SmtSettlementTemplateJche> {

	/**
	 * 保存级层关联
	 * @param reqDTO
	 * @param itemId
	 * @return
	 */
	Boolean saveJche(List<SettlementTemplateJcheReqDTO> reqDTO, Long itemId);

}
